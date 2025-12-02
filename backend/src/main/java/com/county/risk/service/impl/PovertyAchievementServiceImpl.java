package com.county.risk.service.impl;

import com.county.risk.mapper.PovertyAchievementMapper;
import com.county.risk.service.PovertyAchievementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 脱贫攻坚成果展示服务实现类
 */
@Slf4j
@Service
public class PovertyAchievementServiceImpl implements PovertyAchievementService {
    
    @Autowired
    private PovertyAchievementMapper povertyAchievementMapper;
    
    // 贫困县代码列表（从文件中读取或硬编码）
    private List<String> povertyCountyCodes = null;
    
    // 贫困县摘帽年份映射（简化版，实际应该从数据库读取）
    private Map<String, Integer> delistingYearMap = new HashMap<>();
    
    /**
     * 初始化贫困县代码列表
     */
    private List<String> getPovertyCountyCodes() {
        if (povertyCountyCodes == null) {
            povertyCountyCodes = new ArrayList<>();
            try {
                // 尝试从classpath读取文件
                ClassPathResource resource = new ClassPathResource("poverty_county_codes.txt");
                if (resource.exists()) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (!line.isEmpty()) {
                                povertyCountyCodes.add(line);
                            }
                        }
                    }
                } else {
                    // 如果文件不存在，使用硬编码的代码列表（前100个作为示例）
                    // 实际使用时应该从数据库或配置文件中读取完整列表
                    log.warn("贫困县代码文件不存在，使用默认列表");
                    povertyCountyCodes = Arrays.asList(
                            "130125", "130126", "130129", "130131", "130321", "130425", "130434",
                            "130522", "130529", "130530", "130531", "130532", "130533", "130623",
                            "130624", "130627", "130630", "130631", "130633", "130634", "130636"
                            // ... 更多代码应该从数据库或文件中读取
                    );
                }
                log.info("加载了 {} 个贫困县代码", povertyCountyCodes.size());
            } catch (Exception e) {
                log.error("读取贫困县代码文件失败", e);
                povertyCountyCodes = new ArrayList<>();
            }
        }
        return povertyCountyCodes;
    }
    
    /**
     * 获取贫困县代码列表
     */
    private List<String> getPovertyCountyCodesList() {
        return getPovertyCountyCodes();
    }
    
    @Override
    public Map<String, Object> getOverview() {
        List<String> codes = getPovertyCountyCodes();
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalCount", codes.size());
        overview.put("dataYearRange", "2000-2023");
        
        // 按摘帽年份统计
        Map<Integer, Long> delistingStats = new HashMap<>();
        // 这里应该从数据库读取实际的摘帽年份，暂时使用模拟数据
        // 实际应该查询数据库中的摘帽年份信息
        overview.put("delistingYearStats", delistingStats);
        
        return overview;
    }
    
    @Override
    public List<Map<String, Object>> getStatisticsByProvince() {
        List<String> codes = getPovertyCountyCodesList();
        return povertyAchievementMapper.getStatisticsByProvince(codes);
    }
    
    @Override
    public List<Map<String, Object>> getStatisticsByDelistingYear() {
        // 这里应该从数据库查询实际的摘帽年份统计
        // 暂时返回模拟数据
        List<Map<String, Object>> stats = new ArrayList<>();
        Map<String, Object> stat2016 = new HashMap<>();
        stat2016.put("year", 2016);
        stat2016.put("count", 3);
        stats.add(stat2016);
        
        Map<String, Object> stat2017 = new HashMap<>();
        stat2017.put("year", 2017);
        stat2017.put("count", 11);
        stats.add(stat2017);
        
        Map<String, Object> stat2018 = new HashMap<>();
        stat2018.put("year", 2018);
        stat2018.put("count", 282);
        stats.add(stat2018);
        
        Map<String, Object> stat2019 = new HashMap<>();
        stat2019.put("year", 2019);
        stat2019.put("count", 339);
        stats.add(stat2019);
        
        Map<String, Object> stat2020 = new HashMap<>();
        stat2020.put("year", 2020);
        stat2020.put("count", 52);
        stats.add(stat2020);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getDelistingComparison(Integer delistingYear) {
        List<String> codes = getPovertyCountyCodesList();
        List<Map<String, Object>> trendData = povertyAchievementMapper.getEconomicTrend(codes);
        
        Map<String, Object> comparison = new HashMap<>();
        
        // 计算摘帽前后的平均值
        // 这里简化处理，实际应该根据每个县的摘帽年份来计算
        List<Map<String, Object>> beforeData = new ArrayList<>();
        List<Map<String, Object>> afterData = new ArrayList<>();
        
        for (Map<String, Object> data : trendData) {
            Integer year = extractYear(data.get("year"));
            if (year != null) {
                if (delistingYear != null) {
                    if (year < delistingYear) {
                        beforeData.add(data);
                    } else if (year > delistingYear) {
                        afterData.add(data);
                    }
                } else {
                    // 如果没有指定年份，使用2018作为分界点（大部分县在2018-2019年摘帽）
                    if (year < 2018) {
                        beforeData.add(data);
                    } else if (year > 2018) {
                        afterData.add(data);
                    }
                }
            }
        }
        
        // 计算平均值
        Map<String, Object> beforeAvg = calculateAverage(beforeData);
        Map<String, Object> afterAvg = calculateAverage(afterData);
        
        comparison.put("before", beforeAvg);
        comparison.put("after", afterAvg);
        
        // 计算增长率
        Map<String, Object> growth = new HashMap<>();
        if (beforeAvg.containsKey("avgGdp") && afterAvg.containsKey("avgGdp")) {
            Double beforeGdp = ((Number) beforeAvg.get("avgGdp")).doubleValue();
            Double afterGdp = ((Number) afterAvg.get("avgGdp")).doubleValue();
            if (beforeGdp > 0) {
                growth.put("gdpGrowth", ((afterGdp - beforeGdp) / beforeGdp) * 100);
            }
        }
        if (beforeAvg.containsKey("avgGdpPerCapita") && afterAvg.containsKey("avgGdpPerCapita")) {
            Double before = ((Number) beforeAvg.get("avgGdpPerCapita")).doubleValue();
            Double after = ((Number) afterAvg.get("avgGdpPerCapita")).doubleValue();
            if (before > 0) {
                growth.put("gdpPerCapitaGrowth", ((after - before) / before) * 100);
            }
        }
        if (beforeAvg.containsKey("avgFiscalRevenue") && afterAvg.containsKey("avgFiscalRevenue")) {
            Double before = ((Number) beforeAvg.get("avgFiscalRevenue")).doubleValue();
            Double after = ((Number) afterAvg.get("avgFiscalRevenue")).doubleValue();
            if (before > 0) {
                growth.put("fiscalRevenueGrowth", ((after - before) / before) * 100);
            }
        }
        if (beforeAvg.containsKey("avgUrbanIncome") && afterAvg.containsKey("avgUrbanIncome")) {
            Double before = ((Number) beforeAvg.get("avgUrbanIncome")).doubleValue();
            Double after = ((Number) afterAvg.get("avgUrbanIncome")).doubleValue();
            if (before > 0) {
                growth.put("urbanIncomeGrowth", ((after - before) / before) * 100);
            }
        }
        if (beforeAvg.containsKey("avgRuralIncome") && afterAvg.containsKey("avgRuralIncome")) {
            Double before = ((Number) beforeAvg.get("avgRuralIncome")).doubleValue();
            Double after = ((Number) afterAvg.get("avgRuralIncome")).doubleValue();
            if (before > 0) {
                growth.put("ruralIncomeGrowth", ((after - before) / before) * 100);
            }
        }
        
        comparison.put("growth", growth);
        
        return comparison;
    }
    
    private Map<String, Object> calculateAverage(List<Map<String, Object>> dataList) {
        Map<String, Object> avg = new HashMap<>();
        if (dataList.isEmpty()) {
            return avg;
        }
        
        double sumGdp = 0, sumGdpPerCapita = 0, sumFiscalRevenue = 0;
        double sumUrbanIncome = 0, sumRuralIncome = 0;
        int count = 0;
        
        for (Map<String, Object> data : dataList) {
            if (data.get("avgGdp") != null) {
                sumGdp += ((Number) data.get("avgGdp")).doubleValue();
            }
            if (data.get("avgGdpPerCapita") != null) {
                sumGdpPerCapita += ((Number) data.get("avgGdpPerCapita")).doubleValue();
            }
            if (data.get("avgFiscalRevenue") != null) {
                sumFiscalRevenue += ((Number) data.get("avgFiscalRevenue")).doubleValue();
            }
            if (data.get("avgUrbanIncome") != null) {
                sumUrbanIncome += ((Number) data.get("avgUrbanIncome")).doubleValue();
            }
            if (data.get("avgRuralIncome") != null) {
                sumRuralIncome += ((Number) data.get("avgRuralIncome")).doubleValue();
            }
            count++;
        }
        
        if (count > 0) {
            avg.put("avgGdp", sumGdp / count);
            avg.put("avgGdpPerCapita", sumGdpPerCapita / count);
            avg.put("avgFiscalRevenue", sumFiscalRevenue / count);
            avg.put("avgUrbanIncome", sumUrbanIncome / count);
            avg.put("avgRuralIncome", sumRuralIncome / count);
        }
        
        return avg;
    }
    
    @Override
    public List<Map<String, Object>> getPovertyCountyList(String province, Integer delistingYear) {
        List<String> codes = getPovertyCountyCodesList();
        return povertyAchievementMapper.getPovertyCountyList(codes, province);
    }
    
    @Override
    public Map<String, Object> getCountyDetail(String countyCode) {
        List<Map<String, Object>> economicData = povertyAchievementMapper.getCountyEconomicData(countyCode);
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("countyCode", countyCode);
        detail.put("economicData", economicData);
        detail.put("delistingYear", delistingYearMap.getOrDefault(countyCode, null));
        
        return detail;
    }
    
    @Override
    public Map<String, Object> getEconomicTrend(String indicatorType) {
        List<String> codes = getPovertyCountyCodesList();
        List<Map<String, Object>> trend = povertyAchievementMapper.getEconomicTrend(codes);
        
        Map<String, Object> result = new HashMap<>();
        result.put("trend", trend);
        result.put("indicatorType", indicatorType);
        
        return result;
    }
    
    /**
     * 安全地从对象中提取年份（支持Integer、Date、Year等类型）
     */
    private Integer extractYear(Object yearObj) {
        if (yearObj == null) {
            return null;
        }
        
        if (yearObj instanceof Integer) {
            return (Integer) yearObj;
        }
        
        if (yearObj instanceof Number) {
            return ((Number) yearObj).intValue();
        }
        
        if (yearObj instanceof java.sql.Date) {
            java.sql.Date date = (java.sql.Date) yearObj;
            return date.toLocalDate().getYear();
        }
        
        if (yearObj instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) yearObj;
            return date.getYear() + 1900; // Date.getYear()返回的是年份-1900
        }
        
        if (yearObj instanceof java.time.Year) {
            return ((java.time.Year) yearObj).getValue();
        }
        
        // 尝试转换为字符串再解析
        try {
            String yearStr = yearObj.toString();
            return Integer.parseInt(yearStr);
        } catch (Exception e) {
            log.warn("无法解析年份: {}", yearObj, e);
            return null;
        }
    }
}

