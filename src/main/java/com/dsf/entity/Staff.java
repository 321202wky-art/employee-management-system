package com.dsf.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("staff")
public class Staff {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String gender;
    private Integer age;
    @TableField("dept_name")
    private String deptName;
    private String phone;
    private String education;
    @TableField("workStatus")
    private String workStatus;
    @TableField("id_care")
    private String idCare;
    @TableField("native_place")
    private String nativePlace;
    private String address;
//    @TableField(value = "create_time",fill = FieldFill.INSERT)
//    private LocalDateTime createTime;//创建时间,自动填充
//    @TableField(value = "update_time",fill = FieldFill.INSERT)
//    private LocalDateTime updateTime;//更新时间,自动填充
    @TableField(value = "bank_card")
    private String bankCard;
    @TableField(value = "basic_salary")
    private double basicSalary;
}
