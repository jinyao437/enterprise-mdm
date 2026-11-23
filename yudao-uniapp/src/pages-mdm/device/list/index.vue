<template>
  <view class="yd-page-container">
    <wd-navbar title="我的设备" safe-area-inset-top fixed placeholder />

    <view class="px-3 pt-3">
      <wd-search v-model="keyword" placeholder="搜索设备名称/标识" @search="handleSearch" @clear="handleSearch" />

      <view v-for="item in list" :key="item.id" class="mt-3" @click="goDetail(item.id)">
        <wd-cell-group border>
          <wd-cell :title="item.name" :label="item.identifier">
            <template #value>
              <wd-tag :type="statusType(item.status)" round>{{ statusName(item.status) }}</wd-tag>
            </template>
          </wd-cell>
          <wd-cell title="平台" :value="platformName(item.platform)" />
          <wd-cell v-if="item.osVersion" title="系统版本" :value="item.osVersion" />
        </wd-cell-group>
      </view>

      <wd-status-tip v-if="!loading && list.length === 0" image="content" tip="暂无设备" />
      <view class="flex justify-center py-4 text-gray-400 text-sm">{{ loading ? '加载中...' : (noMore ? '没有更多了' : '') }}</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { getDevicePage, type DeviceVO } from '@/api/mdm/device'

definePage({ style: { navigationBarTitleText: '', navigationStyle: 'custom' } })

const loading = ref(false)
const noMore = ref(false)
const list = ref<DeviceVO[]>([])
const keyword = ref('')
const pageNo = ref(1)
const pageSize = 10

const statusName = (s: number) => ({ 0: '已注册', 1: '使用中', 2: '已挂失', 3: '已停用', 4: '已报废' } as any)[s] || '未知'
const statusType = (s: number): any => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger', 4: 'info' } as any)[s] || 'info'
const platformName = (p: string) => ({ android: 'Android', ios: 'iOS', harmony: '鸿蒙', windows: 'Windows', macos: 'macOS' } as any)[p] || p

async function load(reset = false) {
  if (reset) { pageNo.value = 1; noMore.value = false }
  if (noMore.value || loading.value) return
  loading.value = true
  try {
    const data = await getDevicePage({ pageNo: pageNo.value, pageSize, name: keyword.value || undefined })
    list.value = reset ? data.list : list.value.concat(data.list)
    noMore.value = data.list.length < pageSize
    if (!noMore.value) pageNo.value++
  } finally {
    loading.value = false
  }
}

function handleSearch() { load(true) }
function goDetail(id: number) { uni.navigateTo({ url: `/pages-mdm/device/detail/index?id=${id}` }) }

onMounted(() => load(true))
onReachBottom(() => load())
</script>
