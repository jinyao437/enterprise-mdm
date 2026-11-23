import request from '@/config/axios'

// MDM 合规上报 VO
export interface ComplianceVO {
  id: number | undefined
  deviceId: number
  checkItem: string
  status: number
  detail: string
  alerted: boolean
  reportTime: Date
}

// MDM 合规检查 API
export const reportCompliance = (data: ComplianceVO) => request.post({ url: '/mdm/compliance/report', data })
export const listUnalerted = () => request.get({ url: '/mdm/compliance/unalerted' })
export const markAlerted = (id: number) => request.put({ url: '/mdm/compliance/alerted?id=' + id })
