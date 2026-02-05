package com.company.weeklyreport.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_role")
public class Role {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String code;

    private String description;

    /** 鏉冮檺JSON */
    private String permissions;

    /** 鐘舵€? 0-绂佺敤, 1-鍚敤 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    // 闈炴暟鎹簱瀛楁
    @TableField(exist = false)
    private Integer userCount;
}

