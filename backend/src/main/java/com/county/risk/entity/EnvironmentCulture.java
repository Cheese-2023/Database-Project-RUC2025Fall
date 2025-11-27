package com.county.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 环境与文化实体
 */
@Data
@TableName("environment_culture")
public class EnvironmentCulture implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String countyCode;
    private Integer year;
    @com.baomidou.mybatisplus.annotation.TableField("nox_emission_吨")
    private BigDecimal noxEmission吨;
    @com.baomidou.mybatisplus.annotation.TableField("dust_emission_吨")
    private BigDecimal dustEmission吨;
    @com.baomidou.mybatisplus.annotation.TableField("so2_emission_吨")
    private BigDecimal so2Emission吨;
    private Integer theatersCount;
    @com.baomidou.mybatisplus.annotation.TableField("library_collection_千册")
    private BigDecimal libraryCollection千册;
    private Integer sportsVenues;
    private BigDecimal airQualityIndex;
    private BigDecimal greenCoverageRate;
    private BigDecimal emissionIntensity;
}
