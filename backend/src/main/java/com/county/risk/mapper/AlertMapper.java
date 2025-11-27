package com.county.risk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.county.risk.entity.Alert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;
import java.util.List;

@Mapper
public interface AlertMapper extends BaseMapper<Alert> {

    @Select("SELECT a.*, cb.county_name, cb.province_name " +
            "FROM alerts a " +
            "LEFT JOIN county_basic cb ON a.county_code = cb.county_code " +
            "ORDER BY a.created_at DESC")
    IPage<Map<String, Object>> selectAlertsWithCountyInfo(Page<?> page);

        @Select("SELECT a.alert_id, a.title, a.alert_type, a.risk_level, a.status, a.created_at, " +
            "cb.county_name, cb.province_name " +
            "FROM alerts a " +
            "LEFT JOIN county_basic cb ON a.county_code = cb.county_code " +
            "ORDER BY a.created_at DESC LIMIT #{limit}")
        List<Map<String, Object>> selectRecentAlerts(@Param("limit") int limit);
}
