package com.county.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 经济总量统计实体
 */
@Data
@TableName("economic_aggregate")
public class EconomicAggregate implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String countyCode;
    private Integer year;
    @com.baomidou.mybatisplus.annotation.TableField("gdp_万元")
    private Long gdp万元;
    @com.baomidou.mybatisplus.annotation.TableField("primary_industry_万元")
    private Long primaryIndustry万元;
    @com.baomidou.mybatisplus.annotation.TableField("secondary_industry_万元")
    private Long secondaryIndustry万元;
    @com.baomidou.mybatisplus.annotation.TableField("industrial_value_万元")
    private Long industrialValue万元;
    @com.baomidou.mybatisplus.annotation.TableField("tertiary_industry_万元")
    private Long tertiaryIndustry万元;
    @com.baomidou.mybatisplus.annotation.TableField("agriculture_value_万元")
    private Long agricultureValue万元;
    @com.baomidou.mybatisplus.annotation.TableField("livestock_value_万元")
    private Long livestockValue万元;
    private BigDecimal gdpPerCapita;
    private BigDecimal gdpGrowthRate;
    @com.baomidou.mybatisplus.annotation.TableField("industry_structure_1")
    private BigDecimal industryStructure1;
    @com.baomidou.mybatisplus.annotation.TableField("industry_structure_2")
    private BigDecimal industryStructure2;
    @com.baomidou.mybatisplus.annotation.TableField("industry_structure_3")
    private BigDecimal industryStructure3;
}
