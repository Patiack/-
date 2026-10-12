package com.runos.rurallog.controller;

import com.runos.rurallog.common.R;
import com.runos.rurallog.dto.PodRequest;
import com.runos.rurallog.dto.CreateOrderRequest;
import com.runos.rurallog.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/** 子女端：下单 / 订单查询 / 签收回执(送达朋友圈) / 签收拍照回传 */
@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired private OrderService orderService;

    @PostMapping("/orders")
    public R create(@RequestBody CreateOrderRequest req) {
        return R.ok(orderService.createOrder(req));
    }

    @GetMapping("/orders")
    public R myOrders(@RequestParam Long userId) {
        return R.ok(orderService.myOrders(userId));
    }

    @GetMapping("/orders/{id}")
    public R detail(@PathVariable Long id) {
        return R.ok(orderService.detail(id));
    }

    /** 送达朋友圈：回传给子女的签收照片/视频流 */
    @GetMapping("/pods")
    public R podFeed(@RequestParam Long userId) {
        return R.ok(orderService.podFeed(userId));
    }

    /** 站点/派送员上传签收拍照回传（路径参数是运单号） */
    @PostMapping("/orders/{waybillNo}/pod")
    public R pod(@PathVariable String waybillNo, @RequestBody PodRequest req) {
        return R.ok(orderService.uploadPod(waybillNo, req));
    }
}
