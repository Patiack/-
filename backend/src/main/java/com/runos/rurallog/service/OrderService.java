package com.runos.rurallog.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.runos.rurallog.dto.ActionRequest;
import com.runos.rurallog.dto.CreateOrderRequest;
import com.runos.rurallog.dto.PodRequest;
import com.runos.rurallog.entity.*;
import com.runos.rurallog.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 核心业务：订单状态机
 * 下行: CREATED → PICKED → AT_COUNTY → AT_TOWN → AT_VILLAGE → SIGNED
 * 上行: VILLAGE_RECEIVED → AT_TOWN → AT_COUNTY → DISPATCHED
 */
@Service
public class OrderService {

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderItemMapper itemMapper;
    @Autowired private WaybillMapper waybillMapper;
    @Autowired private TraceMapper traceMapper;
    @Autowired private PodMapper podMapper;
    @Autowired private StationMapper stationMapper;
    @Autowired private RouteMapper routeMapper;
    @Autowired private ObjectMapper objectMapper;

    /* ---------------- 下单 ---------------- */

    /** 子女端下单（SEND 代寄 / AGENT 代购），方向 DOWN */
    @Transactional
    public Order createOrder(CreateOrderRequest req) {
        if (req.getReceiverStationId() == null) throw new IllegalArgumentException("缺少收件村站点");
        Station village = stationMapper.selectById(req.getReceiverStationId());
        if (village == null || !"VILLAGE".equals(village.getType()))
            throw new IllegalArgumentException("收件站点不存在或不是村级站点");

        Order o = fillOrder(req);
        o.setType(req.getType() == null ? "SEND" : req.getType());
        o.setDirection("DOWN");
        o.setStatus("CREATED");
        orderMapper.insert(o);
        saveItems(o.getId(), req.getItems());

        Waybill wb = createWaybill(o, village.getId());
        addTrace(wb, null, "CREATED", "子女已下单，等待县城揽收", null);
        return o;
    }

    /** 村站点上行收件（农产品），方向 UP */
    @Transactional
    public Order agriReceive(Long stationId, CreateOrderRequest req) {
        Station village = stationMapper.selectById(stationId);
        if (village == null || !"VILLAGE".equals(village.getType()))
            throw new IllegalArgumentException("站点不存在或不是村级站点");

        Order o = fillOrder(req);
        o.setType("AGRI");
        o.setDirection("UP");
        o.setStatus("VILLAGE_RECEIVED");
        o.setReceiverStationId(stationId);
        if (o.getFreight() == null) o.setFreight(BigDecimal.ZERO);
        orderMapper.insert(o);
        saveItems(o.getId(), req.getItems());

        Waybill wb = createWaybill(o, stationId);
        addTrace(wb, stationId, "VILLAGE_RECEIVED",
                "村站点已收寄，称重 " + (o.getWeight() == null ? "-" : o.getWeight()) + "kg，拍照定价", null);
        return o;
    }

    /* ---------------- 站点操作 ---------------- */

    /** 入库扫描：按站点类型自动推进状态 */
    @Transactional
    public Waybill inbound(Long stationId, ActionRequest req) {
        Station st = stationMapper.selectById(stationId);
        Waybill wb = findWaybill(req.getWaybillNo());
        Order o = orderMapper.selectById(wb.getOrderId());
        assertNotSigned(o);

        String action;
        String desc;
        switch (st.getType()) {
            case "COUNTY":
                action = "AT_COUNTY";
                desc = st.getName() + " 集散分拣入库";
                break;
            case "TOWN":
                action = "AT_TOWN";
                desc = st.getName() + " hub 到件分拣";
                break;
            default:
                action = "AT_VILLAGE";
                desc = st.getName() + " 入库，派送员上门派送中";
        }
        // 上行件到县后发往全网
        if ("UP".equals(o.getDirection()) && "COUNTY".equals(st.getType())) {
            action = "DISPATCHED";
            desc = st.getName() + " 直发网点，转入全国网络";
        }
        advance(wb, o, st.getId(), action, desc, req.getOperatorId());
        return wb;
    }

    /** 班线装车发运 */
    @Transactional
    public Waybill dispatch(String waybillNo, ActionRequest req) {
        Waybill wb = findWaybill(waybillNo);
        Order o = orderMapper.selectById(wb.getOrderId());
        assertNotSigned(o);
        Route rt = routeMapper.selectById(req.getRouteId());
        String routeName = rt == null ? "班线" : rt.getName();
        wb.setRouteId(req.getRouteId());
        String action = "CREATED".equals(o.getStatus()) ? "PICKED" : o.getStatus();
        advance(wb, o, req.getStationId(), action, "已装车，搭载「" + routeName + "」发运", req.getOperatorId());
        return wb;
    }

    /** 签收拍照回传：置 SIGNED 并生成给子女的 POD */
    @Transactional
    public Pod uploadPod(String waybillNo, PodRequest req) {
        Waybill wb = findWaybill(waybillNo);
        Order o = orderMapper.selectById(wb.getOrderId());
        if ("SIGNED".equals(o.getStatus())) throw new IllegalStateException("该单已签收，请勿重复提交");

        Pod pod = new Pod();
        pod.setOrderId(o.getId());
        pod.setWaybillId(wb.getId());
        pod.setSignedName(req.getSignedName());
        pod.setVideoUrl(req.getVideoUrl());
        pod.setRelayText(req.getRelayText());
        try {
            pod.setPhotos(objectMapper.writeValueAsString(
                    req.getPhotos() == null ? Collections.emptyList() : req.getPhotos()));
        } catch (Exception e) {
            pod.setPhotos("[]");
        }
        podMapper.insert(pod);

        advance(wb, o, o.getReceiverStationId(), "SIGNED",
                (req.getSignedName() == null ? "收件人" : req.getSignedName()) + " 签收，照片已回传给家人", null);
        return pod;
    }

