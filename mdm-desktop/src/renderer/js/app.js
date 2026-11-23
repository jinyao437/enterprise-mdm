// 渲染进程：钉钉风格三栏布局的交互逻辑（独立桌面应用，非 Web 套壳）。
// 数据来自真实后端（经主进程 IPC + HTTP 对接 enterprise-module-mdm）。

// 数据源：真实后端。加载失败时显示明确提示，不静默用 mock 冒充真实数据。
const modules = {
  messages: { title: '消息', list: [], render: renderChat, load: loadMessages },
  contacts: { title: '通讯录', list: [], render: renderContact, load: loadContacts },
  devices: { title: '设备管理', list: [], render: renderDevice, load: loadDevices },
  apps: { title: '应用中心', list: [], render: renderApp, load: loadApps },
  approval: { title: '审批', list: [], render: renderApproval, load: loadApprovals },
  compliance: { title: '合规中心', list: [], render: renderCompliance, load: loadCompliance },
  settings: { title: '设置', list: [], render: renderSettings, load: null },
};

let currentModule = 'messages';
let currentItem = null;
let dataSourceError = ''; // 最近一次数据加载错误（未配置/连接失败等）

// ===== 真实 API 加载函数（替换原 mock）=====
async function loadMessages() {
  // 会话列表：后端没有独立会话接口，经 IM 适配层；未配置 IM 时明确提示
  if (!window.ImClient || !window.ImClient.isConfigured()) {
    throw new Error('野火 IM 未配置，无法加载会话（请在设置中配置 IM）');
  }
  return []; // 真实接入后由 ImClient 拉取会话列表
}
async function loadContacts() {
  const users = await window.ApiClient.getUserList();
  return (users || []).map(u => ({
    name: u.nickname || u.username, dept: u.deptName || '—', title: u.post || '—',
    phone: u.mobile || '—', imUserId: String(u.id),
  }));
}
async function loadDevices() {
  const page = await window.ApiClient.getDevicePage({ pageNo: 1, pageSize: 500 });
  return (page && page.list ? page.list : []).map(d => ({
    id: d.id, name: d.name, identifier: d.identifier, platform: d.platform,
    osVersion: d.osVersion, status: d.status,
    statusText: ({ 0: '已注册', 1: '使用中', 2: '已挂失', 3: '已停用', 4: '已报废' })[d.status] || '未知',
    bindUser: d.bindUserId ? String(d.bindUserId) : '未绑定',
  }));
}
async function loadApps() {
  const list = await window.ApiClient.getAppList();
  return (list || []).map(a => ({
    name: a.name, packageName: a.packageName, appType: a.appType, status: a.status,
    statusText: ({ 0: '草稿', 1: '已上市', 2: '已下架' })[a.status] || '未知',
    version: '—',
  }));
}
async function loadApprovals() {
  const page = await window.ApiClient.getApprovalList();
  return (page && page.list ? page.list : []).map(p => ({
    name: p.name || p.processDefinitionName || '审批单',
    applicant: p.startUserName || '—', type: p.processDefinitionName || '流程',
    status: p.status || 0, statusText: '待处理', desc: p.title || '',
  }));
}
async function loadCompliance() {
  const list = await window.ApiClient.getComplianceAlerts();
  return (list || []).map(c => ({
    name: `设备 ${c.deviceId} · ${c.checkItem}`, status: c.status,
    statusText: ({ 0: '合规', 1: '不合规', 2: '待复核' })[c.status] || '未知',
  }));
}

// ===== 性能常量 =====
const VIRTUAL_THRESHOLD = 50;   // 超过该条数启用虚拟列表
const ROW_HEIGHT = 58;          // 列表行高（与 CSS .list-item 对齐）
const OVERSCAN = 8;             // 可视区上下预渲染行数
const MAX_CHAT_MESSAGES = 500;  // 聊天窗口保留的最大消息数
let currentFilteredItems = [];  // 当前过滤后的列表数据（虚拟列表用）
let searchWorker = null;        // 搜索过滤 Worker（大数据时启用）

/** 通用防抖 */
function debounce(fn, delay) {
  let timer = null;
  return function (...args) {
    clearTimeout(timer);
    timer = setTimeout(() => fn.apply(this, args), delay);
  };
}

// ===== 初始化 =====
document.addEventListener('DOMContentLoaded', () => {
  bindWindowControls();
  bindRail();
  bindSearch();
  // 服务端配置/token 只存主进程内存，重启应用后需在设置页重新填写（不落盘，保护凭证）
  switchModule('messages');
});

