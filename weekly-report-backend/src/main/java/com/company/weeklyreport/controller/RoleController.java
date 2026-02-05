package com.company.weeklyreport.controller;

import com.company.weeklyreport.common.Result;
import com.company.weeklyreport.entity.Role;
import com.company.weeklyreport.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "瑙掕壊绠＄悊")
@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @ApiOperation("瑙掕壊鍒楄〃")
    @GetMapping("/list")
    public Result<List<Role>> list() {
        return Result.success(roleService.listAll());
    }

    @ApiOperation("瑙掕壊璇︽儏")
    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        return Result.success(roleService.getRoleWithCount(id));
    }

    @ApiOperation("保存角色")
    @PostMapping
    public Result<Void> save(@RequestBody Role role) {
        if (roleService.checkCodeExists(role.getCode(), role.getId())) {
            return Result.error("角色编码已存在");
        }
        roleService.saveOrUpdate(role);
        roleService.evictRoleCache();
        return Result.success();
    }

    @ApiOperation("更新角色权限")
    @PutMapping("/{id}/permissions")
    public Result<Void> updatePermissions(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Role role = roleService.getById(id);
        if (role == null) {
            return Result.error("角色不存在");
        }
        Object permissions = body.get("permissions");
        role.setPermissions(permissions != null ? permissions.toString() : null);
        roleService.updateById(role);
        roleService.evictRoleCache();
        return Result.success();
    }

    @ApiOperation("删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.removeById(id);
        roleService.evictRoleCache();
        return Result.success();
    }
}

