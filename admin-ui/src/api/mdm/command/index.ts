import request from '@/config/axios'

// 下发远程指令
export const dispatchCommand = (params: {
  deviceId: number
  type: number
  params?: string
  idempotencyKey: string
  confirmToken?: string
}) => request.post({
  url: '/mdm/command/dispatch?deviceId=' + params.deviceId
    + '&type=' + params.type
    + '&idempotencyKey=' + encodeURIComponent(params.idempotencyKey)
    + (params.params ? '&params=' + encodeURIComponent(params.params) : '')
    + (params.confirmToken ? '&confirmToken=' + encodeURIComponent(params.confirmToken) : '')
})
export const getCommand = (id: number) => request.get({ url: '/mdm/command/get?id=' + id })
