package com.county.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 投资消费实体
 */
@Data
@TableName("investment_consumption")
public class InvestmentConsumption implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String countyCode;
    private Integer year;
    @com.baomidou.mybatisplus.annotation.TableField("urban_fixed_investment_万元")
    private Long urbanFixedInvestment万元;
    @com.baomidou.mybatisplus.annotation.TableField("total_fixed_investment_万元")
    private Long totalFixedInvestment万元;
    @com.baomidou.mybatisplus.annotation.TableField("retail_sales_万元")
    private Long retailSales万元;
    @com.baomidou.mybatisplus.annotation.TableField("real_estate_investment_亿元")
    private BigDecimal realEstateInvestment亿元;
    private BigDecimal investmentEfficiency;
    private BigDecimal consumptionRate;
}
