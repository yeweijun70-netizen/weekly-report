package com.company.weeklyreport.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.weeklyreport.common.Result;
import com.company.weeklyreport.entity.OperationLog;
import com.company.weeklyreport.service.OperationLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "操作日志管理")
@RestController
@RequestMapping("/operation-log")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @ApiOperation("操作日志分页列表")
    @GetMapping("/page")
    public Result<IPage<OperationLog>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Page<OperationLog> page = new Page<>(current, size);
        IPage<OperationLog> result = operationLogService.pageList(page, username, module, startTime, endTime);
        return Result.success(result);
    }
}
