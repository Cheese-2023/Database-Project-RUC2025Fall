# 县域风险预警系统 (County Risk Warning System)

本项目是一个县域风险监测与预警系统，旨在通过多维度数据分析（经济、社会、环境、治理等），对县域发展风险进行实时监测、评估和预警。

## 1. 环境准备 (Prerequisites)

在开始之前，请确保您的开发环境已安装以下软件：

- **Java Development Kit (JDK)**: 17 或更高版本
  - [下载 JDK](https://www.oracle.com/java/technologies/downloads/)
- **Node.js**: v16 或更高版本 (推荐 v18 LTS)
  - [下载 Node.js](https://nodejs.org/)
- **MySQL**: 8.0 或更高版本
  - [下载 MySQL](https://dev.mysql.com/downloads/mysql/)
- **Maven**: 3.6 或更高版本 (用于后端构建)
  - [下载 Maven](https://maven.apache.org/download.cgi)
- **Python**: 3.8+ (用于数据生成脚本)
  - [下载 Python](https://www.python.org/downloads/)

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

由于真实数据可能不足，本项目提供了一套 Python 脚本来生成模拟数据以供测试。

1.  **安装 Python 依赖**：
    进入 `data-scripts` 目录并安装依赖：
    ```bash
    cd data-scripts
    pip install -r requirements.txt
    # 如果没有 requirements.txt，请手动安装 mysql-connector-python
    pip install mysql-connector-python
    ```

2.  **配置数据库连接**：
    打开 `data-scripts` 目录下的脚本（如 `generate_more_data.py`），确保 `DB_CONFIG` 中的数据库用户名和密码正确：
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
    按顺序运行以下脚本以生成完整数据：

    ```bash
    # 1. 生成基础测试数据
    python3 generate_test_data.py

    # 2. 导入财政和环境数据
    python3 import_fiscal_environment.py

    # 3. 补充教育、健康及投资消费数据
    python3 generate_more_data.py

    # 4. (可选) 重新生成更合理的财政数据（修复债务率过高问题）
    python3 regenerate_fiscal_data.py
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
    启动后，访问终端显示的地址（通常是 `http://localhost:5173`）即可使用系统。

## 5. 系统使用

- **风险计算**：系统启动后，首次使用建议在"数据管理"页面点击"立即重新计算风险"，以基于最新数据生成风险评估结果。
- **预警检查**：在"预警管理"页面可以查看自动生成的风险预警信息。