function bindWindowControls() {
  document.getElementById('winMin')?.addEventListener('click', () => window.desktopApi?.min());
  document.getElementById('winMax')?.addEventListener('click', () => window.desktopApi?.max());
  document.getElementById('winClose')?.addEventListener('click', () => window.desktopApi?.close());
}

function bindRail() {
  document.querySelectorAll('.rail-item[data-module]').forEach((btn) => {
    btn.addEventListener('click', () => switchModule(btn.dataset.module));
  });
}

function bindSearch() {
  const input = document.getElementById('listSearch');
  // 防抖 200ms：每敲一个字不再立即全量重绘
  input?.addEventListener('input', debounce(() => renderList(currentModule, input.value.trim()), 200));
  document.addEventListener('keydown', (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
      e.preventDefault();
      document.getElementById('globalSearch')?.focus();
    }
  });

  // 大数据量时启用 Worker 做过滤，避免阻塞 UI 线程
  try {
    searchWorker = new Worker('js/search-worker.js');
    searchWorker.onmessage = (e) => {
      currentFilteredItems = e.data.items;
      renderVirtualList(currentFilteredItems);
    };
  } catch (_) {
    searchWorker = null; // Worker 不可用时回退到主线程过滤
  }
}

// ===== 模块切换 =====
async function switchModule(key) {
  const mod = modules[key];
  if (!mod) return;
  currentModule = key;
  currentItem = null;
  document.querySelectorAll('.rail-item').forEach((b) => b.classList.toggle('active', b.dataset.module === key));
  document.getElementById('listTitle').textContent = mod.title;
  renderContent(mod.render(null));
  if (!mod.load) { renderList(key, ''); return; }

  // 真实 API 加载；失败显示明确错误，不塞 mock
  renderContent(`<div class="content-header">${escapeHtml(mod.title)}</div>
    <div class="content-body"><div class="empty"><div class="big">⏳</div><div>加载中…</div></div></div>`);
  try {
    mod.list = await mod.load();
    dataSourceError = '';
    renderList(key, '');
    renderContent(mod.render(null));
  } catch (err) {
    dataSourceError = (err && err.message) || String(err);
    renderContent(`<div class="content-header">${escapeHtml(mod.title)}</div>
      <div class="content-body">
        <div class="panel" style="border-color:#ffccc7;background:#fff2f0">
          <h3 style="color:#cf1322">数据加载失败</h3>
          <p style="line-height:1.8;color:#595959">${escapeHtml(dataSourceError)}</p>
          <p style="color:#8f959e;font-size:12px">请在「设置」中配置服务端地址并登录；消息模块还需配置野火 IM。</p>
          <button class="btn" data-action="goto-settings">前往设置</button>
        </div>
      </div>`);
    document.querySelector('[data-action="goto-settings"]')?.addEventListener('click', () => switchModule('settings'));
  }
}

function filterItems(list, keyword) {
  return (list || []).filter((it) =>
    !keyword || (it.name && it.name.includes(keyword)) || (it.desc && it.desc.includes(keyword))
  );
}

function renderList(key, keyword) {
  const mod = modules[key];
  const raw = mod.list || [];
  // 大数据量交给 Worker 过滤+渲染，避免主线程阻塞
  if (raw.length >= VIRTUAL_THRESHOLD && searchWorker) {
    searchWorker.postMessage({ items: raw, keyword });
    return;
  }
  const items = filterItems(raw, keyword);
  currentFilteredItems = items;
  if (items.length === 0) {
    document.getElementById('listItems').innerHTML =
      '<div class="empty" style="padding:40px 0"><div class="big">📭</div><div>暂无数据</div></div>';
    return;
  }
  // 超阈值（Worker 不可用回退）启用虚拟列表，否则批量插入
  if (items.length > VIRTUAL_THRESHOLD) renderVirtualList(items);
  else renderListBatch(items);
}

/** 小列表：DocumentFragment 批量插入，一次 reflow（替代逐条 innerHTML +=） */
function renderListBatch(items) {
  const container = document.getElementById('listItems');
  const frag = document.createDocumentFragment();
  items.forEach((it, idx) => frag.appendChild(buildItemNode(it, idx)));
  container.replaceChildren(frag); // 一次性替换，单次重排
  bindItemClicks(container, items);
}

