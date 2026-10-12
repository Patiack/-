package com.runos.rurallog.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.runos.rurallog.common.R;
import com.runos.rurallog.entity.User;
import com.runos.rurallog.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** MVP mock 登录：手机号即账号 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserMapper userMapper;

    @PostMapping("/login")
    public R login(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.isEmpty()) return R.fail(1001, "手机号不能为空");
        User u = userMapper.selectOne(new QueryWrapper<User>().eq("phone", phone));
        if (u == null) return R.fail(2001, "账号不存在，请使用演示账号(见页面提示)");
        return R.ok(u);
    }
}
