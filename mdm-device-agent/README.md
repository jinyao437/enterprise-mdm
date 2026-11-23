# 企业 MDM 原生 Android Device Agent

> MDM 设备管控的原生层。uni-app 工作台负责业务 UI，本 agent 负责系统级设备管控（uni-app 无法直接调用）。

## 为什么必须原生

设备管控依赖 Android 系统级 API，uni-app 的 JS 层碰不到：Device Owner 激活、远程擦除/锁屏、静默安装、密码策略、禁用相机、后台常驻心跳等。本工程是独立 Android 应用，与工作台通过本地广播通信。

## 工程结构

- `owner/`：Device Owner 激活（MdmDeviceAdminReceiver / DeviceOwnerManager / EnrollmentActivity）
- `policy/`：策略执行（PolicyExecutor 按类型调原生能力；PolicyJsonParser 纯 Java 解析）
- `heartbeat/`：心跳上报（HeartbeatWorker + BootReceiver 开机自启）
- `bridge/`：与工作台/系统的指令桥（AgentCommandReceiver，权限保护）
- `api/`：与后端通信的接口契约（AgentApi，对接 enterprise-module-mdm）

## 激活前提（产品决策点）

Android Device Owner **不能靠普通安装获得**，必须三选一：
1. 出厂预置 MDM（需 OEM 合作）
2. QR Code provisioning（Android 7+，扫码自动设为 Device Owner）
3. `adb shell dpm set-device-owner`（开发/测试）

未激活时所有管控动作 fail-closed（返回 false / 拒绝擦除）。

## 与工作台通信

工作台（uni-app）通过原生插件发广播 `com.enterprise.mdm.agent.action.COMMAND` 到 `AgentCommandReceiver`，携带 `command`（LOCK/WIPE）与高危令牌；agent 执行原生能力。第三方 App 无 `BIND_AGENT` 权限无法调用。

## 与后端对接（真实 HTTP，已实现）

AgentApi 的 TODO 已替换为 OkHttp 真实实现（`api/OkHttpAgentApi.java`）：
- `reportEnrollment` → `POST /mdm/device/create`（DeviceSaveReqVO）
- `reportHeartbeat` → `POST /mdm/compliance/report`（心跳状态写入 detail）
- `fetchPolicies` → `GET /mdm/policy/effective?scopeType=DEVICE&scopeId=xxx`，Gson 解析 CommonResult
- `reportCommandResult` → 暂以合规记录承载（后端缺专用端点）
- 自动注入 `Authorization: Bearer token` 与 `tenant-id` 头；解包芋道 `CommonResult`（code=0 成功）；网络失败重试 2 次
- 配置（`AgentConfig`）从 SharedPreferences 读服务端/租户/token/deviceId，不硬编码；未配置 fail-closed
- `HeartbeatWorker` 已接入真实上报：未配置返回 failure、网络失败返回 retry（WorkManager 退避）

**架构缺口（如实）**：后端 `/mdm/device/*`、`/mdm/policy/effective` 带 `@PreAuthorize('mdm:*:*')`，是管理端接口，agent 复用需管理员 token——不合理。正确做法是后端补「设备端专用 API」（设备证书/激活令牌鉴权）。当前先注入 token 打通链路，待后端补齐后替换鉴权即可。生产环境 token 应改用 Keystore/EncryptedSharedPreferences 加密存储（当前为普通 SP 骨架）。

## 验证状态（如实说明）

- ✅ 便携 JDK17 + Android API/OkHttp/Gson stub 全量 javac 编译：通过（16 class）
- ✅ PolicyJsonParser 7 项自测：全过
- ✅ API 协议逻辑 7 项自测：全过（CommonResult code 判定、Authorization/tenant-id 注入、fail-closed、策略 query 拼接）
- ❌ **无 Android SDK / Gradle / adb，未能真编译成 APK，也未在真机/模拟器运行**。要出可安装包需在装有 Android SDK 的环境执行 `./gradlew assembleRelease`。
- ❌ **未端到端联调**：无 MySQL/Redis/后端运行环境，真实 HTTP 调用只做了协议层本地验证，未连真后端。
