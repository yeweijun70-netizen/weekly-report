package com.company.weeklyreport.controller;

import com.company.weeklyreport.common.Result;
import com.company.weeklyreport.entity.User;
import com.company.weeklyreport.service.UserService;
import com.company.weeklyreport.util.JwtUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "认证管理")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @ApiOperation("登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        User user = userService.login(dto.getEmail(), dto.getPassword());
        if (user == null) {
            return Result.error("邮箱或密码错误");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);

        return Result.success(data);
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getUserWithInfo(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @ApiOperation("登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @ApiOperation("注册")
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterDTO dto) {
        try {
            User user = userService.register(dto.getUsername(), dto.getEmail(), dto.getPassword());
            user.setPassword(null);
            String token = jwtUtils.generateToken(user.getId(), user.getUsername());
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            return Result.success(data);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        }
    }

    @Data
    public static class LoginDTO {
        private String email;
        private String password;
    }

    @Data
    public static class RegisterDTO {
        private String username;
        private String email;
        private String password;
    }
}

