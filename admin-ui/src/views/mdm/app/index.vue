<template>
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item>
        <el-button @click="getList"><Icon icon="ep:refresh" class="mr-5px" /> 刷新</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['mdm:app:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增应用
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="应用名称" align="center" prop="name" />
      <el-table-column label="包名" align="center" prop="packageName" />
      <el-table-column label="类型" align="center" prop="appType">
        <template #default="scope">{{ scope.row.appType === 'THIRD_PARTY' ? '三方接入' : '企业自建' }}</template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)">{{ statusName(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="260">
        <template #default="scope">
          <el-button link type="primary" @click="openVersion(scope.row.id)" v-hasPermi="['mdm:app:update']">版本</el-button>
          <el-button link type="primary" @click="openDistribute(scope.row)" v-hasPermi="['mdm:app:distribute']">分发</el-button>
          <el-button link type="warning" @click="handleOffline(scope.row.id)" v-hasPermi="['mdm:app:update']">下架</el-button>
          <el-button link type="danger" @click="handleStatus(scope.row.id, 2)" v-hasPermi="['mdm:app:update']">停用</el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <!-- 应用表单弹窗 -->
  <el-dialog v-model="formVisible" :title="formType === 'create' ? '新增应用' : '编辑应用'" width="600">
    <el-form :model="formData" label-width="90px">
      <el-form-item label="应用名称"><el-input v-model="formData.name" /></el-form-item>
      <el-form-item label="包名"><el-input v-model="formData.packageName" /></el-form-item>
      <el-form-item label="类型">
        <el-select v-model="formData.appType" class="!w-full">
          <el-option label="企业自建" value="INTERNAL" />
          <el-option label="三方接入" value="THIRD_PARTY" />
        </el-select>
      </el-form-item>
      <el-form-item label="描述"><el-input v-model="formData.description" type="textarea" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="formVisible = false">取消</el-button>
      <el-button type="primary" @click="submitForm">确定</el-button>
    </template>
  </el-dialog>

  <!-- 版本列表弹窗 -->
  <el-dialog v-model="versionVisible" title="应用版本" width="700">
    <el-button type="primary" plain class="mb-10px" @click="openVersionForm">新增版本</el-button>
    <el-table :data="versions" size="small">
      <el-table-column label="版本" prop="version" />
      <el-table-column label="版本码" prop="versionCode" />
      <el-table-column label="更新说明" prop="changelog" />
      <el-table-column label="强制更新">
        <template #default="scope">{{ scope.row.forceUpdate ? '是' : '否' }}</template>
      </el-table-column>
    </el-table>
    <el-form v-if="versionFormVisible" :model="versionForm" label-width="90px" class="mt-10px">
      <el-form-item label="版本"><el-input v-model="versionForm.version" /></el-form-item>
      <el-form-item label="版本码"><el-input-number v-model="versionForm.versionCode" :min="1" /></el-form-item>
      <el-form-item label="下载地址"><el-input v-model="versionForm.packageUrl" /></el-form-item>
      <el-form-item label="更新说明"><el-input v-model="versionForm.changelog" /></el-form-item>
      <el-form-item><el-button type="primary" @click="submitVersion">保存版本</el-button></el-form-item>
    </el-form>
  </el-dialog>

  <!-- 分发弹窗 -->
  <el-dialog v-model="distributeVisible" title="创建分发任务" width="600">
    <el-form :model="distributeForm" label-width="100px">
      <el-form-item label="版本">
        <el-select v-model="distributeForm.versionId" class="!w-full">
          <el-option v-for="v in versions" :key="v.id" :label="v.version" :value="v.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="范围">
        <el-select v-model="distributeForm.scopeType" class="!w-full">
          <el-option label="全量" value="ALL" />
          <el-option label="分组" value="GROUP" />
          <el-option label="设备" value="DEVICE" />
        </el-select>
      </el-form-item>
      <el-form-item label="目标编号"><el-input-number v-model="distributeForm.scopeId" :min="0" class="!w-full" /></el-form-item>
      <el-form-item label="灰度比例"><el-input-number v-model="distributeForm.grayPercent" :min="0" :max="100" class="!w-full" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="distributeVisible = false">取消</el-button>
      <el-button type="primary" @click="submitDistribution">提交</el-button>
    </template>
  </el-dialog>
</template>
<script lang="ts" setup>
import * as AppApi from '@/api/mdm/app'

defineOptions({ name: 'MdmApp' })
const message = useMessage()
const { t } = useI18n()

const statusName = (s: number) => ({ 0: '草稿', 1: '已上架', 2: '已下架' } as any)[s] || '未知'
const statusTag = (s: number) => ({ 0: 'info', 1: 'success', 2: 'danger' } as any)[s] || 'info'

const loading = ref(true)
const list = ref([])
const queryParams = reactive({})
const getList = async () => {
  loading.value = true
  try { list.value = await AppApi.getAppList() } finally { loading.value = false }
}

// 应用表单
const formVisible = ref(false)
const formType = ref('create')
const formData = ref({ id: undefined as number | undefined, name: '', packageName: '', appType: 'INTERNAL', description: '' })
const openForm = (type: string, row?: any) => {
  formType.value = type
  formData.value = row ? { id: row.id, name: row.name, packageName: row.packageName, appType: row.appType, description: row.description } : { id: undefined, name: '', packageName: '', appType: 'INTERNAL', description: '' }
  formVisible.value = true
}
const submitForm = async () => {
  try {
    if (formType.value === 'create') await AppApi.createApp(formData.value as any)
    else await AppApi.updateApp(formData.value as any)
    message.success(t('common.success')); formVisible.value = false; await getList()
  } catch {}
}
const handleStatus = async (id: number, status: number) => {
  try { await message.confirm('确认停用该应用？'); await AppApi.updateAppStatus(id, status); message.success(t('common.success')); await getList() } catch {}
}
const handleOffline = async (id: number) => {
  try { await message.confirm('确认下架该应用？'); await AppApi.updateAppStatus(id, 2); message.success(t('common.success')); await getList() } catch {}
}

// 版本
const versionVisible = ref(false)
const versionFormVisible = ref(false)
const versions = ref([])
const currentAppId = ref(0)
const versionForm = ref({ version: '', versionCode: 1, packageUrl: '', changelog: '', appId: 0 })
const openVersion = async (appId: number) => {
  currentAppId.value = appId
  versions.value = await AppApi.getVersionList(appId)
  versionVisible.value = true
}
const openVersionForm = () => {
  versionForm.value = { version: '', versionCode: 1, packageUrl: '', changelog: '', appId: currentAppId.value }
  versionFormVisible.value = true
}
const submitVersion = async () => {
  try { await AppApi.addVersion(versionForm.value); message.success(t('common.success')); versionFormVisible.value = false; versions.value = await AppApi.getVersionList(currentAppId.value) } catch {}
}

// 分发
const distributeVisible = ref(false)
const distributeForm = ref({ versionId: undefined as number | undefined, scopeType: 'ALL', scopeId: 0, grayPercent: 100, appId: 0 })
const openDistribute = async (row: any) => {
  versions.value = await AppApi.getVersionList(row.id)
  distributeForm.value = { versionId: undefined, scopeType: 'ALL', scopeId: 0, grayPercent: 100, appId: row.id }
  distributeVisible.value = true
}
const submitDistribution = async () => {
  try { await AppApi.createDistribution(distributeForm.value); message.success(t('common.success')); distributeVisible.value = false } catch {}
}

onMounted(() => getList())
</script>
