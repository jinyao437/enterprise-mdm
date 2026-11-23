import request from '@/config/axios'

// MDM 设备 VO
export interface DeviceVO {
  id: number | undefined
  identifier: string
  name: string
  platform: string
  osVersion: string
  status: number
  statusName: string
  bindUserId: number
  groupId: number
  createTime: Date
}

// MDM 设备 API（对接 enterprise-module-mdm 的 /mdm/device 接口）
// 按芋道范式用具名函数导出，页面用 import * as DeviceApi 调用
export const getDevicePage = (params: any) => {
  return request.get({ url: '/mdm/device/page', params })
}

export const getDevice = (id: number) => {
  return request.get({ url: '/mdm/device/get?id=' + id })
}

export const createDevice = (data: DeviceVO) => {
  return request.post({ url: '/mdm/device/create', data })
}

export const updateDevice = (data: DeviceVO) => {
  return request.put({ url: '/mdm/device/update', data })
}

export const deleteDevice = (id: number) => {
  return request.delete({ url: '/mdm/device/delete?id=' + id })
}

export const updateDeviceStatus = (id: number, status: number) => {
  return request.put({ url: '/mdm/device/status?id=' + id + '&status=' + status })
}
