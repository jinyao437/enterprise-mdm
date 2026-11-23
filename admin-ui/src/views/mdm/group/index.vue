<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="80px">
      <el-form-item label="父分组" prop="parentId">
        <el-input-number v-model="queryParams.parentId" :min="0" controls-position="right" class="!w-240px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')" v-hasPermi="['mdm:group:create']">
          <Icon icon="ep:plus" class="mr-5px" /> 新增分组
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="分组编号" align="center" prop="id" />
      <el-table-column label="分组名称" align="center" prop="name" />
      <el-table-column label="父分组" align="center" prop="parentId" />
      <el-table-column label="排序" align="center" prop="sort" />
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="操作" align="center" width="160">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('update', scope.row)" v-hasPermi="['mdm:group:update']">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="['mdm:group:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>

  <GroupForm ref="formRef" @success="getList" />
</template>
<script lang="ts" setup>
import * as GroupApi from '@/api/mdm/group'
import GroupForm from './GroupForm.vue'

defineOptions({ name: 'MdmGroup' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const list = ref([])
const queryParams = reactive({ parentId: 0 })
const queryFormRef = ref()

const getList = async () => {
  loading.value = true
  try {
    list.value = await GroupApi.getGroupListByParent(queryParams.parentId)
  } finally {
    loading.value = false
  }
}

const handleQuery = () => getList()
const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, row?: GroupApi.GroupVO) => {
  formRef.value.open(type, row)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await GroupApi.deleteGroup(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(() => getList())
</script>
