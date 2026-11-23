<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle" width="600">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="90px">
      <el-form-item label="分组名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入分组名称" />
      </el-form-item>
      <el-form-item label="父分组" prop="parentId">
        <el-input-number v-model="formData.parentId" :min="0" controls-position="right" class="!w-full" />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" controls-position="right" class="!w-full" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" placeholder="请输入备注" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>
<script lang="ts" setup>
import * as GroupApi from '@/api/mdm/group'

defineOptions({ name: 'MdmGroupForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref({
  id: undefined as number | undefined,
  name: '',
  parentId: 0,
  sort: 0,
  remark: ''
})
const formRules = reactive({
  name: [{ required: true, message: '分组名称不能为空', trigger: 'blur' }]
})
const formRef = ref()

const open = async (type: string, row?: GroupApi.GroupVO) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  if (row) {
    formData.value = { ...row }
  } else {
    formData.value = { id: undefined, name: '', parentId: 0, sort: 0, remark: '' }
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
    const data = formData.value as unknown as GroupApi.GroupVO
    if (formType.value === 'create') {
      await GroupApi.createGroup(data)
      message.success(t('common.createSuccess'))
    } else {
      await GroupApi.updateGroup(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>
