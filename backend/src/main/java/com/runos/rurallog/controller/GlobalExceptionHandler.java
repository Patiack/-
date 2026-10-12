package com.runos.rurallog.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.runos.rurallog.common.R;

/** 业务异常 → 统一错误码 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public R badRequest(IllegalArgumentException e) {
        return R.fail(2001, e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public R conflict(IllegalStateException e) {
        return R.fail(2002, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R serverError(Exception e) {
        return R.fail(500, "服务异常: " + e.getMessage());
    }
}
