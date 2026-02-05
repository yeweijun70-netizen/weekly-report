package com.company.weeklyreport.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String email;

    private String password;

    private Long departmentId;

    private Long roleId;

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
    private String departmentName;

    @TableField(exist = false)
    private String roleName;
}

