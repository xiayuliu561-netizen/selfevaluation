# Self Evaluation

Self Evaluation 是一个面向工程教育专业认证和课程质量持续改进场景的 Java Web 管理系统。系统以课程、学生、成绩、课程目标和达成度分析为核心，帮助管理员、教师和学生完成课程评价数据维护、成绩导入、问卷反馈、课程目标达成度计算以及课程质量报告生成。

本项目是传统 Servlet/JSP 架构的 Web 应用，适合用于课程设计、毕业设计、教学管理系统原型、工程教育认证辅助系统等场景。

## 项目背景

工程教育专业认证通常要求课程能够说明教学目标、考核方式、成绩构成、课程目标达成情况以及持续改进依据。手工维护这些数据容易出现以下问题：

- 课程学生名单、成绩表和课程目标数据分散在多个文件中。
- 总评成绩组成、课程目标考核比例和达成度计算过程容易重复录入。
- 教师需要手工整理课程质量分析报告，工作量较大。
- 管理员、教师、学生之间的数据边界和操作入口需要统一管理。

Self Evaluation 将这些流程集中到一个后台系统中，通过数据库、Excel 导入、教学大纲解析和可视化分析，形成相对完整的课程质量评价工作流。

## 核心功能

### 管理员端

- 学院管理：维护学院基础信息。
- 班级管理：维护班级和所属学院关系。
- 教师管理：新增、修改、删除、查询教师账号。
- 学生管理：新增、修改、删除、查询学生账号，支持 Excel 导入学生数据。
- 公告管理：发布和维护系统公告。
- AI 大模型管理：配置用于教学大纲解析和报告辅助生成的大模型接口。
- 系统统计：查看系统中学院、班级、课程、教师、学生等基础数据统计。

### 教师端

- 课程管理：维护课程信息。
- 课程学生管理：为课程绑定学生，支持按学院、班级、学生筛选。
- 融合数据分析：上传教学大纲，解析总评成绩组成、课程目标和考核方式比例。
- 成绩管理：下载课程成绩模板，导入 Excel 成绩，手动维护成绩并计算总评。
- 课程质量分析：基于课程目标和成绩明细计算课程目标达成度。
- 课程质量分析图表：以雷达图、柱状图、散点图展示课程目标达成情况。
- 问卷结果查看：查看学生对课程的反馈和问卷统计。
- 课程质量报告：生成、维护和下载课程质量分析报告。

### 学生端

- 查看公告。
- 查询个人课程成绩。
- 提交课程调查问卷。
- 维护个人信息和修改密码。

## 特色能力

### 教学大纲解析

系统支持上传 `doc`、`docx`、`pdf`、`txt` 等格式的教学大纲文件，并尝试解析以下内容：

- 总评成绩组成，例如平时成绩、实验成绩、期末考试等。
- 各成绩组成在总评成绩中的占比。
- 课程目标。
- 课程目标与考核方式之间的比例或系数关系。

解析流程支持两种方式：

- 调用管理员配置的 AI 大模型接口进行结构化提取。
- 当 AI 调用失败、超时或返回内容不符合要求时，使用本地规则解析作为兜底。

解析结果会先在前端展示，教师确认后才保存到数据库。

### 动态成绩模板

成绩模板不是固定字段，而是根据课程融合数据分析中保存的成绩组成动态生成。教师可以为不同课程下载不同列结构的 Excel 模板，再将填写后的成绩表导入系统。

### 课程目标达成度计算

系统会基于课程目标、考核方式、比例系数和学生成绩明细计算：

- 每个课程目标的满分。
- 每个课程目标的实际得分。
- 每名学生在各课程目标下的达成度。
- 课程整体目标达成情况。

这些结果可用于课程质量分析和报告生成。

### 可视化分析

课程质量分析图表页面提供多种图表展示方式：

- 雷达图：展示不同课程目标的整体达成水平。
- 柱状图：比较各课程目标达成度差异。
- 散点图：辅助观察学生成绩和达成度分布。

## 技术栈

- Java 8
- Servlet 2.5 / JSP
- Spring MVC 3.0.5
- Spring JDBC
- MyBatis 3.0.4
- MySQL
- Apache Tomcat 9
- jQuery
- Bootstrap
- ECharts
- KindEditor
- Apache POI

项目没有使用 Maven 或 Gradle，第三方依赖以 jar 包形式放在 `WebRoot/WEB-INF/lib/` 中。

## 目录结构

