import request from '@/config/axios'

// MDM 开放平台 API
export const registerOpenApp = (data: any) => request.post({ url: '/mdm/open/app/register', data })
export const updateOpenAppStatus = (id: number, status: number) => request.put({ url: '/mdm/open/app/status?id=' + id + '&status=' + status })
export const setQuota = (data: any) => request.post({ url: '/mdm/open/quota', data })
export const listQuotas = (appId: number) => request.get({ url: '/mdm/open/quota/list?appId=' + appId })
export const subscribeWebhook = (data: any) => request.post({ url: '/mdm/open/webhook/subscribe', data })
export const listSubscribers = (event: string) => request.get({ url: '/mdm/open/webhook/subscribers?event=' + event })
