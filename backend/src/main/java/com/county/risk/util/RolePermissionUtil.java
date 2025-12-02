package com.county.risk.util;

/**
 * 角色权限工具类
 * 定义各角色的权限范围
 */
public class RolePermissionUtil {

    /**
     * 角色枚举
     */
    public enum Role {
        ADMIN,              // 系统管理员：全部权限
        USER,               // 普通用户：只能看风险监控大屏
        VIP,                // VIP用户：USER权限 + AI助手
        RISK_ANALYST,       // 风险分析员：VIP权限 + 数据增删改 + 风险参数调整 + 全部页面
        DATA_MAINTAINER     // 数据维护员：VIP权限 + 数据增删改 + 全部页面
    }

    /**
     * 检查用户是否可以访问风险监控大屏
     */
    public static boolean canAccessDashboard(String role) {
        return true; // 所有用户都可以访问
    }

    /**
     * 检查用户是否可以使用AI助手
     */
    public static boolean canUseAI(String role) {
        if (role == null) return false;
        return role.equals(Role.VIP.name()) 
            || role.equals(Role.RISK_ANALYST.name())
            || role.equals(Role.DATA_MAINTAINER.name())
            || role.equals(Role.ADMIN.name());
    }

    /**
     * 检查用户是否可以访问风险分析页面
     */
    public static boolean canAccessRiskAnalysis(String role) {
        if (role == null) return false;
        return role.equals(Role.RISK_ANALYST.name())
            || role.equals(Role.DATA_MAINTAINER.name())
            || role.equals(Role.ADMIN.name());
    }

    /**
     * 检查用户是否可以访问预警管理页面
     */
    public static boolean canAccessAlertManage(String role) {
        if (role == null) return false;
        return role.equals(Role.RISK_ANALYST.name())
            || role.equals(Role.DATA_MAINTAINER.name())
            || role.equals(Role.ADMIN.name());
    }

    /**
     * 检查用户是否可以访问数据管理页面
     */
    public static boolean canAccessDataManage(String role) {
        if (role == null) return false;
        return role.equals(Role.RISK_ANALYST.name())
            || role.equals(Role.DATA_MAINTAINER.name())
            || role.equals(Role.ADMIN.name());
    }

    /**
     * 检查用户是否可以访问成果展示页面
     */
    public static boolean canAccessPovertyAchievement(String role) {
        if (role == null) return false;
        return role.equals(Role.RISK_ANALYST.name())
            || role.equals(Role.DATA_MAINTAINER.name())
            || role.equals(Role.ADMIN.name());
    }

    /**
     * 检查用户是否可以进行数据增删改操作
     */
    public static boolean canModifyData(String role) {
        if (role == null) return false;
        return role.equals(Role.RISK_ANALYST.name())
            || role.equals(Role.DATA_MAINTAINER.name())
            || role.equals(Role.ADMIN.name());
    }

    /**
     * 检查用户是否可以执行SQL操作
     */
    public static boolean canExecuteSQL(String role) {
        if (role == null) return false;
        return role.equals(Role.RISK_ANALYST.name())
            || role.equals(Role.DATA_MAINTAINER.name())
            || role.equals(Role.ADMIN.name());
    }

    /**
     * 检查用户是否可以调整风险参数
     */
    public static boolean canAdjustRiskParams(String role) {
        if (role == null) return false;
        return role.equals(Role.RISK_ANALYST.name())
            || role.equals(Role.ADMIN.name());
    }

    /**
     * 检查用户是否具有全部权限（系统管理员）
     */
    public static boolean isAdmin(String role) {
        return role != null && role.equals(Role.ADMIN.name());
    }
}

