<template>
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="生效范围" prop="scopeType">
        <el-select v-model="queryParams.scopeType" class="!w-200px">
          <el-option label="分组" value="GROUP" />
          <el-option label="设备" value="DEVICE" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标编号" prop="scopeId">
        <el-input-number v-model="queryParams.scopeId" :min="0" controls-position="right" class="!w-200px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 查询</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['mdm:policy:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增策略
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="策略编号" align="center" prop="id" />
      <el-table-column label="策略名称" align="center" prop="name" />
      <el-table-column label="策略类型" align="center" prop="type">
        <template #default="scope">
          {{ typeName(scope.row.type) }}
        </template>
      </el-table-column>
      <el-table-column label="生效范围" align="center">
        <template #default="scope">
          {{ scopeName(scope.row.scopeType) }} / {{ scope.row.scopeId }}
        </template>
      </el-table-column>
      <el-table-column label="优先级" align="center" prop="priority" />
      <el-table-column label="启用" align="center" prop="enabled">
        <template #default="scope">
          <el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row)" v-hasPermi="['mdm:policy:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['mdm:policy:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <PolicyForm ref="formRef" @success="handleQuery" />
</template>
<script lang="ts" setup>
import * as PolicyApi from '@/api/mdm/policy'
import PolicyForm from './PolicyForm.vue'

defineOptions({ name: 'MdmPolicy' })

const message = useMessage()
const { t } = useI18n()

const TYPE_OPTIONS = [
  { value: 1, label: '安全策略' },
  { value: 2, label: '网络策略' },
  { value: 3, label: '应用策略' },
  { value: 4, label: '内容策略' }
]
const typeName = (type: number) => TYPE_OPTIONS.find((o) => o.value === type)?.label || '未知'
const scopeName = (scope: string) => (scope === 'DEVICE' ? '设备' : '分组')

const loading = ref(true)
const list = ref([])
const queryParams = reactive({ scopeType: 'GROUP', scopeId: 0 })
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    list.value = await PolicyApi.getEffectivePolicies(queryParams.scopeType, queryParams.scopeId)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => getList()

const formRef = ref()
const openForm = (type: string, row?: PolicyApi.PolicyVO) => {
  formRef.value.open(type, row)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await PolicyApi.deletePolicy(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(() => getList())
</script>
