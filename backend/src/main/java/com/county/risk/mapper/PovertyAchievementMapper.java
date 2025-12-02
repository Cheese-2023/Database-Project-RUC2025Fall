package com.county.risk.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 脱贫攻坚成果展示Mapper
 */
@Mapper
public interface PovertyAchievementMapper {
    
    /**
     * 获取贫困县按省份统计
     * 注意：这里使用${}是因为需要动态构建IN子句，povertyCountyCodes应该是安全的（来自配置文件）
     */
    @Select({
            "<script>",
            "SELECT cb.province_name as province, COUNT(DISTINCT cb.county_code) as count ",
            "FROM county_basic cb ",
            "WHERE cb.county_code IN ",
            "<foreach collection='codes' item='code' open='(' separator=',' close=')'>",
            "  #{code}",
            "</foreach>",
            "GROUP BY cb.province_name ",
            "ORDER BY count DESC",
            "</script>"
    })
    List<Map<String, Object>> getStatisticsByProvince(@Param("codes") List<String> codes);
    
    /**
     * 获取贫困县经济数据（用于摘帽前后对比）
     */
    @Select("SELECT " +
            "  ea.year, " +
            "  ea.gdp_万元 as gdp, " +
            "  ea.gdp_per_capita as gdpPerCapita, " +
            "  ff.fiscal_revenue_万元 as fiscalRevenue, " +
            "  ils.urban_disposable_income as urbanIncome, " +
            "  ils.rural_disposable_income as ruralIncome " +
            "FROM economic_aggregate ea " +
            "LEFT JOIN fiscal_finance ff ON ea.county_code = ff.county_code AND ea.year = ff.year " +
            "LEFT JOIN income_living_standard ils ON ea.county_code = ils.county_code AND ea.year = ils.year " +
            "WHERE ea.county_code = #{countyCode} " +
            "ORDER BY ea.year ASC")
    List<Map<String, Object>> getCountyEconomicData(@Param("countyCode") String countyCode);
    
    /**
     * 获取所有贫困县的经济指标趋势
     */
    @Select({
            "<script>",
            "SELECT ",
            "  ea.year, ",
            "  AVG(ea.gdp_万元) as avgGdp, ",
            "  AVG(ea.gdp_per_capita) as avgGdpPerCapita, ",
            "  AVG(ff.fiscal_revenue_万元) as avgFiscalRevenue, ",
            "  AVG(ils.urban_disposable_income) as avgUrbanIncome, ",
            "  AVG(ils.rural_disposable_income) as avgRuralIncome ",
            "FROM economic_aggregate ea ",
            "LEFT JOIN fiscal_finance ff ON ea.county_code = ff.county_code AND ea.year = ff.year ",
            "LEFT JOIN income_living_standard ils ON ea.county_code = ils.county_code AND ea.year = ils.year ",
            "WHERE ea.county_code IN ",
            "<foreach collection='codes' item='code' open='(' separator=',' close=')'>",
            "  #{code}",
            "</foreach>",
            "GROUP BY ea.year ",
            "ORDER BY ea.year ASC",
            "</script>"
    })
    List<Map<String, Object>> getEconomicTrend(@Param("codes") List<String> codes);
    
    /**
     * 获取贫困县列表（带基础信息）
     */
    @Select({
            "<script>",
            "SELECT cb.county_code as countyCode, cb.county_name as countyName, ",
            "       cb.province_name as provinceName, cb.city_name as cityName ",
            "FROM county_basic cb ",
            "WHERE cb.county_code IN ",
            "<foreach collection='codes' item='code' open='(' separator=',' close=')'>",
            "  #{code}",
            "</foreach>",
            "<if test='province != null and province != \"\"'>",
            "  AND cb.province_name = #{province} ",
            "</if>",
            "ORDER BY cb.province_name, cb.county_name",
            "</script>"
    })
    List<Map<String, Object>> getPovertyCountyList(@Param("codes") List<String> codes,
                                                     @Param("province") String province);
}

