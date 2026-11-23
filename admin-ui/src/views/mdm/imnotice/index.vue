<template>
  <ContentWrap>
    <el-form class="-mb-15px" :inline="true" label-width="90px">
      <el-form-item>
        <el-button @click="getList"><Icon icon="ep:refresh" class="mr-5px" /> 刷新待发</el-button>
        <el-button type="primary" @click="handleScan" v-hasPermi="['mdm:im:notify']">
          <Icon icon="ep:promotion" class="mr-5px" /> 扫描并发送
        </el-button>
      </el-form-item>
    </el-form>
    <el-alert type="info" :closable="false" title="业务通知经 IM 适配层发送；未配置野火服务端时发送会失败并计入重试，由定时任务逐租户续发。" />
  </ContentWrap>

  <ContentWrap>
    <el-form :inline="true" label-width="90px">
      <el-form-item label="事件编号"><el-input v-model="form.eventId" class="!w-180px" /></el-form-item>
      <el-form-item label="消息类型">
        <el-select v-model="form.messageType" class="!w-200px">
          <el-option v-for="o in TYPES" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="接收人"><el-input-number v-model="form.recipient" :min="1" class="!w-160px" /></el-form-item>
      <el-form-item label="标题"><el-input v-model="form.title" class="!w-200px" /></el-form-item>
      <el-form-item label="内容"><el-input v-model="form.content" class="!w-260px" /></el-form-item>
      <el-form-item><el-button type="primary" @click="handleEnqueue" v-hasPermi="['mdm:im:notify']">提交通知</el-button></el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="事件编号" align="center" prop="eventId" />
      <el-table-column label="消息类型" align="center" prop="messageType">
        <template #default="scope">{{ typeName(scope.row.messageType) }}</template>
      </el-table-column>
      <el-table-column label="接收人" align="center" prop="recipient" />
      <el-table-column label="标题" align="center" prop="title" />
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)">{{ statusName(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="重试" align="center" prop="retryCount" />
      <el-table-column label="操作" align="center">
        <template #default="scope">
          <el-button link type="primary" @click="handleTrySend(scope.row.id)" v-hasPermi="['mdm:im:notify']">发送</el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
</template>
<script lang="ts" setup>
import * as NoticeApi from '@/api/mdm/imnotice'

defineOptions({ name: 'MdmImNotice' })
const message = useMessage()
const { t } = useI18n()

const TYPES = [
  { value: 1, label: '审批结果' },
  { value: 2, label: '设备告警' },
  { value: 3, label: '策略变更' },
  { value: 4, label: '合规告警' },
  { value: 5, label: '应用分发' }
]
const typeName = (v: number) => TYPES.find((o) => o.value === v)?.label || '未知'
const statusName = (v: number) => ({ 0: '待发送', 1: '已送达', 2: '发送失败' } as any)[v] || '未知'
const statusTag = (v: number) => ({ 0: 'warning', 1: 'success', 2: 'danger' } as any)[v] || 'info'

const loading = ref(true)
const list = ref([])
const getList = async () => {
  loading.value = true
  try { list.value = await NoticeApi.listPendingNotices() } finally { loading.value = false }
}
const form = ref({ eventId: '', messageType: 1, recipient: undefined as number | undefined, title: '', content: '' })
const handleEnqueue = async () => {
  if (!form.value.eventId || !form.value.recipient) {
    message.error('事件编号与接收人必填')
    return
  }
  try {
    await NoticeApi.enqueueNotice(form.value as any)
    message.success(t('common.success')); await getList()
  } catch {}
}
const handleTrySend = async (id: number) => {
  try { await NoticeApi.trySendNotice(id); message.success(t('common.success')); await getList() } catch {}
}
const handleScan = async () => {
  try { const n = await NoticeApi.scanAndSend(); message.success('本次成功发送 ' + n + ' 条'); await getList() } catch {}
}
onMounted(() => getList())
</script>