/** 大列表：虚拟滚动，只渲染可视区 + 上下预渲染行 */
function renderVirtualList(items) {
  const container = document.getElementById('listItems');
  const viewportH = container.clientHeight || 600;
  const total = items.length;
  const topSpacer = document.createElement('div');
  const bottomSpacer = document.createElement('div');
  const render = () => {
    const scrollTop = container.scrollTop;
    const start = Math.max(0, Math.floor(scrollTop / ROW_HEIGHT) - OVERSCAN);
    const end = Math.min(total, Math.ceil((scrollTop + viewportH) / ROW_HEIGHT) + OVERSCAN);
    const frag = document.createDocumentFragment();
    topSpacer.style.height = (start * ROW_HEIGHT) + 'px';
    bottomSpacer.style.height = ((total - end) * ROW_HEIGHT) + 'px';
    frag.appendChild(topSpacer);
    for (let i = start; i < end; i++) frag.appendChild(buildItemNode(items[i], i));
    frag.appendChild(bottomSpacer);
    container.replaceChildren(frag);
    bindItemClicks(container, items);
  };
  container.onscroll = render; // 覆盖式绑定，避免重复监听累积
  render();
}

/** 构造单个列表行节点 */
function buildItemNode(it, idx) {
  const el = document.createElement('div');
  el.className = 'list-item' + (idx === 0 && !currentItem ? ' active' : '');
  el.dataset.idx = idx;
  el.innerHTML = `
    <div class="list-item-avatar">${escapeHtml((it.name || '?').slice(0, 1))}</div>
    <div class="list-item-main">
      <div class="list-item-name">${escapeHtml(it.name || '')}</div>
      <div class="list-item-desc">${escapeHtml(it.desc || '')}</div>
    </div>
    ${it.statusText ? `<span class="list-item-status ${statusClass(it.status)}">${escapeHtml(it.statusText)}</span>` : ''}`;
  return el;
}

/** 绑定列表行点击（事件委托到容器，减少监听器数量） */
function bindItemClicks(container, items) {
  container.onclick = (e) => {
    const row = e.target.closest('.list-item');
    if (!row) return;
    container.querySelectorAll('.list-item').forEach((x) => x.classList.remove('active'));
    row.classList.add('active');
    currentItem = items[Number(row.dataset.idx)];
    renderContent(modules[currentModule].render(currentItem));
  };
}

function renderContent(html) {
  document.getElementById('content').innerHTML = html;
  bindContentActions();
}

// ===== 内容渲染函数 =====
function renderChat(item) {
  if (!item) {
    return contentShell('消息', `
      <div class="empty"><div class="big">💬</div><div>选择一个会话开始沟通</div></div>`);
  }
  return `
    <div class="content-header">${escapeHtml(item.name)}</div>
    <div class="chat-area">
      <div class="chat-messages">
        <div class="chat-bubble left">你好，设备巡检任务已下发，请确认。</div>
        <div class="chat-bubble right">收到，正在处理。</div>
        <div class="chat-bubble left">${escapeHtml(item.desc || '')}</div>
      </div>
      <div class="chat-input">
        <input type="text" placeholder="输入消息，回车发送" id="chatInput" />
        <button class="btn" data-action="send-chat">发送</button>
      </div>
    </div>`;
}

function renderContact(item) {
  if (!item) {
    return contentShell('通讯录', `<div class="stat-grid">
      <div class="stat-card"><div class="num">${modules.contacts.list.length}</div><div class="label">联系人</div></div>
    </div><div class="empty"><div class="big">👥</div><div>选择联系人查看详情</div></div>`);
  }
  return contentShell('联系人详情', infoPanel([
    ['姓名', item.name], ['部门', item.dept || '—'], ['职位', item.title || '—'],
    ['手机号', item.phone || '—'], ['IM 账号', item.imUserId || '—'], ['状态', tag('在职', 'green')],
  ]));
}

function renderDevice(item) {
  const stats = `<div class="stat-grid">
    <div class="stat-card"><div class="num">${modules.devices.list.length}</div><div class="label">纳管设备</div></div>
    <div class="stat-card"><div class="num">${modules.devices.list.filter(d => d.status === 1).length}</div><div class="label">使用中</div></div>
    <div class="stat-card"><div class="num">${modules.devices.list.filter(d => d.status === 3).length}</div><div class="label">已停用</div></div>
  </div>`;
  if (!item) {
    return contentShell('设备管理', stats + `
      <div class="panel"><h3>设备列表</h3><div class="empty"><div class="big">📱</div><div>从左侧选择设备查看管控面板</div></div></div>`);
  }
  return contentShell('设备管控 · ' + item.name, stats + `
    <div class="panel"><h3>设备信息</h3>
      ${infoRows([
        ['设备名称', item.name], ['设备标识', item.identifier || '—'],
        ['平台', item.platform || 'android'], ['系统版本', item.osVersion || '—'],
        ['状态', tag(item.statusText, statusClass(item.status))],
        ['绑定用户', item.bindUser || '未绑定'],
      ])}
    </div>
    <div class="panel"><h3>远程管控</h3>
      <button class="btn" data-action="device-lock">远程锁定</button>
      <button class="btn btn-ghost" data-action="device-wipe">远程擦除（高危）</button>
      <p style="color:var(--text-sub);font-size:12px;margin-top:10px">管控指令经本地桥接下发到原生 device agent；擦除需二次确认令牌。</p>
    </div>`);
}

