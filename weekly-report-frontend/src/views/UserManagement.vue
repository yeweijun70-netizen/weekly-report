<template>
  <div class="user-management">
    <div class="card">
      <div class="page-header">
        <h2>用户管理</h2>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>新增用户
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input v-model="searchKeyword" placeholder="搜索用户名/邮箱..." style="width: 200px" clearable @clear="loadData" @keyup.enter="loadData" />
        <el-tree-select 
          v-model="searchDept" 
          :data="departmentTree" 
          :props="{ label: 'name', value: 'id', children: 'children' }"
          placeholder="全部部门" 
          clearable 
          check-strictly
          style="width: 180px" 
          @change="loadData"
        />
        <el-select v-model="searchRole" placeholder="全部角色" clearable style="width: 150px" @change="loadData">
          <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.id" />
        </el-select>
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="users" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="departmentName" label="部门" width="150" />
        <el-table-column prop="roleName" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.roleId)">{{ row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" layout="total, prev, pager, next" @current-change="loadData" style="margin-top: 16px; justify-content: flex-end;" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!form.id">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="部门">
          <el-tree-select 
            v-model="form.departmentId" 
            :data="departmentTree" 
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="请选择部门" 
            check-strictly
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserPage, saveUser, updateUser, deleteUser, getRoleList, getDepartmentTree } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const formRef = ref()

const users = ref([])
const roles = ref([])
const departmentTree = ref([])
const departmentsFlat = ref([])

const searchKeyword = ref('')
const searchDept = ref('')
const searchRole = ref('')

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const form = reactive({
  id: null,
  username: '',
  email: '',
  password: '',
  departmentId: null,
  roleId: null,
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

onMounted(async () => {
  try {
    const roleRes = await getRoleList({ silent: true })
    if (roleRes?.code === 200) {
      roles.value = roleRes.data || []
    }
  } catch (_) { /* 角色列表失败 */ }
  try {
    const deptRes = await getDepartmentTree({ silent: true })
    if (deptRes?.code === 200) {
      departmentTree.value = deptRes.data || []
      const flatten = (arr, result = []) => {
        arr.forEach(item => {
          result.push(item)
          if (item.children) flatten(item.children, result)
        })
        return result
      }
      departmentsFlat.value = flatten(deptRes.data || [])
    }
  } catch (_) { /* 部门树失败 */ }
  loadData()
})

const loadData = async (showError = false) => {
  loading.value = true
  try {
    const res = await getUserPage({
      current: pagination.current,
      size: pagination.size,
      keyword: searchKeyword.value || undefined,
      departmentId: searchDept.value || undefined,
      roleId: searchRole.value || undefined
    }, showError ? undefined : { silent: true })
    if (res?.code === 200) {
      users.value = res.data?.records || []
      pagination.total = res.data?.total || 0
    }
  } catch (e) {
    if (showError) {
      ElMessage.error(e?.response?.data?.message || e?.message || '加载用户列表失败')
    }
  } finally {
    loading.value = false
  }
}

const getRoleType = (roleId) => {
  const typeMap = { 1: 'primary', 2: '', 3: 'info' }
  return typeMap[roleId] || 'info'
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: null, username: '', email: '', password: '', departmentId: null, roleId: null, status: 1 })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await updateUser(form)
    } else {
      await saveUser(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    if (!form.id) pagination.current = 1
    await loadData(true)
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '删除失败')
  }
}
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
  margin-bottom: 20px;
  
  h2 {
    font-size: 18px;
    color: #303133;
  }
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
</style>


