# 企业 MDM PC 桌面端（Electron）

> **独立开发的桌面应用**，钉钉风格原生三栏布局，**不是把 Web 管理后台套壳**。

## 布局（钉钉风格）

- **自绘标题栏**：左侧应用标识+名称、中部全局搜索（`Ctrl+K`）、右侧窗口控制（自绘最小/最大/关闭）
- **第一栏 图标导航**：消息 / 通讯录 / 设备 / 应用 / 审批 / 合规 / 设置
- **第二栏 列表栏**：随模块切换的会话/联系人/设备/应用列表（搜索、状态标签）
- **第三栏 内容区**：
  - 消息：三栏聊天界面（气泡、输入框）
  - 设备：统计卡片 + 设备信息 + 远程锁定/擦除入口
  - 通讯录/应用/审批/合规：详情卡片与操作
  - 设置：应用信息、运行时平台、架构说明

## 技术形态

- Electron 33 + 原生 HTML/CSS/JS（不引入前端框架，保持独立轻量）
- 主进程/预加载/渲染进程分离，`contextIsolation` + `sandbox` 开启，安全桥接 `desktopApi`
- 自绘标题栏通过 IPC 控制窗口（`window-min/max/close`）
- `frame: false` + 本地 `index.html`，`usesCleartextTraffic` 不适用（非 WebView 套壳）

## 与平台对接

当前列表/详情为本地演示数据（`src/renderer/js/app.js` 的 `mock*` 常量）。接后端时替换为 IPC/HTTP 调用即可：
- 设备/应用/审批/合规 → enterprise-module-mdm 的 REST API
- 消息/通讯录 → 野火 IM SDK 或底座 IM API
- 远程管控 → 本地 IPC → OS 原生 agent（Windows/macOS 设备管控层，后续）

## 验证状态（如实说明）

- ✅ `node --check` 三个 JS 文件语法检查全过
- ✅ Electron 33.4.11 依赖安装成功，二进制已下载（用 npmmirror 镜像）
- ❌ **无法在本容器真实启动窗口**：缺 libnss3/gtk3/X11 等 GUI 系统库，且沙箱禁止 apt 安装（seteuid/setgroups 不允许）。无图形界面也无法截图验证布局。
- 在装有 GUI 库的桌面环境执行 `pnpm start` 可启动；`pnpm dist` 可打包成 Windows NSIS / macOS DMG 安装包。

## 运行

```bash
pnpm install
pnpm start       # 启动桌面应用
pnpm dist        # 打包安装包
```
