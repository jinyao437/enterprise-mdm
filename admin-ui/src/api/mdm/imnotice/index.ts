import request from '@/config/axios'

// MDM 业务 IM 通知 API
export const enqueueNotice = (params: {
  eventId: string
  messageType: number
  recipient: number
  title: string
  content: string
}) => request.post({
  url: '/mdm/im/notification/enqueue?eventId=' + encodeURIComponent(params.eventId)
    + '&messageType=' + params.messageType
    + '&recipient=' + params.recipient
    + '&title=' + encodeURIComponent(params.title)
    + '&content=' + encodeURIComponent(params.content)
})
export const listPendingNotices = () => request.get({ url: '/mdm/im/notification/pending' })
export const trySendNotice = (id: number) => request.post({ url: '/mdm/im/notification/try-send?id=' + id })
export const scanAndSend = () => request.post({ url: '/mdm/im/notification/scan-and-send' })
