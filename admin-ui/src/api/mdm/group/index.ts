import request from '@/config/axios'

// MDM 设备分组 VO
export interface GroupVO {
  id: number | undefined
  name: string
  parentId: number
  sort: number
  remark: string
}

// MDM 设备分组 API
export const createGroup = (data: GroupVO) => {
  return request.post({ url: '/mdm/group/create', data })
}

export const updateGroup = (data: GroupVO) => {
  return request.put({ url: '/mdm/group/update', data })
}

export const deleteGroup = (id: number) => {
  return request.delete({ url: '/mdm/group/delete?id=' + id })
}

export const getGroupListByParent = (parentId: number) => {
  return request.get({ url: '/mdm/group/list-by-parent?parentId=' + parentId })
}
