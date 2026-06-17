# Self Evaluation

面向工程教育专业认证自评工作的 Java Web 后台管理系统。系统围绕课程达成度评价，支持学校、教师和学生完成基础数据维护、成绩管理、问卷反馈、融合数据分析和课程质量报告生成。

## 功能概览

- 管理员：维护学院、班级、教师、学生、公告和 AI 大模型配置。
- 教师：维护课程，绑定课程学生，上传教学大纲进行融合数据分析，管理成绩，查看课程质量分析和问卷结果，生成课程质量报告。
- 学生：查询个人总评成绩，提交课程调查问卷，查看公告并维护个人信息。
- 数据分析：根据课程目标、成绩组成、考核方式和权重计算课程目标达成度，并提供雷达图、柱状图和散点图展示。
- 文件导入：支持学生名单、课程学生、成绩 Excel 模板下载和导入。
- AI 辅助：可配置大模型接口解析教学大纲中的总评成绩组成、课程目标和考核比例；解析失败时提供本地规则解析兜底。

## 技术栈

- Java 8
- Servlet/JSP
- Spring MVC 3.0.5
- Spring JDBC / MyBatis 3.0.4
- MySQL 5.x/8.x
- Apache Tomcat 9
- jQuery、EasyUI、ECharts、KindEditor

项目是传统 IntelliJ IDEA Java Web 工程，目前没有 Maven/Gradle 构建文件，第三方依赖以 jar 形式放在 `WebRoot/WEB-INF/lib/`。

## 目录结构

```text
.
├── src/                    # Java 源码，包含 controller/service/mapper/bean/util
├── resources/              # Spring、MyBatis、日志和系统配置
├── WebRoot/                # JSP、静态资源、WEB-INF 和依赖 jar
├── 模板/                   # Excel 导入模板
├── 示例/                   # 示例大纲和报告模板
├── selfevaluation.sql      # 数据库初始化脚本
├── reset_database.sql      # 重置业务数据脚本
├── upgrade_*.sql           # 增量升级脚本
├── start-tomcat.sh         # 本地 Tomcat 启动脚本
└── stop-tomcat.sh          # 本地 Tomcat 停止脚本
```

## 本地运行

### 1. 准备环境

- JDK 8
- MySQL
- Tomcat 9
- IntelliJ IDEA 或兼容传统 Web 工程的 Java IDE

如果使用仓库内脚本启动 Tomcat，需要确认 `start-tomcat.sh` 中的 `CATALINA_HOME_DIR` 指向本机 Tomcat 安装目录。

### 2. 初始化数据库

创建数据库并导入初始化脚本：

```sql
CREATE DATABASE selfevaluation DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

```bash
mysql -u root -p selfevaluation < selfevaluation.sql
```

如需在已有库上应用增量更新，可按时间和业务需要执行：

```bash
mysql -u root -p selfevaluation < upgrade_fusion_analysis.sql
mysql -u root -p selfevaluation < upgrade_system_statistics.sql
```

如需恢复到初始业务数据状态：

```bash
mysql -u root -p selfevaluation < reset_database.sql
```

### 3. 配置数据库连接

复制示例配置：

```bash
cp resources/connection.properties.example resources/connection.properties
```

按本机环境修改：

```properties
jdbc.driverClassName=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/selfevaluation?useUnicode=true&characterEncoding=UTF-8
jdbc.username=your_mysql_user
jdbc.password=your_mysql_password
```

`resources/connection.properties` 包含本地凭据，已被 `.gitignore` 忽略，不应提交到仓库。

### 4. 启动应用

使用 IDE 部署时：

1. 以 `selfevaluation.iml` 导入 IntelliJ IDEA。
2. 将 `src` 标记为源码目录，`resources` 标记为资源目录。
3. 配置 Tomcat Artifact，Web 根目录为 `WebRoot`。
4. 启动后访问 `http://localhost:8080/selfevaluation/` 或 IDE 中配置的上下文路径。

使用脚本启动时：

```bash
chmod +x start-tomcat.sh stop-tomcat.sh
./start-tomcat.sh
```

停止服务：

```bash
./stop-tomcat.sh
```

## 默认账号

初始化 SQL 会创建一个管理员账号：

```text
用户名：admin
密码：123456
```

首次部署后请立即修改默认密码。

## 配置说明

- `resources/SystemConfig.properties`：上传根目录，默认 `runtime/upload/`。
- `resources/log4j.xml`：日志输出到 `runtime/logs/`。
- AI 大模型配置通过系统后台维护，数据库表为 `ai_model`。API Key 属于敏感信息，不应写入代码或 SQL 示例。

## 开发说明

- 代码包名为 `com.mywork`。
- Controller 位于 `src/com/mywork/controller`。
- Service 位于 `src/com/mywork/service` 和 `src/com/mywork/service/impl`。
- MyBatis Mapper 接口位于 `src/com/mywork/mapper`，XML 位于 `resources/mybatis`。
- JSP 页面位于 `WebRoot/WEB-INF/views`。

本项目仍保留较多传统 Java Web 结构。后续如需现代化，建议优先补充 Maven/Gradle 构建、统一依赖版本、引入自动化测试，并将明文密码迁移为加盐哈希。

## Git 忽略策略

仓库不会提交以下内容：

- `runtime/` 下的日志、上传文件和 UI 审计截图。
- `out/`、`target/`、`build/` 等编译产物。
- 本机数据库配置 `resources/connection.properties`。
- IDE 本地状态文件，如 `.idea/workspace.xml`。

## License

本项目代码以 MIT License 发布。第三方库、前端组件和模板文件仍遵循其各自许可证。
