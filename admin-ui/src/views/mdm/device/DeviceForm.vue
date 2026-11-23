<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="700">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="90px"
    >
      <el-form-item label="设备标识" prop="identifier">
        <el-input v-model="formData.identifier" placeholder="请输入 IMEI / SN / UUID" :disabled="formType === 'update'" />
      </el-form-item>
      <el-form-item label="设备名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入设备名称" />
      </el-form-item>
      <el-form-item label="平台" prop="platform">
        <el-select v-model="formData.platform" clearable placeholder="请选择平台" class="!w-full">
          <el-option v-for="item in PLATFORM_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="系统版本" prop="osVersion">
        <el-input v-model="formData.osVersion" placeholder="如 Android 14" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="formData.status" clearable placeholder="请选择状态" class="!w-full">
          <el-option v-for="item in STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="绑定用户" prop="bindUserId">
        <el-input-number v-model="formData.bindUserId" :min="0" controls-position="right" class="!w-full" placeholder="芋道系统用户编号" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import * as DeviceApi from '@/api/mdm/device'

defineOptions({ name: 'MdmDeviceForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

// 与后端 DeviceStatusEnum / 平台约定一致
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

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined as number | undefined,
  identifier: '',
  name: '',
  platform: 'android',
  osVersion: '',
  status: 0,
  bindUserId: undefined as number | undefined,
  groupId: undefined as number | undefined
})
const formRules = reactive({
  identifier: [{ required: true, message: '设备标识不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
  platform: [{ required: true, message: '平台不能为空', trigger: 'change' }],
  status: [{ required: true, message: '状态不能为空', trigger: 'change' }]
})
const formRef = ref()

/** 打开弹窗 */
const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await DeviceApi.getDevice(id) as any
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    identifier: '',
    name: '',
    platform: 'android',
    osVersion: '',
    status: 0,
    bindUserId: undefined,
    groupId: undefined
  }
  formRef.value?.resetFields()
}

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    const data = formData.value as unknown as DeviceApi.DeviceVO
    if (formType.value === 'create') {
      await DeviceApi.createDevice(data)
      message.success(t('common.createSuccess'))
    } else {
      await DeviceApi.updateDevice(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>
