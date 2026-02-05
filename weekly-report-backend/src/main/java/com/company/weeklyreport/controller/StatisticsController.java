package com.company.weeklyreport.controller;

import com.company.weeklyreport.common.Result;
import com.company.weeklyreport.service.WeeklyReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

@Api(tags = "统计分析")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private WeeklyReportService weeklyReportService;

    @ApiOperation("提交率统计")
    @GetMapping("/submit-rate")
    public Result<Map<String, Object>> submitRate(@RequestParam Integer year, @RequestParam Integer weekNumber) {
        Map<String, Object> stats = weeklyReportService.getSubmitStats(year, weekNumber);
        return Result.success(stats);
    }

    @ApiOperation("评分趋势")
    @GetMapping("/score-trend")
    public Result<Map<String, Object>> scoreTrend() {
        // 模拟数据，实际应从数据库查询
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.CHINA);
        int currentWeek = now.get(weekFields.weekOfYear());

        List<String> weeks = new ArrayList<>();
        List<Double> avgScores = new ArrayList<>();

        for (int i = 4; i >= 1; i--) {
            weeks.add("第" + (currentWeek - i + 1) + "周");
            avgScores.add(4.0 + Math.random());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("weeks", weeks);
        data.put("avgScores", avgScores);
        return Result.success(data);
    }
}

