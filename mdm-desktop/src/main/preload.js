// 预加载脚本：通过 contextBridge 安全暴露 API 给渲染进程。
// contextIsolation + sandbox 下，渲染进程不能直接访问 Node/Electron。
const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('desktopApi', {
  // 窗口控制
  min: () => ipcRenderer.send('window-min'),
  max: () => ipcRenderer.send('window-max'),
  close: () => ipcRenderer.send('window-close'),
  openExternal: (url) => ipcRenderer.send('open-external', url),
  platform: process.platform,

  // 配置
  getConfig: () => ipcRenderer.invoke('config:get'),
  setConfig: (cfg) => ipcRenderer.invoke('config:set', cfg),

  // 认证
  login: (username, password) => ipcRenderer.invoke('auth:login', { username, password }),
  logout: () => ipcRenderer.invoke('auth:logout'),
  authStatus: () => ipcRenderer.invoke('auth:status'),

  // 通用后端 API（带认证头 + CommonResult 解包，在主进程完成）
  request: (req) => ipcRenderer.invoke('api:request', req),
});
