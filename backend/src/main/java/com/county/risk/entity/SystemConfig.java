package com.county.risk.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统配置实体
 */
@Data
@TableName("system_configs")
public class SystemConfig implements Serializable {
    @TableId(value = "config_id", type = IdType.AUTO)
    private Integer configId;

    private String configKey;

    private String configValue;

    private String configType;

    private String category;

    private String description;

    private Boolean isEditable;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer updatedBy;
}
