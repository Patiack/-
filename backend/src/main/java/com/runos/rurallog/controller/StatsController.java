package com.runos.rurallog.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.runos.rurallog.common.R;
import com.runos.rurallog.entity.Order;
import com.runos.rurallog.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/** 运营总览数据 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired private OrderMapper orderMapper;
    @Autowired private PodMapper podMapper;
    @Autowired private RouteMapper routeMapper;
    @Autowired private ScheduleMapper scheduleMapper;
    @Autowired private StationMapper stationMapper;

    @GetMapping
    public R stats() {
        Map<String, Object> d = new HashMap<>();
        d.put("stationCount", stationMapper.selectCount(null));
        d.put("routeCount", routeMapper.selectCount(null));
        d.put("podCount", podMapper.selectCount(null));
        d.put("todaySchedules", scheduleMapper.selectCount(
                new QueryWrapper<com.runos.rurallog.entity.Schedule>().apply("DATE(depart_time) = CURDATE()")));
        d.put("totalOrders", orderMapper.selectCount(null));
        d.put("todayOrders", orderMapper.selectCount(
                new QueryWrapper<Order>().apply("DATE(created_at) = CURDATE()")));
        d.put("todayDownOrders", orderMapper.selectCount(
                new QueryWrapper<Order>().eq("direction", "DOWN").apply("DATE(created_at) = CURDATE()")));
        d.put("todayAgriOrders", orderMapper.selectCount(
                new QueryWrapper<Order>().eq("type", "AGRI").apply("DATE(created_at) = CURDATE()")));
        d.put("inTransit", orderMapper.selectCount(
                new QueryWrapper<Order>().notIn("status", "SIGNED", "DISPATCHED")));
        d.put("signedToday", orderMapper.selectCount(
                new QueryWrapper<Order>().eq("status", "SIGNED").apply("DATE(updated_at) = CURDATE()")));
        return R.ok(d);
    }
}
