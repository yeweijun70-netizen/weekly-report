package com.company.weeklyreport.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("weekly_report")
public class WeeklyReport {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 鍛ㄦ */
    private Integer weekNumber;

    /** 骞翠唤 */
    private Integer year;

    /** 鏈懆宸ヤ綔 */
    private String thisWeekWork;

    /** 涓嬪懆璁″垝 */
    private String nextWeekPlan;

    /** 闇€鍗忚皟浜嬮」 */
    private String coordination;

    /** 鐘舵€? 0-鑽夌, 1-宸叉彁浜?*/
    private Integer status;

    /** 璇勫垎 */
    private BigDecimal score;

    /** 璇勮 */
    private String comment;

    /** 璇勫垎浜篒D */
    private Long reviewerId;

    private LocalDateTime submitTime;

    private LocalDateTime reviewTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    // 闈炴暟鎹簱瀛楁
    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String departmentName;
}

