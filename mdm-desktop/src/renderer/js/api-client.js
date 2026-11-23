// 渲染进程 API 客户端：调用主进程 IPC，对接 enterprise-module-mdm 后端接口。
// 主进程已完成认证头注入与 CommonResult 解包；这里只做路径封装。
const api = window.desktopApi;

const ApiClient = {
  // ===== 配置 / 认证 =====
  getConfig: () => api.getConfig(),
  setConfig: (baseUrl, tenantId) => api.setConfig({ baseUrl, tenantId }),
  login: (username, password) => api.login(username, password),
  logout: () => api.logout(),
  authStatus: () => api.authStatus(),

  request: (method, path, params, body) => api.request({ method, path, params, body }),

  // ===== 设备 =====
  getDevicePage: (params) => api.request({ method: 'GET', path: '/mdm/device/page', params }),
  getDevice: (id) => api.request({ method: 'GET', path: '/mdm/device/get', params: { id } }),
  updateDeviceStatus: (id, status) => api.request({ method: 'PUT', path: '/mdm/device/status', params: { id, status } }),
  createDevice: (data) => api.request({ method: 'POST', path: '/mdm/device/create', body: data }),
  deleteDevice: (id) => api.request({ method: 'DELETE', path: '/mdm/device/delete', params: { id } }),

  // ===== 通讯录（对接底座 system/user 简单列表；如无权限返回空）=====
  getUserList: () => api.request({ method: 'GET', path: '/system/user/list' }),

  // ===== 应用 =====
  getAppList: () => api.request({ method: 'GET', path: '/mdm/app/list' }),

  // ===== 审批（对接 Flowable 流程实例，按实际端点调整）=====
  getApprovalList: () => api.request({ method: 'GET', path: '/bpm/process-instance/page', params: { pageNo: 1, pageSize: 50 } }),

  // ===== 合规 =====
  getComplianceAlerts: () => api.request({ method: 'GET', path: '/mdm/compliance/unalerted' }),
  markComplianceAlerted: (id) => api.request({ method: 'PUT', path: '/mdm/compliance/alerted', params: { id } }),

  // ===== 远程管控 =====
  dispatchCommand: (params) => api.request({ method: 'POST', path: '/mdm/command/dispatch', params }),
  getCommand: (id) => api.request({ method: 'GET', path: '/mdm/command/get', params: { id } }),

  // ===== 消息（经 IM 适配层；见 im-client.js）=====
  sendNotice: (p) => api.request({ method: 'POST', path: '/mdm/im/notification/enqueue', params: p }),
};

window.ApiClient = ApiClient;
