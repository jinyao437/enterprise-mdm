import request from '@/config/axios'

// MDM 企业应用 VO
export interface AppVO {
  id: number | undefined
  name: string
  packageName: string
  appType: string
  status: number
  icon: string
  description: string
  openAppId: string
}

// MDM 应用分发 API
export const getAppList = () => request.get({ url: '/mdm/app/list' })
export const createApp = (data: AppVO) => request.post({ url: '/mdm/app/create', data })
export const updateApp = (data: AppVO) => request.put({ url: '/mdm/app/update', data })
export const updateAppStatus = (id: number, status: number) => request.put({ url: '/mdm/app/status?id=' + id + '&status=' + status })
export const addVersion = (data: any) => request.post({ url: '/mdm/app/version/add', data })
export const getVersionList = (appId: number) => request.get({ url: '/mdm/app/version/list?appId=' + appId })
export const createDistribution = (data: any) => request.post({ url: '/mdm/app/distribution/create', data })
export const pauseDistribution = (id: number) => request.put({ url: '/mdm/app/distribution/pause?id=' + id })
