<template>
  <ContentWrap>
    <el-form class="-mb-15px" :inline="true">
      <el-form-item>
        <el-button @click="getList"><Icon icon="ep:refresh" class="mr-5px" /> 刷新</el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="设备编号" align="center" prop="deviceId" />
      <el-table-column label="检查项" align="center" prop="checkItem" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)">{{ statusName(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="详情" align="center" prop="detail" show-overflow-tooltip />
      <el-table-column label="已告警" align="center" prop="alerted">
        <template #default="scope">{{ scope.row.alerted ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="上报时间" align="center" prop="reportTime" width="180" :formatter="dateFormatter" />
      <el-table-column label="操作" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="handleAlerted(scope.row.id)" v-hasPermi="['mdm:compliance:update']">标记已告警</el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
</template>
<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import * as ComplianceApi from '@/api/mdm/compliance'

defineOptions({ name: 'MdmCompliance' })
const message = useMessage()
const { t } = useI18n()

const statusName = (s: number) => ({ 0: '合规', 1: '不合规', 2: '待复核' } as any)[s] || '未知'
const statusTag = (s: number) => ({ 0: 'success', 1: 'danger', 2: 'warning' } as any)[s] || 'info'

const loading = ref(true)
const list = ref([])
const getList = async () => {
  loading.value = true
  try { list.value = await ComplianceApi.listUnalerted() } finally { loading.value = false }
}
const handleAlerted = async (id: number) => {
  try { await ComplianceApi.markAlerted(id); message.success(t('common.success')); await getList() } catch {}
}
onMounted(() => getList())
</script>
