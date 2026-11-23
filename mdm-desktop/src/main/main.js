// Electron 主进程：创建窗口、管理应用生命周期。
// 桌面端是独立应用（原生三栏布局），不是 Web 套壳——加载本地 renderer/index.html。
const { app, BrowserWindow, ipcMain, shell } = require('electron');
const path = require('path');

let mainWindow = null;

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 800,
    minWidth: 960,
    minHeight: 640,
    frame: false, // 自绘标题栏（钉钉风格）
    webPreferences: {
      preload: path.join(__dirname, 'preload.js'),
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true,
    },
  });

  mainWindow.loadFile(path.join(__dirname, '..', 'renderer', 'index.html'));
}

app.whenReady().then(() => {
  createWindow();
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow();
  });
});

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit();
});

// ===== 自绘标题栏窗口控制 =====
ipcMain.on('window-min', () => mainWindow && mainWindow.minimize());
ipcMain.on('window-max', () => {
  if (!mainWindow) return;
  if (mainWindow.isMaximized()) mainWindow.unmaximize();
  else mainWindow.maximize();
});
ipcMain.on('window-close', () => mainWindow && mainWindow.close());

// ===== 外部链接用系统浏览器打开 =====
ipcMain.on('open-external', (_event, url) => {
  if (typeof url === 'string' && /^https?:\/\//.test(url)) {
    shell.openExternal(url);
  }
});

// ============================================================
// 真实后端对接：HTTP 请求走主进程（Node fetch），渲染进程只发 IPC。
// token / tenantId 只存内存，不落盘；芋道 CommonResult code=0 为成功。
// ============================================================
let serverConfig = { baseUrl: '', tenantId: '' };
let authState = { accessToken: '' };

/** 统一请求后端，自动加认证头并解包 CommonResult */
async function requestBackend({ method, path, params, body }) {
  if (!serverConfig.baseUrl) {
    throw new Error('未配置服务端地址，请先在设置中填写 baseUrl');
  }
  let url = serverConfig.baseUrl.replace(/\/$/, '') + path;
  if (params && Object.keys(params).length) {
    const qs = new URLSearchParams();
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null) qs.append(k, String(v));
    });
    url += '?' + qs.toString();
  }
  const headers = { 'Content-Type': 'application/json' };
  if (authState.accessToken) headers['Authorization'] = 'Bearer ' + authState.accessToken;
  if (serverConfig.tenantId) headers['tenant-id'] = String(serverConfig.tenantId);

  const resp = await fetch(url, {
    method: method || 'GET',
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!resp.ok) throw new Error(`HTTP ${resp.status} ${resp.statusText}`);
  const data = await resp.json();
  // 芋道 CommonResult: { code, data, msg }
  if (data && typeof data.code !== 'undefined') {
    if (data.code !== 0) throw new Error(data.msg || `业务错误 code=${data.code}`);
    return data.data;
  }
  return data; // 非标准包装（如野火 IM API）原样返回
}

// 配置（由设置页调用）
ipcMain.handle('config:set', (_e, cfg) => {
  if (cfg && typeof cfg.baseUrl === 'string') serverConfig.baseUrl = cfg.baseUrl.trim();
  if (cfg && cfg.tenantId !== undefined) serverConfig.tenantId = cfg.tenantId;
  return { ...serverConfig };
});
ipcMain.handle('config:get', () => ({ ...serverConfig }));

// OAuth2 密码登录（芋道 /system/oauth2/token，username+password+grant_type=password）
ipcMain.handle('auth:login', async (_e, { username, password }) => {
  const cfg = { ...serverConfig };
  if (!cfg.baseUrl) throw new Error('未配置服务端地址');
  const form = new URLSearchParams();
  form.append('username', username || '');
  form.append('password', password || '');
  form.append('grant_type', 'password');
  if (cfg.tenantId) form.append('tenant_id', String(cfg.tenantId));
  const resp = await fetch(cfg.baseUrl.replace(/\/$/, '') + '/system/oauth2/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: form.toString(),
  });
  const data = await resp.json();
  if (data.code !== 0) throw new Error(data.msg || '登录失败');
  authState.accessToken = data.data && data.data.accessToken;
  return { accessToken: authState.accessToken, userId: data.data && data.data.userId };
});

ipcMain.handle('auth:logout', () => { authState.accessToken = ''; return true; });
ipcMain.handle('auth:status', () => ({ loggedIn: !!authState.accessToken }));

// 通用 API 请求（renderer 的 api-client 通过它调用所有 MDM 接口）
ipcMain.handle('api:request', async (_e, req) => requestBackend(req));