    /* ---------------- 查询 ---------------- */

    public List<Order> myOrders(Long userId) {
        return orderMapper.selectList(new QueryWrapper<Order>().eq("user_id", userId).orderByDesc("id"));
    }

    /** 订单详情：order + waybill + traces + pods + items + 站点名 */
    public Map<String, Object> detail(Long orderId) {
        Order o = orderMapper.selectById(orderId);
        if (o == null) throw new IllegalArgumentException("订单不存在");
        Waybill wb = waybillMapper.selectOne(new QueryWrapper<Waybill>().eq("order_id", orderId));
        List<Trace> traces = wb == null ? Collections.emptyList()
                : traceMapper.selectList(new QueryWrapper<Trace>().eq("waybill_id", wb.getId()).orderByAsc("id"));
        List<Pod> pods = podMapper.selectList(new QueryWrapper<Pod>().eq("order_id", orderId).orderByDesc("id"));
        List<OrderItem> items = itemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_id", orderId));

        Map<Long, String> stationNames = stationNameMap();
        List<Map<String, Object>> traceVos = traces.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("action", t.getAction());
            m.put("description", t.getDescription());
            m.put("createdAt", t.getCreatedAt());
            m.put("stationName", t.getStationId() == null ? null : stationNames.get(t.getStationId()));
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> res = new HashMap<>();
        res.put("order", o);
        res.put("waybill", wb);
        res.put("traces", traceVos);
        res.put("pods", pods);
        res.put("items", items);
        res.put("receiverStationName", o.getReceiverStationId() == null ? null : stationNames.get(o.getReceiverStationId()));
        return res;
    }

    /** 送达朋友圈：用户名下所有订单的签收回执流 */
    public List<Map<String, Object>> podFeed(Long userId) {
        List<Order> orders = myOrders(userId);
        if (orders.isEmpty()) return Collections.emptyList();
        List<Long> ids = orders.stream().map(Order::getId).collect(Collectors.toList());
        Map<Long, Order> orderIdx = orders.stream().collect(Collectors.toMap(Order::getId, x -> x));
        List<Pod> pods = podMapper.selectList(new QueryWrapper<Pod>().in("order_id", ids).orderByDesc("id"));
        List<Map<String, Object>> feed = new ArrayList<>();
        for (Pod p : pods) {
            Map<String, Object> m = new HashMap<>();
            m.put("pod", p);
            m.put("order", orderIdx.get(p.getOrderId()));
            feed.add(m);
        }
        return feed;
    }

    public Map<Long, String> stationNameMap() {
        return stationMapper.selectList(null).stream()
                .collect(Collectors.toMap(Station::getId, Station::getName));
    }

    /* ---------------- 内部工具 ---------------- */

    private Order fillOrder(CreateOrderRequest req) {
        Order o = new Order();
        o.setOrderNo("RL" + System.currentTimeMillis());
        o.setUserId(req.getUserId());
        o.setSenderName(req.getSenderName());
        o.setSenderPhone(req.getSenderPhone());
        o.setReceiverName(req.getReceiverName());
        o.setReceiverPhone(req.getReceiverPhone());
        o.setReceiverStationId(req.getReceiverStationId());
        o.setItemDesc(req.getItemDesc());
        o.setWeight(req.getWeight());
        o.setGoodsAmount(req.getGoodsAmount() == null ? BigDecimal.ZERO : req.getGoodsAmount());
        o.setFreight(req.getFreight());
        o.setRemark(req.getRemark());
        return o;
    }

    private void saveItems(Long orderId, List<OrderItem> items) {
        if (items == null) return;
        for (OrderItem it : items) {
            it.setId(null);
            it.setOrderId(orderId);
            itemMapper.insert(it);
        }
    }

    private Waybill createWaybill(Order o, Long stationId) {
        Waybill wb = new Waybill();
        wb.setWaybillNo("WB" + o.getId());
        wb.setOrderId(o.getId());
        wb.setCurrentStationId(stationId);
        wb.setStatus(o.getStatus());
        waybillMapper.insert(wb);
        return wb;
    }

    /** 推进状态：更新订单+运单，写轨迹 */
    private void advance(Waybill wb, Order o, Long stationId, String action, String desc, Long operatorId) {
        o.setStatus(action);
        orderMapper.updateById(o);
        wb.setStatus(action);
        if (stationId != null) wb.setCurrentStationId(stationId);
        waybillMapper.updateById(wb);
        addTrace(wb, stationId, action, desc, operatorId);
    }

    private void addTrace(Waybill wb, Long stationId, String action, String desc, Long operatorId) {
        Trace t = new Trace();
        t.setWaybillId(wb.getId());
        t.setStationId(stationId);
        t.setAction(action);
        t.setDescription(desc);
        t.setOperatorId(operatorId);
        traceMapper.insert(t);
    }

    private Waybill findWaybill(String waybillNo) {
        Waybill wb = waybillMapper.selectOne(new QueryWrapper<Waybill>().eq("waybill_no", waybillNo));
        if (wb == null) throw new IllegalArgumentException("运单不存在: " + waybillNo);
        return wb;
    }

    private void assertNotSigned(Order o) {
        if ("SIGNED".equals(o.getStatus()) || "DISPATCHED".equals(o.getStatus()))
            throw new IllegalStateException("订单状态[" + o.getStatus() + "]不允许继续操作");
    }
}
