package com.runos.rurallog.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.runos.rurallog.common.R;
import com.runos.rurallog.entity.Route;
import com.runos.rurallog.entity.Schedule;
import com.runos.rurallog.mapper.RouteMapper;
import com.runos.rurallog.mapper.ScheduleMapper;
import com.runos.rurallog.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 班线 & 排班（运营端） */
@RestController
@RequestMapping("/api")
public class RouteController {

    @Autowired private RouteMapper routeMapper;
    @Autowired private ScheduleMapper scheduleMapper;
    @Autowired private OrderService orderService;

    @GetMapping("/routes")
    public R routes() {
        Map<Long, String> names = orderService.stationNameMap();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Route rt : routeMapper.selectList(null)) {
            StringBuilder chain = new StringBuilder();
            for (String sid : rt.getStations().split(",")) {
                if (chain.length() > 0) chain.append(" → ");
                chain.append(names.getOrDefault(Long.valueOf(sid.trim()), "?"));
            }
            Map<String, Object> m = new HashMap<>();
            m.put("id", rt.getId());
            m.put("name", rt.getName());
            m.put("description", rt.getDescription());
            m.put("stationChain", chain.toString());
            list.add(m);
        }
        return R.ok(list);
    }

    @GetMapping("/schedules")
    public R schedules(@RequestParam(required = false) String date) {
        QueryWrapper<Schedule> qw = new QueryWrapper<>();
        if (date != null && !date.isEmpty()) {
            qw.ge("depart_time", date + " 00:00:00").le("depart_time", date + " 23:59:59");
        }
        Map<Long, String> routeNames = new HashMap<>();
        for (Route rt : routeMapper.selectList(null)) routeNames.put(rt.getId(), rt.getName());
        List<Map<String, Object>> list = new ArrayList<>();
        for (Schedule s : scheduleMapper.selectList(qw.orderByAsc("depart_time"))) {
            Map<String, Object> m = new HashMap<>();
            m.put("schedule", s);
            m.put("routeName", routeNames.get(s.getRouteId()));
            list.add(m);
        }
        return R.ok(list);
    }

    @PostMapping("/schedules")
    public R add(@RequestBody Schedule s) {
        s.setId(null);
        if (s.getStatus() == null) s.setStatus("PENDING");
        scheduleMapper.insert(s);
        return R.ok(s);
    }
}
