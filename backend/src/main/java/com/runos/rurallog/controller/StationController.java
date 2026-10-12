package com.runos.rurallog.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.runos.rurallog.common.R;
import com.runos.rurallog.dto.ActionRequest;
import com.runos.rurallog.dto.CreateOrderRequest;
import com.runos.rurallog.entity.Station;
import com.runos.rurallog.mapper.StationMapper;
import com.runos.rurallog.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/** 站点端：站点查询 / 入库扫描 / 班线发运 / 农产品上行收件 */
@RestController
@RequestMapping("/api")
public class StationController {

    @Autowired private StationMapper stationMapper;
    @Autowired private OrderService orderService;

    @GetMapping("/stations")
    public R list(@RequestParam(required = false) String type) {
        QueryWrapper<Station> qw = new QueryWrapper<>();
        if (type != null && !type.isEmpty()) qw.eq("type", type);
        return R.ok(stationMapper.selectList(qw.orderByAsc("id")));
    }

    /** 入库扫描：按站点类型自动推进状态 */
    @PostMapping("/stations/{stationId}/inbound")
    public R inbound(@PathVariable Long stationId, @RequestBody ActionRequest req) {
        req.setStationId(stationId);
        orderService.inbound(stationId, req);
        Map<String, Object> d = new HashMap<>();
        d.put("waybillNo", req.getWaybillNo());
        d.put("next", "若为村站: 可上门派送并上传签收拍照; 若为县/镇: 等待班线发运");
        return R.ok(d);
    }

    /** 班线装车发运 */
    @PostMapping("/waybills/{waybillNo}/dispatch")
    public R dispatch(@PathVariable String waybillNo, @RequestBody ActionRequest req) {
        orderService.dispatch(waybillNo, req);
        return R.ok();
    }

    /** 上行收件：村民在村站交寄农产品 */
    @PostMapping("/stations/{stationId}/agri-receive")
    public R agri(@PathVariable Long stationId, @RequestBody CreateOrderRequest req) {
        return R.ok(orderService.agriReceive(stationId, req));
    }
}
