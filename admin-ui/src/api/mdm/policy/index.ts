import request from '@/config/axios'

// MDM 设备策略 VO
export interface PolicyVO {
  id: number | undefined
  name: string
  type: number
  content: string
  scopeType: string
  scopeId: number
  priority: number
  enabled: boolean
}

// MDM 设备策略 API
export const createPolicy = (data: PolicyVO) => {
  return request.post({ url: '/mdm/policy/create', data })
}

export const updatePolicy = (data: PolicyVO) => {
  return request.put({ url: '/mdm/policy/update', data })
}

export const deletePolicy = (id: number) => {
  return request.delete({ url: '/mdm/policy/delete?id=' + id })
}

export const getEffectivePolicies = (scopeType: string, scopeId: number) => {
  return request.get({ url: '/mdm/policy/effective?scopeType=' + scopeType + '&scopeId=' + scopeId })
}
