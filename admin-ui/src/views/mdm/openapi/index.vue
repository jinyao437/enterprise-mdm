<template>
  <ContentWrap>
    <el-tabs v-model="activeTab">
      <!-- 应用注册 -->
      <el-tab-pane label="应用注册" name="app">
        <el-form :inline="true" label-width="90px">
          <el-form-item label="应用名称"><el-input v-model="appForm.name" class="!w-200px" /></el-form-item>
          <el-form-item label="appKey"><el-input v-model="appForm.appKey" class="!w-200px" /></el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleRegister" v-hasPermi="['mdm:open:app']">注册应用</el-button>
          </el-form-item>
        </el-form>
        <el-alert type="info" :closable="false" title="鉴权复用芋道底座 OAuth2（client_credentials）：appKey 对应 OAuth2 clientId，secret 只存哈希。" />
      </el-tab-pane>

      <!-- API 配额 -->
      <el-tab-pane label="API 配额" name="quota">
        <el-form :inline="true" label-width="90px">
          <el-form-item label="应用编号"><el-input-number v-model="quotaForm.appId" :min="1" class="!w-160px" /></el-form-item>
          <el-form-item label="接口"><el-input v-model="quotaForm.apiPattern" placeholder="如 device:list 或 *" class="!w-200px" /></el-form-item>
          <el-form-item label="QPS"><el-input-number v-model="quotaForm.qps" :min="1" class="!w-140px" /></el-form-item>
          <el-form-item label="日限"><el-input-number v-model="quotaForm.dailyLimit" :min="1" class="!w-160px" /></el-form-item>
          <el-form-item><el-button type="primary" @click="handleQuota" v-hasPermi="['mdm:open:quota']">设置配额</el-button></el-form-item>
        </el-form>
        <el-form :inline="true" label-width="90px">
          <el-form-item label="查询应用"><el-input-number v-model="queryAppId" :min="1" class="!w-160px" /></el-form-item>
          <el-form-item><el-button @click="loadQuotas">查询</el-button></el-form-item>
        </el-form>
        <el-table :data="quotas" size="small">
          <el-table-column label="接口" prop="apiPattern" />
          <el-table-column label="QPS" prop="qps" />
          <el-table-column label="日限" prop="dailyLimit" />
          <el-table-column label="启用">
            <template #default="scope">{{ scope.row.enabled ? '是' : '否' }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- Webhook -->
      <el-tab-pane label="Webhook" name="webhook">
        <el-form :inline="true" label-width="90px">
          <el-form-item label="应用编号"><el-input-number v-model="webhookForm.appId" :min="1" class="!w-160px" /></el-form-item>
          <el-form-item label="事件">
            <el-select v-model="webhookForm.event" class="!w-200px">
              <el-option v-for="o in EVENTS" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="回调地址"><el-input v-model="webhookForm.url" class="!w-260px" /></el-form-item>
          <el-form-item><el-button type="primary" @click="handleSubscribe" v-hasPermi="['mdm:open:webhook']">订阅</el-button></el-form-item>
        </el-form>
        <el-table :data="subscribers" size="small">
          <el-table-column label="事件" prop="event" />
          <el-table-column label="回调地址" prop="url" show-overflow-tooltip />
          <el-table-column label="启用">
            <template #default="scope">{{ scope.row.enabled ? '是' : '否' }}</template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </ContentWrap>
</template>
<script lang="ts" setup>
import * as OpenApi from '@/api/mdm/openapi'

defineOptions({ name: 'MdmOpenPlatform' })
const message = useMessage()
const { t } = useI18n()

const EVENTS = [
  { value: 'device.registered', label: '设备注册' },
  { value: 'device.alert', label: '设备告警' },
  { value: 'compliance.violation', label: '合规违规' },
  { value: 'approval.result', label: '审批结果' },
  { value: 'app.distribution', label: '应用分发' }
]
const activeTab = ref('app')

const appForm = ref({ name: '', appKey: '', secretHash: '', status: 1, description: '' })
const handleRegister = async () => {
  try { await OpenApi.registerOpenApp(appForm.value); message.success(t('common.success')); appForm.value = { name: '', appKey: '', secretHash: '', status: 1, description: '' } } catch {}
}

const quotaForm = ref({ appId: 0, apiPattern: '*', qps: 10, dailyLimit: 10000, enabled: true })
const handleQuota = async () => {
  try { await OpenApi.setQuota(quotaForm.value); message.success(t('common.success')) } catch {}
}
const queryAppId = ref(0)
const quotas = ref([])
const loadQuotas = async () => {
  if (!queryAppId.value) return
  try { quotas.value = await OpenApi.listQuotas(queryAppId.value) } catch {}
}

const webhookForm = ref({ appId: 0, event: 'device.alert', url: '', secretEnc: '', enabled: true })
const subscribers = ref([])
const handleSubscribe = async () => {
  try { await OpenApi.subscribeWebhook(webhookForm.value); message.success(t('common.success')); subscribers.value = await OpenApi.listSubscribers(webhookForm.value.event) } catch {}
}
</script>
