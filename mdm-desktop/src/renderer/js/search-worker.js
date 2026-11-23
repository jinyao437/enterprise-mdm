// 搜索过滤 Worker：在后台线程过滤大列表，避免阻塞 UI 渲染线程。
// 只做纯数据操作，不访问 DOM。结果回传给渲染线程做虚拟列表渲染。
self.onmessage = (e) => {
  const { items, keyword } = e.data || {};
  if (!Array.isArray(items)) {
    self.postMessage({ items: [] });
    return;
  }
  const kw = keyword || '';
  const filtered = kw
    ? items.filter((it) =>
        (it.name && it.name.includes(kw)) || (it.desc && it.desc.includes(kw)))
    : items;
  self.postMessage({ items: filtered });
};