```text
.
├── src/
│   └── com/mywork/
│       ├── bean/             # 实体类
│       ├── common/           # 通用对象
│       ├── controller/       # Spring MVC Controller
│       ├── inteceptor/       # 登录会话拦截器
│       ├── mapper/           # MyBatis Mapper 接口
│       ├── service/          # Service 接口
│       ├── service/impl/     # Service 实现
│       └── util/             # Excel、JSON、解析、计算等工具类
├── resources/
│   ├── mybatis/              # MyBatis XML 映射文件
│   ├── app-context.xml       # Spring 应用上下文
│   ├── servlet-context.xml   # Spring MVC 配置
│   ├── mybatis-config.xml    # MyBatis 配置
│   ├── log4j.xml             # 日志配置
│   └── SystemConfig.properties
├── WebRoot/
│   ├── WEB-INF/
│   │   ├── views/            # JSP 页面
│   │   ├── lib/              # 第三方 jar 依赖
│   │   └── web.xml           # Web 应用配置
│   └── resources/            # 前端 CSS、JS、图片和组件
├── 模板/                     # Excel 导入模板
├── 示例/                     # 教学大纲和报告示例
├── selfevaluation.sql        # 数据库初始化脚本
├── reset_database.sql        # 数据重置脚本
├── upgrade_fusion_analysis.sql
├── upgrade_system_statistics.sql
├── start-tomcat.sh
└── stop-tomcat.sh
```

## 环境要求

建议环境：

- JDK 8
- MySQL 5.7 或 8.x
- Apache Tomcat 9
- IntelliJ IDEA

项目依赖旧版本 Spring、MyBatis 和 MySQL Driver，建议优先使用 JDK 8 运行。如果使用更高版本 JDK，可能需要额外处理旧依赖兼容性问题。

## 数据库初始化

创建数据库：

```sql
CREATE DATABASE selfevaluation DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

导入初始化脚本：

```bash
mysql -u root -p selfevaluation < selfevaluation.sql
```

如果需要应用增量更新：

```bash
mysql -u root -p selfevaluation < upgrade_fusion_analysis.sql
mysql -u root -p selfevaluation < upgrade_system_statistics.sql
```

如果需要将业务数据恢复到初始状态：

```bash
mysql -u root -p selfevaluation < reset_database.sql
```

## 本地配置

复制数据库配置模板：

```bash
cp resources/connection.properties.example resources/connection.properties
```

修改 `resources/connection.properties`：

```properties
jdbc.driverClassName=com.mysql.jdbc.Driver
jdbc.url=jdbc:mysql://localhost:3306/selfevaluation?useUnicode=true&characterEncoding=UTF-8
jdbc.username=your_mysql_user
jdbc.password=your_mysql_password
```

上传目录配置位于 `resources/SystemConfig.properties`：

```properties
uploadRoot =runtime/upload/
```

日志配置位于 `resources/log4j.xml`，默认输出到 `runtime/logs/`。

## 运行方式

### 使用 IntelliJ IDEA

1. 使用 IntelliJ IDEA 打开项目目录。
2. 确认项目 SDK 为 JDK 8。
3. 确认 `src` 为源码目录，`resources` 为资源目录。
4. 配置 Tomcat Server。
5. 将 Web 根目录配置为 `WebRoot`。
6. 部署 Artifact 后启动 Tomcat。
7. 访问本地应用地址，例如 `http://localhost:8080/selfevaluation/`。

### 使用脚本启动

项目提供了本地 Tomcat 启停脚本：

```bash
chmod +x start-tomcat.sh stop-tomcat.sh
./start-tomcat.sh
```

停止服务：

```bash
./stop-tomcat.sh
```

使用脚本前需要确认 `start-tomcat.sh` 中的 `CATALINA_HOME_DIR` 指向本机 Tomcat 安装目录。

## 默认账号

初始化 SQL 中包含一个管理员账号：

```text
用户名：admin
密码：123456
```

首次部署后请立即登录系统并修改默认密码。

## Excel 模板

仓库中包含两个常用模板：

- `模板/学生导入模板.xls`
- `模板/成绩导入模板.xls`

学生导入模板用于管理员批量导入学生基础信息。成绩导入模板可作为基础示例，实际课程成绩模板会根据课程的成绩组成动态生成。

## 示例文件

`示例/` 目录中包含教学大纲和课程质量报告模板示例，可用于验证大纲解析和报告生成相关流程。

## AI 大模型配置

系统支持在管理员端配置大模型接口，包括供应商、模型名称、接口地址和 API Key。配置后，教师端融合数据分析和报告生成可以使用该模型能力。

注意事项：

- 同一时间只建议启用一个默认模型。
- API Key 属于敏感信息，不应写入代码或提交到仓库。
- AI 解析结果需要教师确认后再入库。
- AI 返回异常时，系统会尝试使用本地规则解析兜底。

## 已知限制

- 当前项目仍为传统 Java Web 工程，没有 Maven/Gradle 构建文件。
- 部分依赖版本较旧，生产环境使用前建议升级依赖并补充安全扫描。
- 初始化 SQL 中的默认账号使用明文密码，仅适合本地演示或课程项目。
- 用户密码存储逻辑需要进一步升级为加盐哈希。
- 缺少自动化测试，需要后续补充单元测试和端到端测试。

## 后续改进方向

- 引入 Maven 或 Gradle 统一管理依赖。
- 将 JSP 页面逐步拆分为更清晰的前端模块。
- 增加自动化测试和 CI。
- 升级 Spring、MyBatis、MySQL Driver 等核心依赖。
- 加强权限校验、密码加密、审计日志和敏感数据保护。
- 将 AI 调用配置迁移到更安全的密钥管理方式。

## License

本项目代码以 MIT License 发布。第三方依赖、前端组件、示例模板和字体图标等资源遵循其各自许可证。
