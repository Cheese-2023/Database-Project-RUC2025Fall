package com.county.risk.service;

/**
 * 风险计算服务接口
 */
public interface RiskCalculationService {

    /**
     * 计算所有县域的风险
     * 
     * @param year 年份
     */
    void calculateAll(Integer year);

    /**
     * 计算所有年份的风险
     */
    void calculateAllYears();

    /**
     * 计算指定县域的风险
     * 
     * @param countyCode 县域代码
     * @param year       年份
     */
    void calculateCounty(String countyCode, Integer year);
}
