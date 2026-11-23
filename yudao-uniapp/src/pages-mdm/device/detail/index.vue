<template>
  <view class="yd-page-container">
    <wd-navbar title="设备详情" left-arrow safe-area-inset-top fixed placeholder @click-left="goBack" />

    <view class="px-3 pt-3">
      <wd-cell-group border>
        <wd-cell title="设备名称" :value="device?.name || '-'" />
        <wd-cell title="设备标识" :value="device?.identifier || '-'" />
        <wd-cell title="平台" :value="platformName(device?.platform)" />
        <wd-cell title="系统版本" :value="device?.osVersion || '-'" />
        <wd-cell title="状态">
          <template #value>
            <wd-tag :type="statusType(device?.status || 0)" round>{{ statusName(device?.status || 0) }}</wd-tag>
          </template>
        </wd-cell>
        <wd-cell title="绑定用户" :value="device?.bindUserId ? String(device.bindUserId) : '未绑定'" />
      </wd-cell-group>

      <view class="mt-4">
        <wd-button type="warning" block @click="handleStatus(3)">停用设备</wd-button>
        <wd-button type="error" block custom-class="mt-2" @click="handleStatus(4)">设备报废</wd-button>
      </view>

      <view class="mt-4 text-gray-400 text-xs">
        远程锁定/擦除等管控由原生 device agent 执行；高危操作需二次确认。
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { getDevice, updateDeviceStatus, type DeviceVO } from '@/api/mdm/device'

definePage({ style: { navigationBarTitleText: '', navigationStyle: 'custom' } })

const toast = useToast()
const device = ref<DeviceVO | null>(null)
const deviceId = ref(0)

const statusName = (s: number) => ({ 0: '已注册', 1: '使用中', 2: '已挂失', 3: '已停用', 4: '已报废' } as any)[s] || '未知'
const statusType = (s: number): any => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger', 4: 'info' } as any)[s] || 'info'
const platformName = (p?: string) => ({ android: 'Android', ios: 'iOS', harmony: '鸿蒙', windows: 'Windows', macos: 'macOS' } as any)[p || ''] || p || '-'

onLoad((options) => {
  deviceId.value = Number(options?.id || 0)
  load()
})

async function load() {
  if (!deviceId.value) return
  device.value = await getDevice(deviceId.value)
}

async function handleStatus(status: number) {
  const confirm = await new Promise<boolean>((resolve) => {
    uni.showModal({
      title: '确认操作',
      content: `确认将设备设为「${statusName(status)}」？`,
      success: (res) => resolve(res.confirm),
      fail: () => resolve(false),
    })
  })
  if (!confirm) return
  await updateDeviceStatus(deviceId.value, status)
  toast.success('操作成功')
  await load()
}

function goBack() { uni.navigateBack() }
</script>
