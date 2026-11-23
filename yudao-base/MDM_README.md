# 企业 MDM 多租户平台 — 基于芋道底座的增量开发

> 本仓库不是从零自研，而是**基于芋道 ruoyi-vue-pro 底座做增量开发**：底座已有的能力一律复用，不重复实现。

## 底座信息

- 来源：`https://gitee.com/zhijiantianya/ruoyi-vue-pro`
- 版本：`2026.10-jdk8-SNAPSHOT`（根 pom `<revision>`）
- 克隆commit：`da4b1b4c57e62483ee541683048f76b183b146f1`

## 复用的底座能力（本系统不重复实现）

| 能力 | 底座组件 | 本系统用法 |
|---|---|---|
| 多租户隔离 | `TenantBaseDO` + MyBatis-Plus 租户插件 | 设备 DO 继承 `TenantBaseDO`，租户条件自动注入 |
| 组织/人员/权限 | `yudao-module-system` | 设备绑定用户直接用底座用户体系 |
| Web/鉴权 | `yudao-spring-boot-starter-web`、`-security` | Controller 用 `@PreAuthorize("@ss.hasPermission(...)")` |
| 数据访问 | `yudao-spring-boot-starter-mybatis` | Mapper 继承 `BaseMapperX` |
| 统一返回 | `CommonResult` / `PageResult` / `BeanUtils` | 全部复用 |

## MDM 增量模块

`enterprise-module-mdm`：只实现芋道底座没有的设备管理领域能力。

- `DeviceDO`（设备，继承 `TenantBaseDO`）、`DeviceStatusEnum`（注册/使用/挂失/停用/报废生命周期）
- `DeviceMapper` / `DeviceService` / `DeviceController`：设备注册、更新、删除、分页、状态变更
- SQL：`mdm_device` 表（含 `tenant_id`，唯一键 `(tenant_id, identifier)`）

## 复现步骤

```bash
# 1. 克隆芋道底座
git clone https://gitee.com/zhijiantianya/ruoyi-vue-pro.git
cd ruoyi-vue-pro

# 2. 将本仓库 enterprise-module-mdm 目录拷入底座根目录

# 3. 根 pom.xml <modules> 增加：
#    <module>enterprise-module-mdm</module>
#    yudao-server/pom.xml 增加 enterprise-module-mdm 依赖

# 4. 编译（JDK17 + Maven3.9）
mvn -pl enterprise-module-mdm -am -DskipTests compile

# 5. 执行 SQL：enterprise-module-mdm/src/test/java/.../sql/mdm_device.sql

# 6. 跑单测
mvn -pl enterprise-module-mdm -am -Dtest=DeviceDOTest -Dsurefire.failIfNoSpecifiedTests=false test
```

## 验证状态

- `mvn -pl enterprise-module-mdm -am -DskipTests compile`：通过，11 个 class
- `mvn -pl yudao-server -am -DskipTests compile`：通过（模块已接入应用）
- `DeviceDOTest`：3 个测试全部通过（验证 DO 继承底座租户基类、状态枚举）

关联需求：issue-1795395365458416306
