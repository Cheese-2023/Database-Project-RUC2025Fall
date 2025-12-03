import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 直接通过数据库查询触发风险计算的辅助脚本
 * 这个脚本会检查哪些年份需要计算，然后输出SQL语句
 * 
 * 使用方法：
 * 1. 编译：javac CalculateRisk.java
 * 2. 运行：java CalculateRisk
 * 3. 或者直接查看输出的SQL，手动执行
 */
public class CalculateRisk {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/county_risk_warning_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "20050210Wbz";
    
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            
            // 获取所有有基础数据的年份
            List<Integer> yearsWithData = new ArrayList<>();
            PreparedStatement stmt1 = conn.prepareStatement("SELECT DISTINCT year FROM economic_aggregate ORDER BY year");
            ResultSet rs1 = stmt1.executeQuery();
            while (rs1.next()) {
                yearsWithData.add(rs1.getInt("year"));
            }
            rs1.close();
            stmt1.close();
            
            // 获取所有已有风险评估的年份
            List<Integer> yearsWithAssessment = new ArrayList<>();
            PreparedStatement stmt2 = conn.prepareStatement("SELECT DISTINCT year FROM comprehensive_risk_assessment ORDER BY year");
            ResultSet rs2 = stmt2.executeQuery();
            while (rs2.next()) {
                yearsWithAssessment.add(rs2.getInt("year"));
            }
            rs2.close();
            stmt2.close();
            
            // 找出缺失的年份
            List<Integer> missingYears = new ArrayList<>();
            for (Integer year : yearsWithData) {
                if (!yearsWithAssessment.contains(year)) {
                    missingYears.add(year);
                }
            }
            
            System.out.println("=".repeat(60));
            System.out.println("风险计算年份检查");
            System.out.println("=".repeat(60));
            System.out.println("有基础数据的年份范围: " + yearsWithData.get(0) + " - " + yearsWithData.get(yearsWithData.size() - 1));
            System.out.println("已有风险评估的年份范围: " + 
                (yearsWithAssessment.isEmpty() ? "无" : (yearsWithAssessment.get(0) + " - " + yearsWithAssessment.get(yearsWithAssessment.size() - 1))));
            System.out.println("缺失的年份数量: " + missingYears.size());
            
            if (!missingYears.isEmpty()) {
                System.out.println("\n缺失的年份: " + missingYears);
                System.out.println("\n请在前端'数据管理'页面点击'立即重新计算风险'，");
                System.out.println("或者通过API调用: POST http://localhost:8080/api/risk/indicators/calculate");
                System.out.println("Header: role: ADMIN");
            } else {
                System.out.println("\n✓ 所有年份的风险评估已完成！");
            }
            
            conn.close();
            
        } catch (Exception e) {
            System.err.println("错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

