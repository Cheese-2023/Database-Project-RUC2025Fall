package com.county.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 财政金融实体
 */
@Data
@TableName("fiscal_finance")
public class FiscalFinance implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String countyCode;
    private Integer year;
    @com.baomidou.mybatisplus.annotation.TableField("fiscal_revenue_万元")
    private Long fiscalRevenue万元;
    @com.baomidou.mybatisplus.annotation.TableField("tax_revenue_万元")
    private Long taxRevenue万元;
    @com.baomidou.mybatisplus.annotation.TableField("fiscal_expenditure_万元")
    private Long fiscalExpenditure万元;
    @com.baomidou.mybatisplus.annotation.TableField("savings_balance_万元")
    private Long savingsBalance万元;
    @com.baomidou.mybatisplus.annotation.TableField("loan_balance_万元")
    private Long loanBalance万元;
    private BigDecimal fiscalSelfSufficiency;
    private BigDecimal debtToRevenueRatio;
}
