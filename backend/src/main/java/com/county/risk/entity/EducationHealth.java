package com.county.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 教育卫生实体
 */
@Data
@TableName("education_health")
public class EducationHealth implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String countyCode;
    private Integer year;
    @com.baomidou.mybatisplus.annotation.TableField("education_investment_万元")
    private Long educationInvestment万元;
    @com.baomidou.mybatisplus.annotation.TableField("health_investment_万元")
    private Long healthInvestment万元;
    // Other fields omitted for brevity as they are not currently used in
    // calculation
}
