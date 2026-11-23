import { http } from '@/http/http'

/** MDM 设备（对齐 enterprise-module-mdm DeviceRespVO） */
export interface DeviceVO {
  id: number
  identifier: string
  name: string
  platform: string
  osVersion?: string
  status: number
  statusName?: string
  bindUserId?: number
  groupId?: number
  createTime?: string
}

/** 分页查询我的设备 */
export function getDevicePage(params: { pageNo: number, pageSize: number, name?: string }) {
  return http.get<{ list: DeviceVO[], total: number }>('/mdm/device/page', params)
}

/** 设备详情 */
export function getDevice(id: number) {
  return http.get<DeviceVO>('/mdm/device/get', { id })
}

/** 更新设备状态（停用/报废等） */
export function updateDeviceStatus(id: number, status: number) {
  return http.put<boolean>('/mdm/device/status', null, { id, status })
}
