# 县域风险预警系统 (County Risk Warning System)

数据库系统大作业 2025Fall

## 1. 环境准备 (Prerequisites)

在开始之前，请确保您的开发环境已安装以下软件：

- **Java Development Kit (JDK)**: 17 或更高版本
- **Node.js**: v16 或更高版本 (推荐 v18 LTS)
- **MySQL**: 8.0 或更高版本
- **Maven**: 3.6 或更高版本 (用于后端构建)
- **Python**: 3.8+ (用于数据生成脚本)

## 2. 数据库设置与数据导入

### 2.1 创建数据库

登录 MySQL 并创建数据库：

```sql
CREATE DATABASE county_risk_warning_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2.2 导入基础表结构

将项目 `database` 目录下的 SQL 脚本导入数据库：

```bash
# 假设您在项目根目录
mysql -u root -p county_risk_warning_system < database/schema.sql
```

### 2.3 生成与导入数据
由于数据文件过大，故需要在群里下载并放置在data文件夹下；

由于真实数据不足，提供了一套 Python 脚本来生成模拟数据以供测试。

1.  **安装 Python 依赖**：
    进入 `data-scripts` 目录并安装依赖：
    ```bash
    cd data-scripts
    pip install -r requirements.txt
    # 如果没有 requirements.txt，请手动安装 mysql-connector-python
    pip install mysql-connector-python
    ```

2.  **配置数据库连接**：
    打开 `data-scripts` 目录下的脚本（如 `import_full_history.py`），确保 `DB_CONFIG` 中的数据库用户名和密码正确：
    ```python
    DB_CONFIG = {
        'host': 'localhost',
        'user': 'root',       # 修改为您的用户名
        'password': 'your_password', # 修改为您的密码
        'database': 'county_risk_warning_system',
        # ...
    }
    ```

3.  **运行数据生成脚本**：
    *   **导入全量历史数据**
        导入 2000-2023 年的所有数据，包含真实数据和模拟补全数据。仅导入数据，不自动触发计算。
        ```bash
        python3 import_full_history.py
        ```

## 3. 后端启动 (Backend)

1.  **配置数据库**：
    打开 `backend/src/main/resources/application.yml`，修改数据库连接信息：
    ```yaml
    spring:
      datasource:
        url: jdbc:mysql://localhost:3306/county_risk_warning_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
        username: your_username  # 您的MySQL用户名
        password: your_password  # 您的MySQL密码
    ```

2.  **启动服务**：
    进入 `backend` 目录并运行：
    ```bash
    cd backend
    mvn spring-boot:run
    ```
    服务启动后将监听 `http://localhost:8080`。

## 4. 前端启动 (Frontend)

1.  **安装依赖**：
    进入 `frontend` 目录：
    ```bash
    cd frontend
    npm install
    ```

2.  **启动开发服务器**：
    ```bash
    npm run dev
    ```
    启动后，访问终端显示的地址（通常是 `http://localhost:5173`  或者 `http://localhost:3000`）即可使用系统。

## 5. 系统使用

- **风险计算**：系统启动后，首次使用建议在"数据管理"页面点击"立即重新计算风险"，以基于最新数据生成风险评估结果。（时间可能会比较久）
- **预警检查**：在"预警管理"页面可以查看自动生成的风险预警信息。

## 6. 如何修改风险评估标准？
 1. backend/src/main/java/com/county/risk/service/impl/RiskCalculationServiceImpl.java  第323 - 339行 修改判断风险等级阈值
 2. backend/src/main/java/com/county/risk/service/impl/RiskIndicatorServiceImpl.java 修改公式默认指标权重

## 7.有什么问题？
- **数据缺失**：数据缺失，自动生成的数据过于统一，无法展示特性。


# 系统模块功能介绍

本项目包含四个核心功能模块，旨在提供全方位的县域金融风险监测与预警服务。

## 1. 风险监控大屏 (Dashboard)
**定位**：系统的“驾驶舱”，提供全局视角的风险概览。
*   **核心功能**：
    *   **关键指标统计**：实时展示监测县域总数及各风险等级（高/中/低）的县域数量。
    *   **可视化图表**：
        *   **风险等级分布**：通过饼图直观展示全国县域的风险构成比例。
        *   **风险趋势分析**：通过折线图展示历年平均风险分数的演变趋势。
    *   **最新预警**：滚动展示最新的高风险县域预警信息。

## 2. 风险分析 (Risk Analysis)
**定位**：深度分析工具，支持多维度的风险数据查询与筛选。
*   **核心功能**：
    *   **多维筛选**：支持按**年份**、**省份**、**风险等级**组合查询特定县域。
    *   **详细数据列表**：展示县域的综合风险评分、具体排名及各项分指标得分。
    *   **趋势洞察**：帮助分析师快速定位特定区域或特定类型的风险聚集情况。

## 3. 预警管理 (Alert Management)
**定位**：风险事件的处理与跟踪中心。
*   **核心功能**：
    *   **预警列表**：自动捕获并列出所有触发阈值的风险事件。
    *   **状态跟踪**：支持对预警进行状态管理（如：待处理、处理中、已解决）。
    *   **详情查看**：提供预警触发的具体原因、相关指标数值及时间点。

## 4. 数据管理 (Data Management)
**定位**：系统的“后台控制台”，负责数据维护与模型配置。
*   **核心功能**：
    *   **风险计算触发**：提供“立即重新计算风险”按钮，用于在导入新数据或修改模型后更新全量结果。
    *   **指标配置**：支持动态调整风险评估模型：
        *   **权重调整**：修改经济、社会、环境等各维度的权重。
        *   **阈值设定**：自定义高/中/低风险的判定阈值。
    *   **配置重置**：一键恢复系统默认的专家模型配置。
