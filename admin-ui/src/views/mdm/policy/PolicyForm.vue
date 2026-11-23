<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="650">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="100px">
      <el-form-item label="策略名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入策略名称" />
      </el-form-item>
      <el-form-item label="策略类型" prop="type">
        <el-select v-model="formData.type" placeholder="请选择类型" class="!w-full">
          <el-option v-for="item in TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="生效范围" prop="scopeType">
        <el-select v-model="formData.scopeType" placeholder="请选择范围" class="!w-full">
          <el-option label="分组" value="GROUP" />
          <el-option label="设备" value="DEVICE" />
        </el-select>
      </el-form-item>
      <el-form-item label="范围目标" prop="scopeId">
        <el-input-number v-model="formData.scopeId" :min="0" controls-position="right" class="!w-full" />
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-input-number v-model="formData.priority" controls-position="right" class="!w-full" />
      </el-form-item>
      <el-form-item label="策略内容" prop="content">
        <el-input v-model="formData.content" placeholder="请输入 JSON 格式策略配置，如 passwordMinLength 等" type="textarea" />
      </el-form-item>
      <el-form-item label="启用" prop="enabled">
        <el-switch v-model="formData.enabled" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import * as PolicyApi from '@/api/mdm/policy'

defineOptions({ name: 'MdmPolicyForm' })

const { t } = useI18n()
const message = useMessage()

const TYPE_OPTIONS = [
  { value: 1, label: '安全策略' },
  { value: 2, label: '网络策略' },
  { value: 3, label: '应用策略' },
  { value: 4, label: '内容策略' }
]

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined as number | undefined,
  name: '',
  type: 1,
  content: '',
  scopeType: 'GROUP',
  scopeId: 0,
  priority: 0,
  enabled: true
})
const formRules = reactive({
  name: [{ required: true, message: '策略名称不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '策略类型不能为空', trigger: 'change' }],
  scopeType: [{ required: true, message: '生效范围不能为空', trigger: 'change' }]
})
const formRef = ref()

const open = async (type: string, row?: PolicyApi.PolicyVO) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  if (row) {
    formData.value = { ...row }
  } else {
    formData.value = { id: undefined, name: '', type: 1, content: '', scopeType: 'GROUP', scopeId: 0, priority: 0, enabled: true }
  }
  formRef.value?.resetFields()
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    const data = formData.value as unknown as PolicyApi.PolicyVO
    if (formType.value === 'create') {
      await PolicyApi.createPolicy(data)
      message.success(t('common.createSuccess'))
    } else {
      await PolicyApi.updatePolicy(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>
