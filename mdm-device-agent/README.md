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

## 验证状态（如实说明）

- ✅ 用便携 JDK17 + Android API stub 对全部 8 个 Java 类做 javac 编译：通过（9 class）
- ✅ PolicyJsonParser 7 项自测：全过（密码长度/相机禁用/缺键默认/null 安全）
- ❌ **无 Android SDK / Gradle / adb，未能真编译成 APK，也未在真机/模拟器运行**。要出可安装包需在装有 Android SDK 的环境执行 `./gradlew assembleRelease`。
- 真实网络调用（AgentApi、心跳、策略拉取）为接口/TODO，需接后端后实现。
