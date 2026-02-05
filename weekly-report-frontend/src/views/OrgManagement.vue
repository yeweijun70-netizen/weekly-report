<template>
  <div class="org-management">
    <div class="card">
      <div class="page-header">
        <h2><el-icon><OfficeBuilding /></el-icon> 组织机构管理</h2>
        <div>
          <el-button type="primary" @click="openDialog(null)">
            <el-icon><Plus /></el-icon>新增根部门
          </el-button>
        </div>
      </div>
      <el-input v-model="filterText" placeholder="搜索部门名称..." clearable class="tree-search">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-tree
        ref="treeRef"
        :data="treeData"
        :props="{ label: 'name', children: 'children' }"
        :filter-node-method="filterNode"
        node-key="id"
        default-expand-all
        highlight-current
      >
        <template #default="{ node, data }">
          <span class="tree-node">
            <el-icon v-if="data.children && data.children.length"><FolderOpened /></el-icon>
            <el-icon v-else><Folder /></el-icon>
            <span>{{ node.label }}</span>
            <span class="node-actions">
              <el-button link type="primary" size="small" @click.stop="openDialog(data, true)">添加子部门</el-button>
              <el-button link type="primary" size="small" @click.stop="openDialog(data)">编辑</el-button>
              <el-button link type="danger" size="small" @click.stop="handleDelete(data)">删除</el-button>
            </span>
          </span>
        </template>
      </el-tree>
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑部门' : (form.parentId ? '新增子部门' : '新增根部门')" width="400px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="部门名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDepartmentTree, saveDepartment, deleteDepartment } from '@/api'
import { Folder, FolderOpened } from '@element-plus/icons-vue'

const treeRef = ref()
const formRef = ref()
const filterText = ref('')
const treeData = ref([])
const dialogVisible = ref(false)
const submitting = ref(false)

const form = reactive({
  id: null,
  name: '',
  parentId: 0,
  sort: 0
})

const rules = {
  name: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

watch(filterText, (val) => {
  treeRef.value?.filter(val)
})

function filterNode(value, data) {
  if (!value) return true
  return data.name.includes(value)
}

async function loadTree() {
  try {
    const res = await getDepartmentTree({ silent: true })
    if (res?.code === 200) treeData.value = res.data || []
  } catch (_) {}
}

function openDialog(row, asChild = false) {
  if (row && asChild) {
    form.id = null
    form.name = ''
    form.parentId = row.id
    form.sort = 0
  } else if (row) {
    form.id = row.id
    form.name = row.name
    form.parentId = row.parentId ?? 0
    form.sort = row.sort ?? 0
  } else {
    form.id = null
    form.name = ''
    form.parentId = 0
    form.sort = 0
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await saveDepartment(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadTree()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定要删除部门「${row.name}」吗？如有子部门将一并考虑。`, '提示', { type: 'warning' })
  try {
    await deleteDepartment(row.id)
    ElMessage.success('删除成功')
    loadTree()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '删除失败')
  }
}

onMounted(() => loadTree())
</script>

<style lang="scss" scoped>
.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  h2 { font-size: 18px; color: #303133; display: flex; align-items: center; gap: 8px; }
}
.tree-search { margin-bottom: 16px; }
.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 6px;
  .node-actions { margin-left: 8px; opacity: 0.8; }
}
</style>
