package com.county.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 县域基础信息实体
 */
@Data
@TableName("county_basic")
public class CountyBasic implements Serializable {
    @TableId(type = IdType.INPUT)
    private String countyCode;
    private String countyName;
    private String cityName;
    private String provinceName;
    private String regionCode;
    private String developmentLevel;
    private BigDecimal landAreaKm2;
    private String administrativeLevel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