function renderApp(item) {
  if (!item) {
    return contentShell('应用中心', `<div class="stat-grid">
      <div class="stat-card"><div class="num">${modules.apps.list.length}</div><div class="label">企业应用</div></div>
    </div><div class="empty"><div class="big">🧩</div><div>选择应用查看版本与分发</div></div>`);
  }
  return contentShell('应用 · ' + item.name, infoPanel([
    ['应用名称', item.name], ['包名', item.packageName || '—'],
    ['类型', item.appType === 'THIRD_PARTY' ? '三方接入' : '企业自建'],
    ['状态', tag(item.statusText, statusClass(item.status))],
    ['最新版本', item.version || '—'],
  ]));
}

function renderApproval(item) {
  if (!item) {
    return contentShell('审批', `<div class="stat-grid">
      <div class="stat-card"><div class="num">${modules.approval.list.length}</div><div class="label">待处理</div></div>
    </div><div class="empty"><div class="big">✅</div><div>选择审批单查看详情</div></div>`);
  }
  return contentShell('审批详情', infoPanel([
    ['标题', item.name], ['发起人', item.applicant || '—'],
    ['类型', item.type || '请假'], ['状态', tag(item.statusText, statusClass(item.status))],
    ['说明', item.desc || '—'],
  ]) + `<div class="panel"><button class="btn" data-action="approve">同意</button> <button class="btn btn-ghost" data-action="reject">拒绝</button></div>`);
}

function renderCompliance(item) {
  const list = modules.compliance.list;
  return contentShell('合规中心', `<div class="stat-grid">
    <div class="stat-card"><div class="num">${list.length}</div><div class="label">待处理告警</div></div>
  </div><div class="panel"><h3>合规告警</h3>
    ${list.length ? infoRows(list.slice(0, 100).map(c => [c.name, tag(c.statusText, statusClass(c.status))])) : '<div style="color:var(--text-sub)">暂无告警</div>'}
  </div>`);
}

function renderSettings() {
  return contentShell('设置', `
    <div class="panel">
      <h3>服务端连接</h3>
      <div class="info-row"><div class="k">服务端地址</div><div class="v"><input id="cfgBaseUrl" placeholder="https://mdm.example.com" style="width:100%;padding:8px;border:1px solid var(--border);border-radius:5px" /></div></div>
      <div class="info-row"><div class="k">租户编号</div><div class="v"><input id="cfgTenantId" placeholder="选填，逻辑隔离租户 ID" style="width:100%;padding:8px;border:1px solid var(--border);border-radius:5px" /></div></div>
      <div style="margin-top:12px"><button class="btn" data-action="save-config">保存配置</button> <button class="btn btn-ghost" data-action="test-conn">测试连接</button></div>
    </div>
    <div class="panel">
      <h3>账号登录（芋道 OAuth2）</h3>
      <div class="info-row"><div class="k">用户名</div><div class="v"><input id="cfgUser" placeholder="用户名" style="width:100%;padding:8px;border:1px solid var(--border);border-radius:5px" /></div></div>
      <div class="info-row"><div class="k">密码</div><div class="v"><input id="cfgPass" type="password" placeholder="密码" style="width:100%;padding:8px;border:1px solid var(--border);border-radius:5px" /></div></div>
      <div style="margin-top:12px"><button class="btn" data-action="login">登录</button> <button class="btn btn-ghost" data-action="logout">退出</button> <span id="authStatusText" style="margin-left:10px;color:var(--text-sub)"></span></div>
    </div>
    <div class="panel">
      <h3>野火 IM</h3>
      <div class="info-row"><div class="k">服务端地址</div><div class="v"><input id="cfgImUrl" placeholder="野火 IM server URL（开源自建）" style="width:100%;padding:8px;border:1px solid var(--border);border-radius:5px" /></div></div>
      <div style="margin-top:12px"><button class="btn" data-action="save-im">保存 IM 配置</button></div>
      <p style="color:var(--text-sub);font-size:12px;margin-top:10px">野火 IM 需真实服务端与 SDK；未配置时消息模块会明确提示，不显示伪造会话。</p>
    </div>
    <div class="panel"><p style="color:var(--text-sub);font-size:12px;line-height:1.8">
      独立 Electron 桌面应用，自绘标题栏 + 三栏布局，不加载远程 Web 页面。<br/>
      运行时平台：${escapeHtml(window.desktopApi?.platform || 'unknown')} ｜ 版本 0.1.0
    </p></div>`);
}

