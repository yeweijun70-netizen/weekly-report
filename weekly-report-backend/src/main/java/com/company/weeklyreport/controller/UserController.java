package com.company.weeklyreport.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.weeklyreport.common.Result;
import com.company.weeklyreport.entity.User;
import com.company.weeklyreport.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "鐢ㄦ埛绠＄悊")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation("鐢ㄦ埛鍒嗛〉鍒楄〃")
    @GetMapping("/page")
    public Result<IPage<User>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long roleId) {
        Page<User> page = new Page<>(current, size);
        IPage<User> result = userService.pageList(page, keyword, departmentId, roleId);
        return Result.success(result);
    }

    @ApiOperation("鐢ㄦ埛璇︽儏")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getUserWithInfo(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @ApiOperation("保存用户")
    @PostMapping
    public Result<Void> save(@RequestBody User user) {
        if (userService.checkEmailExists(user.getEmail(), user.getId())) {
            return Result.error("邮箱已存在");
        }
        userService.saveUser(user);
        if (user.getId() != null) {
            userService.evictUserCache(user.getId());
        }
        return Result.success();
    }

    @ApiOperation("更新用户")
    @PutMapping
    public Result<Void> update(@RequestBody User user) {
        if (userService.checkEmailExists(user.getEmail(), user.getId())) {
            return Result.error("邮箱已存在");
        }
        userService.updateById(user);
        if (user.getId() != null) {
            userService.evictUserCache(user.getId());
        }
        return Result.success();
    }

    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        userService.evictUserCache(id);
        return Result.success();
    }
}

