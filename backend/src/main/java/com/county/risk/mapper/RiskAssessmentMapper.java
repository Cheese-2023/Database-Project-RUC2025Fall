package com.county.risk.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.county.risk.entity.ComprehensiveRiskAssessment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 综合风险评估Mapper接口
 */
@Mapper
public interface RiskAssessmentMapper extends BaseMapper<ComprehensiveRiskAssessment> {

        /**
         * 获取风险统计信息
         * 
         * @return 统计结果：各等级数量、平均分等
         */
        @Select("SELECT " +
                        "COUNT(*) as totalCount, " +
                        "AVG(t.comprehensive_risk_score) as avgScore, " +
                        "MIN(t.comprehensive_risk_score) as minScore, " +
                        "MAX(t.comprehensive_risk_score) as maxScore, " +
                        "SUM(CASE WHEN t.risk_level = '低风险' THEN 1 ELSE 0 END) as lowRiskCount, " +
                        "SUM(CASE WHEN t.risk_level = '中低风险' THEN 1 ELSE 0 END) as mediumLowRiskCount, " +
                        "SUM(CASE WHEN t.risk_level = '中风险' THEN 1 ELSE 0 END) as mediumRiskCount, " +
                        "SUM(CASE WHEN t.risk_level = '中高风险' THEN 1 ELSE 0 END) as mediumHighRiskCount, " +
                        "SUM(CASE WHEN t.risk_level = '高风险' THEN 1 ELSE 0 END) as highRiskCount " +
                        "FROM ( " +
                        "  SELECT cra.* FROM comprehensive_risk_assessment cra " +
                        "  INNER JOIN ( " +
                        "    SELECT county_code, MAX(year) AS latest_year " +
                        "    FROM comprehensive_risk_assessment " +
                        "    GROUP BY county_code " +
                        "  ) latest ON cra.county_code = latest.county_code AND cra.year = latest.latest_year " +
                        ") t")
        Map<String, Object> getRiskStatistics();

        /**
         * 获取县域风险趋势（按年份）
         * 
         * @param countyCode 县域代码
         * @return 历年风险数据
         */
        @Select("SELECT year, comprehensive_risk_score, risk_level " +
                        "FROM comprehensive_risk_assessment " +
                        "WHERE county_code = #{countyCode} " +
                        "ORDER BY year ASC")
        List<Map<String, Object>> getRiskTrendByCounty(@Param("countyCode") String countyCode);

        /**
         * 获取指定年份的平均风险趋势
         * 
         * @return 历年平均风险数据
         */
        @Select("SELECT year, AVG(comprehensive_risk_score) as avgScore " +
                        "FROM comprehensive_risk_assessment " +
                        "GROUP BY year " +
                        "ORDER BY year ASC")
        List<Map<String, Object>> getAverageRiskTrend();

        /**
         * 获取最新年份的高风险县域TOP N
         * 
         * @param limit 返回数量
         * @return 高风险县域列表
         */
        @Select("SELECT cra.*, cb.county_name, cb.province_name " +
                        "FROM comprehensive_risk_assessment cra " +
                        "JOIN county_basic cb ON cra.county_code = cb.county_code " +
                        "WHERE cra.year = (SELECT MAX(year) FROM comprehensive_risk_assessment) " +
                        "ORDER BY cra.comprehensive_risk_score DESC " +
                        "LIMIT #{limit}")
        List<Map<String, Object>> getTopRiskCounties(@Param("limit") int limit);

        /**
         * 获取风险评估列表（包含县域信息）
         */
        @Select({
                        "<script>",
                        "SELECT cra.*, cb.county_name, cb.city_name, cb.province_name ",
                        "FROM comprehensive_risk_assessment cra ",
                        "JOIN county_basic cb ON cra.county_code = cb.county_code ",
                        "WHERE 1 = 1 ",
                        "<choose>",
                        "  <when test='year != null'>",
                        "    AND cra.year = #{year} ",
                        "  </when>",
                        "  <otherwise>",
                        "    AND cra.year = (SELECT MAX(year) FROM comprehensive_risk_assessment) ",
                        "  </otherwise>",
                        "</choose>",
                        "<if test='level != null and level != \"\"'>",
                        "  AND cra.risk_level = #{level} ",
                        "</if>",
                        "<if test='provinceName != null and provinceName != \"\"'>",
                        "  AND cb.province_name = #{provinceName} ",
                        "</if>",
                        "ORDER BY cra.comprehensive_risk_score DESC",
                        "</script>"
        })
        List<Map<String, Object>> getRiskListWithCountyInfo(@Param("level") String level,
                        @Param("provinceName") String provinceName,
                        @Param("year") Integer year);

        /**
         * 获取最新的评估年份
         */
        @Select("SELECT MAX(year) FROM comprehensive_risk_assessment")
        Integer getLatestAssessmentYear();

        @Select("SELECT COUNT(DISTINCT county_code) FROM comprehensive_risk_assessment")
        Long countDistinctCounties();

        /**
         * 获取数据表中的最小年份
         */
        @Select("SELECT MIN(year) FROM economic_aggregate")
        Integer getMinDataYear();

        /**
         * 获取数据表中的最大年份
         */
        @Select("SELECT MAX(year) FROM economic_aggregate")
        Integer getMaxDataYear();

        /**
         * 获取所有有基础数据的年份
         */
        @Select("SELECT DISTINCT year FROM economic_aggregate ORDER BY year")
        List<Integer> getYearsWithData();

        /**
         * 获取所有已有风险评估的年份
         */
        @Select("SELECT DISTINCT year FROM comprehensive_risk_assessment ORDER BY year")
        List<Integer> getYearsWithAssessment();
}