// ===== DOM 辅助 =====
function contentShell(header, body) {
  return `<div class="content-header">${escapeHtml(header)}</div><div class="content-body">${body}</div>`;
}
function infoPanel(rows) { return `<div class="panel">${infoRows(rows)}</div>`; }
function infoRows(rows) {
  return rows.map(([k, v]) => `<div class="info-row"><div class="k">${escapeHtml(k)}</div><div class="v">${v}</div></div>`).join('');
}
function tag(text, cls) { return `<span class="tag ${cls}">${escapeHtml(text || '')}</span>`; }
function statusClass(status) {
  return ({ 0: 'tag-gray', 1: 'tag-green', 2: 'tag-red', 3: 'tag-gray', 4: 'tag-blue' })[status] || 'tag-gray';
}
function escapeHtml(s) {
  return String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

// ===== 内容区按钮行为 =====
function bindContentActions() {
  document.querySelectorAll('[data-action]').forEach((btn) => {
    btn.addEventListener('click', async () => {
      const action = btn.dataset.action;
      const client = window.ApiClient;
      try {
        if (action === 'send-chat') {
          const input = document.getElementById('chatInput');
          if (!input || !input.value.trim()) return;
          const box = document.querySelector('.chat-messages');
          const bubble = document.createElement('div');
          bubble.className = 'chat-bubble right';
          bubble.textContent = input.value.trim();
          box.appendChild(bubble);
          while (box.children.length > MAX_CHAT_MESSAGES) box.removeChild(box.firstChild);
          input.value = '';
          box.scrollTop = box.scrollHeight;
          // 真实发送：经后端 IM 通知接口入队（野火 SDK 未接入时由后端适配层处理）
          if (client && currentItem) {
            await client.sendNotice({
              eventId: 'chat-' + Date.now(), messageType: 1,
              recipient: currentItem.imUserId || 0,
              title: '桌面端消息', content: bubble.textContent,
            });
          }
        } else if (action === 'device-lock' || action === 'device-wipe') {
          if (!currentItem) return;
          const isWipe = action === 'device-wipe';
          const type = isWipe ? 2 : 1;
          let confirmToken;
          if (isWipe) {
            confirmToken = prompt('高危操作：请输入预发放的二次确认令牌（后端会校验）');
            if (!confirmToken) return;
          } else if (!confirm('确认远程锁定该设备？')) {
            return;
          }
          await client.dispatchCommand({
            deviceId: currentItem.id, type,
            idempotencyKey: `${type}-${currentItem.id}-${Date.now()}`,
            confirmToken,
          });
          alert(isWipe ? '擦除指令已提交' : '锁定指令已提交');
        } else if (action === 'approve' || action === 'reject') {
          alert('审批操作需对接 Flowable 流程接口（当前端点按实际部署调整）');
        } else if (action === 'save-config') {
          const baseUrl = document.getElementById('cfgBaseUrl').value.trim();
          const tenantId = document.getElementById('cfgTenantId').value.trim();
          await client.setConfig(baseUrl, tenantId);
          alert('服务端配置已保存');
        } else if (action === 'test-conn') {
          const cfg = await client.getConfig();
          if (!cfg.baseUrl) { alert('请先填写并保存服务端地址'); return; }
          const users = await client.getUserList();
          alert('连接成功，返回 ' + (users ? users.length : 0) + ' 条用户数据');
        } else if (action === 'login') {
          const u = document.getElementById('cfgUser').value.trim();
          const p = document.getElementById('cfgPass').value;
          await client.login(u, p);
          document.getElementById('authStatusText').textContent = '已登录';
          alert('登录成功');
        } else if (action === 'logout') {
          await client.logout();
          document.getElementById('authStatusText').textContent = '未登录';
        } else if (action === 'save-im') {
          const url = document.getElementById('cfgImUrl').value.trim();
          const userId = document.getElementById('cfgUser')?.value.trim() || '';
          window.ImClient.configure({ serverUrl: url, userId, token: '' });
          alert('IM 配置已保存（token 需在接入 SDK 后注入）');
        }
      } catch (err) {
        alert('操作失败：' + ((err && err.message) || err));
      }
    });
  });
}
