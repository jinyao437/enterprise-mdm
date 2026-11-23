<template>
  <ContentWrap>
    <el-form class="-mb-15px" :inline="true" label-width="90px">
      <el-form-item label="设备编号">
        <el-input-number v-model="form.deviceId" :min="1" class="!w-200px" />
      </el-form-item>
      <el-form-item label="指令类型">
        <el-select v-model="form.type" class="!w-200px">
          <el-option v-for="o in TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="幂等键">
        <el-input v-model="form.idempotencyKey" placeholder="业务唯一编号" class="!w-200px" />
      </el-form-item>
      <el-form-item label="确认令牌" v-if="isDangerous">
        <el-input v-model="form.confirmToken" placeholder="高危指令必填" class="!w-200px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleDispatch" v-hasPermi="['mdm:command:dispatch']">
          <Icon icon="ep:promotion" class="mr-5px" /> 下发指令
        </el-button>
      </el-form-item>
    </el-form>
    <el-alert v-if="isDangerous" type="warning" :closable="false" title="远程擦除为高危指令，必须提供预发放的二次确认令牌，否则后端拒绝下发。" />
  </ContentWrap>

  <ContentWrap>
    <el-form class="-mb-15px" :inline="true">
      <el-form-item label="查询指令编号">
        <el-input-number v-model="queryId" :min="1" class="!w-200px" />
        <el-button class="ml-10px" @click="handleGet">查询</el-button>
      </el-form-item>
    </el-form>
    <el-descriptions v-if="command" :column="2" border>
      <el-descriptions-item label="编号">{{ command.id }}</el-descriptions-item>
      <el-descriptions-item label="设备编号">{{ command.deviceId }}</el-descriptions-item>
      <el-descriptions-item label="指令类型">{{ typeName(command.type) }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ statusName(command.status) }}</el-descriptions-item>
      <el-descriptions-item label="幂等键">{{ command.idempotencyKey }}</el-descriptions-item>
      <el-descriptions-item label="下发次数">{{ command.dispatchAttempts }}</el-descriptions-item>
    </el-descriptions>
  </ContentWrap>
</template>
<script lang="ts" setup>
import * as CommandApi from '@/api/mdm/command'

defineOptions({ name: 'MdmCommand' })
const message = useMessage()
const { t } = useI18n()

const TYPE_OPTIONS = [
  { value: 1, label: '远程锁定' },
  { value: 2, label: '远程擦除（高危）' },
  { value: 3, label: '定位' },
  { value: 4, label: '应用推送' },
  { value: 5, label: '策略刷新' }
]
const typeName = (v: number) => TYPE_OPTIONS.find((o) => o.value === v)?.label || '未知'
const statusName = (v: number) => ({ 0: '已创建', 1: '已下发', 2: '设备已确认', 3: '下发失败' } as any)[v] || '未知'
const isDangerous = computed(() => form.value.type === 2)

const form = ref({ deviceId: undefined as number | undefined, type: 1, idempotencyKey: '', confirmToken: '', params: '' })
const handleDispatch = async () => {
  if (!form.value.deviceId || !form.value.idempotencyKey) {
    message.error('设备编号与幂等键必填')
    return
  }
  try {
    await CommandApi.dispatchCommand({
      deviceId: form.value.deviceId,
      type: form.value.type,
      idempotencyKey: form.value.idempotencyKey,
      params: form.value.params || undefined,
      confirmToken: form.value.confirmToken || undefined
    })
    message.success(t('common.success'))
  } catch {}
}

const queryId = ref(0)
const command = ref<any>(null)
const handleGet = async () => {
  if (!queryId.value) return
  try { command.value = await CommandApi.getCommand(queryId.value) } catch {}
}
</script>
