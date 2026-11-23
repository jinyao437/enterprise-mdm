// 渲染进程：钉钉风格三栏布局的交互逻辑（独立桌面应用，非 Web 套壳）。
// 数据目前为本地演示数据；接后端时把这些 mock 替换为 IPC/HTTP 调用即可。

const modules = {
  messages: { title: '消息', list: mockConversations, render: renderChat },
  contacts: { title: '通讯录', list: mockContacts, render: renderContact },
  devices: { title: '设备管理', list: mockDevices, render: renderDevice },
  apps: { title: '应用中心', list: mockApps, render: renderApp },
  approval: { title: '审批', list: mockApprovals, render: renderApproval },
  compliance: { title: '合规中心', list: mockCompliance, render: renderCompliance },
  settings: { title: '设置', list: [], render: renderSettings },
};

let currentModule = 'messages';
let currentItem = null;

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
function switchModule(key) {
  const mod = modules[key];
  if (!mod) return;
  currentModule = key;
  currentItem = null;
  document.querySelectorAll('.rail-item').forEach((b) => b.classList.toggle('active', b.dataset.module === key));
  document.getElementById('listTitle').textContent = mod.title;
  renderList(key, '');
  renderContent(mod.render(null));
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
      <div class="stat-card"><div class="num">${mockContacts.length}</div><div class="label">联系人</div></div>
      <div class="stat-card"><div class="num">6</div><div class="label">部门</div></div>
    </div><div class="empty"><div class="big">👥</div><div>选择联系人查看详情</div></div>`);
  }
  return contentShell('联系人详情', infoPanel([
    ['姓名', item.name], ['部门', item.dept || '—'], ['职位', item.title || '—'],
    ['手机号', item.phone || '—'], ['IM 账号', item.imUserId || '—'], ['状态', tag('在职', 'green')],
  ]));
}

function renderDevice(item) {
  const stats = `<div class="stat-grid">
    <div class="stat-card"><div class="num">${mockDevices.length}</div><div class="label">纳管设备</div></div>
    <div class="stat-card"><div class="num">${mockDevices.filter(d => d.status === 1).length}</div><div class="label">使用中</div></div>
    <div class="stat-card"><div class="num">${mockDevices.filter(d => d.status === 3).length}</div><div class="label">已停用</div></div>
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
      <div class="stat-card"><div class="num">${mockApps.length}</div><div class="label">企业应用</div></div>
      <div class="stat-card"><div class="num">2</div><div class="label">待更新</div></div>
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
      <div class="stat-card"><div class="num">3</div><div class="label">待我审批</div></div>
      <div class="stat-card"><div class="num">12</div><div class="label">本月已处理</div></div>
    </div><div class="empty"><div class="big">✅</div><div>选择审批单查看详情</div></div>`);
  }
  return contentShell('审批详情', infoPanel([
    ['标题', item.name], ['发起人', item.applicant || '—'],
    ['类型', item.type || '请假'], ['状态', tag(item.statusText, statusClass(item.status))],
    ['说明', item.desc || '—'],
  ]) + `<div class="panel"><button class="btn" data-action="approve">同意</button> <button class="btn btn-ghost" data-action="reject">拒绝</button></div>`);
}

function renderCompliance(item) {
  return contentShell('合规中心', `<div class="stat-grid">
    <div class="stat-card"><div class="num">${mockCompliance.length}</div><div class="label">待处理告警</div></div>
    <div class="stat-card"><div class="num">96%</div><div class="label">合规率</div></div>
  </div><div class="panel"><h3>合规告警</h3>
    ${infoRows(mockCompliance.map(c => [c.name, tag(c.statusText, statusClass(c.status))]))}
  </div>`);
}

function renderSettings() {
  return contentShell('设置', infoPanel([
    ['应用名称', '企业 MDM 工作台'], ['版本', '0.1.0'],
    ['服务端地址', '未配置（接后端时设置）'], ['设备管控', '原生 agent 桥接'],
    ['运行时平台', window.desktopApi?.platform || 'unknown'],
  ]) + `<div class="panel"><p style="color:var(--text-sub);font-size:12px;line-height:1.8">
    本客户端为独立 Electron 桌面应用，采用自绘标题栏与三栏布局，不加载远程 Web 页面。<br/>
    消息、通讯录、设备、应用、审批、合规各模块通过 IPC / HTTP 与平台后端对接。
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
    btn.addEventListener('click', () => {
      const action = btn.dataset.action;
      if (action === 'send-chat') {
        const input = document.getElementById('chatInput');
        if (input && input.value.trim()) {
          const box = document.querySelector('.chat-messages');
          const bubble = document.createElement('div');
          bubble.className = 'chat-bubble right';
          bubble.textContent = input.value.trim();
          box.appendChild(bubble);
          // 消息上限：超出后移除最早的，防止 DOM 无限增长导致内存/布局变重
          while (box.children.length > MAX_CHAT_MESSAGES) {
            box.removeChild(box.firstChild);
          }
          input.value = '';
          box.scrollTop = box.scrollHeight;
        }
      } else if (action === 'device-lock') {
        alert('远程锁定指令已下发（演示）');
      } else if (action === 'device-wipe') {
        confirm('高危操作：确认远程擦除该设备？真实环境需二次确认令牌。') && alert('擦除指令已下发（演示）');
      } else if (action === 'approve' || action === 'reject') {
        alert(action === 'approve' ? '已同意（演示）' : '已拒绝（演示）');
      }
    });
  });
}

