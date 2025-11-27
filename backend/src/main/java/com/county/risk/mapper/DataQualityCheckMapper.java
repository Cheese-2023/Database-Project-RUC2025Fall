package com.county.risk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.county.risk.dto.DataQualityIssueDTO;
import com.county.risk.entity.DataQualityCheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DataQualityCheckMapper extends BaseMapper<DataQualityCheck> {

	@Select("SELECT COUNT(*) FROM data_quality_checks WHERE status IN ('待处理', '处理中')")
	Long countUnresolvedIssues();

	@Select("SELECT d.id, d.table_name, d.severity, d.status, d.issue_description, d.detected_at, " +
			"cb.county_name " +
			"FROM data_quality_checks d " +
			"LEFT JOIN county_basic cb ON d.county_code = cb.county_code " +
			"WHERE d.status IN ('待处理', '处理中') " +
			"ORDER BY d.detected_at DESC LIMIT #{limit}")
	List<Map<String, Object>> selectPendingIssues(@Param("limit") int limit);

	@Select("SELECT d.id AS id, d.table_name AS tableName, d.county_code AS countyCode, d.year AS year, " +
			"cb.county_name AS countyName, cb.province_name AS provinceName, d.check_type AS checkType, " +
			"d.field_name AS fieldName, d.issue_description AS issueDescription, d.expected_value AS expectedValue, " +
			"d.actual_value AS actualValue, d.severity, d.status, d.detected_at AS detectedAt, " +
			"d.resolved_at AS resolvedAt, d.resolution_comment AS resolutionComment " +
			"FROM data_quality_checks d " +
			"LEFT JOIN county_basic cb ON d.county_code = cb.county_code " +
			"ORDER BY d.detected_at DESC")
	IPage<DataQualityIssueDTO> selectIssuePage(Page<?> page);
}
