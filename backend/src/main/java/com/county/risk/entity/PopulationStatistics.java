package com.county.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 人口统计实体
 */
@Data
@TableName("population_statistics")
public class PopulationStatistics implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String countyCode;
    private Integer year;
    private Integer totalHouseholds;
    private Integer ruralHouseholds;
    @com.baomidou.mybatisplus.annotation.TableField("total_population_万")
    private BigDecimal totalPopulation万;
    @com.baomidou.mybatisplus.annotation.TableField("rural_population_万")
    private BigDecimal ruralPopulation万;
    @com.baomidou.mybatisplus.annotation.TableField("registered_population_万")
    private BigDecimal registeredPopulation万;
    private BigDecimal populationDensity;
    private BigDecimal urbanizationRate;
}
