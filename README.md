# 员工信息管理系统

基于 Spring Boot 3 + Vue 3 的员工信息管理系统，软件技术专业课程实训项目。系统设计、后端、前端与数据库均由本人独立完成。

## 功能

- **用户认证**：注册 / 登录 / 注销，Spring Security 认证，BCrypt 密码加密存储，登录图形验证码，Redis 集中管理会话（服务重启登录态不丢失）
- **员工管理**：员工信息的新增、编辑、删除、批量删除
- **查询**：多条件分页查询
- **统计与导出**：员工数据统计接口、员工信息表导出（CSV，带 BOM，Excel 直接打开不乱码）

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2、Spring Security、MyBatis-Plus 3.5.5、Spring Session + Redis、MySQL 8、Lombok |
| 前端 | Vue 3、Element Plus、Axios |

## 项目结构

```
src/main/java/com/dsf/
├── config/        # Spring Security、MyBatis-Plus、全局异常等配置
├── controller/    # 用户、员工、验证码接口
├── entity/        # User / Staff 实体
├── mapper/        # MyBatis-Plus Mapper
├── service/       # 业务逻辑（含 UserDetailsService 实现）
└── util/          # 统一返回结果、验证码工具
src/main/resources/static/   # Vue 3 + Element Plus 前端页面
sql/staff.sql                # 数据库建表脚本
```

## 快速开始

1. **环境**：JDK 17+、MySQL 8.x、Redis、Maven
2. **建库**：创建数据库 `staff`，导入 `sql/staff.sql`
3. **配置**：修改 `src/main/resources/application.yml` 中的 MySQL 密码（Redis 默认 localhost:6379）
4. **启动**：运行 `Staff6Application`，默认端口 8081
5. **访问**：浏览器打开 <http://localhost:8081/login.html>
