// 预加载脚本：通过 contextBridge 安全暴露最小化 API 给渲染进程。
// contextIsolation + sandbox 下，渲染进程不能直接访问 Node/Electron。
const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('desktopApi', {
  min: () => ipcRenderer.send('window-min'),
  max: () => ipcRenderer.send('window-max'),
  close: () => ipcRenderer.send('window-close'),
  openExternal: (url) => ipcRenderer.send('open-external', url),
  platform: process.platform,
});
