package com.company.weeklyreport.controller;

import com.company.weeklyreport.common.Result;
import com.company.weeklyreport.entity.Department;
import com.company.weeklyreport.service.DepartmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "部门管理")
@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @ApiOperation("部门树")
    @GetMapping("/tree")
    public Result<List<Department>> tree() {
        return Result.success(departmentService.getTree());
    }

    @ApiOperation("保存部门")
    @PostMapping
    public Result<Void> save(@RequestBody Department department) {
        departmentService.saveOrUpdate(department);
        departmentService.evictDepartmentCache();
        return Result.success();
    }

    @ApiOperation("删除部门")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.removeById(id);
        departmentService.evictDepartmentCache();
        return Result.success();
    }
}

