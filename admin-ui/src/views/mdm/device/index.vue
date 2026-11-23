<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="设备标识" prop="identifier">
        <el-input v-model="queryParams.identifier" placeholder="请输入设备标识" clearable @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="设备名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入设备名称" clearable @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>
      <el-form-item label="平台" prop="platform">
        <el-select v-model="queryParams.platform" placeholder="请选择平台" clearable class="!w-240px">
          <el-option v-for="item in PLATFORM_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable class="!w-240px">
          <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['mdm:device:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 注册设备
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="设备编号" align="center" prop="id" />
      <el-table-column label="设备标识" align="center" prop="identifier" />
      <el-table-column label="设备名称" align="center" prop="name" />
      <el-table-column label="平台" align="center" prop="platform">
        <template #default="scope">
          {{ platformName(scope.row.platform) }}
        </template>
      </el-table-column>
      <el-table-column label="系统版本" align="center" prop="osVersion" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusName(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="绑定用户" align="center" prop="bindUserId" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180" :formatter="dateFormatter" />
      <el-table-column label="操作" align="center" width="200">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row.id)" v-hasPermi="['mdm:device:update']">编辑</el-button>
          <el-button link type="warning" @click="handleStatus(scope.row.id, 3)" v-hasPermi="['mdm:device:update']">停用</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['mdm:device:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination :total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <DeviceForm ref="formRef" @success="getList" />
</template>
<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as DeviceApi from '@/api/mdm/device'
import DeviceForm from './DeviceForm.vue'

defineOptions({ name: 'MdmDevice' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化

const STATUS_OPTIONS = [
  { value: 0, label: '已注册' },
  { value: 1, label: '使用中' },
  { value: 2, label: '已挂失' },
  { value: 3, label: '已停用' },
  { value: 4, label: '已报废' }
]
const PLATFORM_OPTIONS = [
  { value: 'android', label: 'Android' },
  { value: 'ios', label: 'iOS' },
  { value: 'harmony', label: '鸿蒙' },
  { value: 'windows', label: 'Windows' },
  { value: 'macos', label: 'macOS' }
]
const statusName = (status: number) => STATUS_OPTIONS.find((o) => o.value === status)?.label || '未知'
const statusTagType = (status: number): any => {
  const map: Record<number, string> = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger', 4: 'info' }
  return map[status] || 'info'
}
const platformName = (platform: string) => PLATFORM_OPTIONS.find((o) => o.value === platform)?.label || platform

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  identifier: '',
  name: '',
  platform: undefined as string | undefined,
  status: undefined as number | undefined
})
const queryFormRef = ref()

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await DeviceApi.getDevicePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

/** 添加/修改 */
const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

/** 删除 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await DeviceApi.deleteDevice(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

/** 状态变更（如停用） */
const handleStatus = async (id: number, status: number) => {
  try {
    await message.confirm('确认要变更设备状态吗？')
    await DeviceApi.updateDeviceStatus(id, status)
    message.success(t('common.updateSuccess'))
    await getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>
