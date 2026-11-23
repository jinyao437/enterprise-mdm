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

## 与平台对接（真实 IPC/HTTP，已替换 mock）

数据全部来自真实后端，**不再使用 mock 假数据**：

- **主进程 HTTP 客户端**（`src/main/main.js`）：Node `fetch` 调后端，自动注入 `Authorization: Bearer <token>` 与 `tenant-id` 头，解包芋道 `CommonResult`（`code=0` 成功，否则抛业务错误）
- **认证**（`auth:login`）：对接芋道 OAuth2 `/system/oauth2/token`（password 模式）；token 只存主进程内存，不落盘
- **API 客户端**（`src/renderer/js/api-client.js`）：封装设备/应用/审批/合规/远程管控/通知接口，路径对齐 enterprise-module-mdm 的 Controller
- **野火 IM 适配**（`src/renderer/js/im-client.js`）：统一接口，未配置 serverUrl/userId/token 时明确抛错、拒绝收发，**不伪造消息**；接真实 SDK 时替换 `connect/sendMessage` 即可（商业版可插拔）
- **远程管控**：`device-lock`/`device-wipe` 直接调 `/mdm/command/dispatch`，WIPE 必填二次确认令牌
- **设置页**：可配置服务端地址、租户、登录、IM 地址；数据加载失败显示明确错误并引导去设置，不静默降级假数据

**尚未接真环境的部分（如实）**：审批列表指向 Flowable `/bpm/process-instance/page`（端点按实际部署调整）；野火 IM SDK 未集成；OS 原生设备管控 agent（Windows/macOS）未实现。

## 性能优化（已实现）

针对"接真实数据后上规模"做了 4 项优化：
- **虚拟列表**：列表超过 50 条只渲染可视区 + 上下 8 行预渲染，万级数据首屏不再全量建 DOM（`ROW_HEIGHT`/`OVERSCAN` 可调）
- **搜索防抖**：搜索框 200ms 防抖，避免每敲一个字重排整个列表
- **消息上限**：聊天窗口最多保留 500 条，超出移除最早的，防止长时间运行内存/DOM 无限增长
- **后台过滤 + 批量插入**：大数据量用 `search-worker.js` 在 Worker 线程过滤；列表用 `DocumentFragment` + `replaceChildren` 一次 reflow，事件用容器级委托减少监听器

## 验证状态（如实说明）

- ✅ `node --check` 全部 6 个 JS 文件语法检查全过
- ✅ HTTP 对接隔离测试 2/2 通过：用本地模拟芋道后端验证 `CommonResult` 解包、`Authorization`/`tenant-id` 头注入、query 拼接、`code!=0` 抛错
- ✅ Electron 33.4.11 依赖安装成功，二进制已下载（用 npmmirror 镜像）
- ❌ **无法在本容器真实启动窗口**：缺 libnss3/gtk3/X11 等 GUI 系统库，且沙箱禁止 apt 安装（seteuid/setgroups 不允许）。无图形界面也无法截图验证布局。
- ❌ **未端到端联调**：工作区无 MySQL/Redis/野火 IM 服务端，无法验证与真实后端的完整链路；上述 HTTP 测试是本地模拟服务验证对接逻辑。
- 在装有 GUI 库的桌面环境执行 `pnpm start` 可启动；`pnpm dist` 可打包成 Windows NSIS / macOS DMG 安装包。

## 运行

```bash
pnpm install
pnpm start       # 启动桌面应用
pnpm dist        # 打包安装包
```
