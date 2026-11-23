// 野火 IM（WildfireIM）桌面端适配层。
// 真实收发消息需要野火服务端（自建开源版或商业版）+ 野火 IM SDK。
// 本适配器定义统一接口；未配置 imServerUrl / 无 SDK 时明确抛错，绝不伪造消息。
//
// 接入野火时：引入官方 SDK 并在 connect()/sendMessage() 中替换为 SDK 调用即可，
// 上层 UI 只依赖本接口（与后端 ImClient 适配层设计一致，商业版可插拔）。

class WildfireImClient {
  constructor() {
    this.config = { serverUrl: '', userId: '', token: '' };
    this.connected = false;
    this.socket = null;
    this.handlers = { message: [] };
  }

  /** 配置连接参数（由设置页/登录后注入，不硬编码密钥） */
  configure({ serverUrl, userId, token }) {
    this.config = {
      serverUrl: serverUrl || '',
      userId: userId || '',
      token: token || '',
    };
  }

  isConfigured() {
    return !!(this.config.serverUrl && this.config.userId && this.config.token);
  }

  /** 连接野火 IM：未配置时拒绝（fail-closed） */
  connect() {
    if (!this.isConfigured()) {
      throw new Error('野火 IM 未配置（serverUrl/userId/token），无法连接');
    }
    // TODO 真实接入：初始化野火 IM SDK（openSDK / connect），或连接其 WebSocket 网关。
    // 这里不伪造连接成功，交由真实 SDK 实现。
    throw new Error('野火 IM SDK 尚未集成：需在有野火服务端环境引入官方 SDK 并实现 connect()');
  }

  /** 注册消息监听 */
  onMessage(handler) {
    if (typeof handler === 'function') this.handlers.message.push(handler);
  }

  /** 发送单聊消息：未连接时拒绝 */
  sendMessage(toUserId, content) {
    if (!this.connected) {
      throw new Error('野火 IM 未连接，消息未发送');
    }
    // TODO 真实接入：调用野火 SDK sendMessage
    return false;
  }

  disconnect() {
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
    this.connected = false;
  }
}

window.ImClient = new WildfireImClient();