// ===== 演示数据（接后端时替换）=====
const mockConversations = [
  { name: '产品协作群', desc: '张伟：策略下发完成，请查收', status: 1, statusText: '12' },
  { name: 'IT 运维', desc: '李娜：设备巡检报告已生成', status: 1, statusText: '3' },
  { name: '张伟', desc: '收到，稍后处理', status: 1 },
  { name: '系统通知', desc: '合规告警：1 台设备密码策略不合规', status: 0 },
];
const mockContacts = [
  { name: '张伟', dept: '技术部', title: '后端工程师', phone: '138****0012', imUserId: 'zhangsan' },
  { name: '李娜', dept: 'IT 运维', title: '运维主管', phone: '138****0034', imUserId: 'lina' },
  { name: '王强', dept: '产品部', title: '产品经理', phone: '138****0056', imUserId: 'wangqiang' },
];
const mockDevices = [
  { name: '张三的手机', identifier: 'IMEI-860000001', platform: 'android', osVersion: 'Android 14', status: 1, statusText: '使用中', bindUser: '张伟' },
  { name: '李四的平板', identifier: 'SN-PAD-002', platform: 'android', osVersion: 'Android 13', status: 3, statusText: '已停用', bindUser: '李娜' },
  { name: '演示机', identifier: 'IMEI-860000003', platform: 'android', osVersion: 'Android 12', status: 0, statusText: '已注册', bindUser: '未绑定' },
];
const mockApps = [
  { name: '企业微信', packageName: 'com.tencent.wework', appType: 'THIRD_PARTY', status: 1, statusText: '已上架', version: '4.1.0' },
  { name: '内部报销', packageName: 'com.enterprise.expense', appType: 'INTERNAL', status: 1, statusText: '已上架', version: '1.2.0' },
];
const mockApprovals = [
  { name: '设备领用申请', applicant: '王强', type: '设备领用', status: 0, statusText: '待审批', desc: '申请领用测试机一台' },
  { name: '请假申请', applicant: '张伟', type: '请假', status: 0, statusText: '待审批', desc: '11-25 至 11-26 年假' },
];
const mockCompliance = [
  { name: '张三的手机 · 密码策略不合规', status: 1, statusText: '不合规' },
  { name: '李四的平板 · 系统版本过低', status: 2, statusText: '待复核' },
];
