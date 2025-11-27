package com.county.risk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 县域风险预警系统启动类
 */
@SpringBootApplication
@MapperScan("com.county.risk.mapper")
public class CountyRiskWarningApplication {
    public static void main(String[] args) {
        SpringApplication.run(CountyRiskWarningApplication.class, args);
        System.out.println("==================================");
        System.out.println("县域风险预警系统启动成功！");
        System.out.println("API文档地址: http://localhost:8080/api/doc.html");
        System.out.println("==================================");
    }
}
