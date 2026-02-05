<template>
  <div class="role-management">
    <div class="card">
      <div class="page-header">
        <h2>角色管理</h2>
        <el-button type="primary" @click="openRoleDialog()">
          <el-icon><Plus /></el-icon>新增角色
        </el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="roles" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.id)">{{ row.name }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="角色编码" width="120" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="userCount" label="用户数" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'warning'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="openRoleDialog(row)">编辑</el-button>
            <el-button link type="primary" @click="openPermDialog(row)">权限</el-button>
            <el-button link type="danger" :disabled="row.id === 1" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 角色弹窗 -->
    <el-dialog v-model="roleDialogVisible" :title="roleForm.id ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="roleForm" :rules="roleRules" ref="roleFormRef" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="roleForm.code" placeholder="请输入角色编码（如 ADMIN）" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="roleForm.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="roleForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRole" :loading="submitting">保存</el-button>
      </template>
    </el-dialog>

    <!-- 权限弹窗 -->
    <el-dialog v-model="permDialogVisible" title="权限配置" width="700px">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="perm-card">
            <div class="perm-header">
              <el-checkbox v-model="permForm.report" @change="toggleGroup('report')">周报管理</el-checkbox>
            </div>
            <div class="perm-body">
              <el-checkbox v-model="permForm.reportView">查看周报</el-checkbox>
              <el-checkbox v-model="permForm.reportWrite">撰写周报</el-checkbox>
              <el-checkbox v-model="permForm.reportSubmit">提交周报</el-checkbox>
              <el-checkbox v-model="permForm.reportDelete">删除周报</el-checkbox>
            </div>
          </div>
          <div class="perm-card">
            <div class="perm-header">
              <el-checkbox v-model="permForm.team" @change="toggleGroup('team')">团队管理</el-checkbox>
            </div>
            <div class="perm-body">
              <el-checkbox v-model="permForm.teamView">查看团队周报</el-checkbox>
              <el-checkbox v-model="permForm.teamReview">评分评价</el-checkbox>
              <el-checkbox v-model="permForm.teamRemind">催交提醒</el-checkbox>
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="perm-card">
            <div class="perm-header">
              <el-checkbox v-model="permForm.stats" @change="toggleGroup('stats')">统计分析</el-checkbox>
            </div>
            <div class="perm-body">
              <el-checkbox v-model="permForm.statsView">查看统计报表</el-checkbox>
              <el-checkbox v-model="permForm.statsExport">导出报表</el-checkbox>
            </div>
          </div>
          <div class="perm-card">
            <div class="perm-header">
              <el-checkbox v-model="permForm.system" @change="toggleGroup('system')">系统管理</el-checkbox>
            </div>
            <div class="perm-body">
              <el-checkbox v-model="permForm.systemUser">用户管理</el-checkbox>
              <el-checkbox v-model="permForm.systemRole">角色管理</el-checkbox>
              <el-checkbox v-model="permForm.systemDept">部门管理</el-checkbox>
            </div>
          </div>
        </el-col>
      </el-row>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePerm">保存权限</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoleList, saveRole, updateRolePermissions, deleteRole } from '@/api'

const loading = ref(false)
const submitting = ref(false)
const roleDialogVisible = ref(false)
const permDialogVisible = ref(false)
const roleFormRef = ref()

const roles = ref([])
const currentRoleId = ref(null)

const roleForm = reactive({
  id: null,
  name: '',
  code: '',
  description: '',
  status: 1
})

const roleRules = {
name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
    code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

const permForm = reactive({
  report: false, reportView: false, reportWrite: false, reportSubmit: false, reportDelete: false,
  team: false, teamView: false, teamReview: false, teamRemind: false,
  stats: false, statsView: false, statsExport: false,
  system: false, systemUser: false, systemRole: false, systemDept: false
})

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getRoleList({ silent: true })
    if (res.code === 200) {
      roles.value = res.data || []
    }
  } finally {
    loading.value = false
  }
}

const getRoleType = (id) => {
  const typeMap = { 1: 'primary', 2: '', 3: 'info' }
  return typeMap[id] || 'info'
}

const openRoleDialog = (row) => {
  if (row) {
    Object.assign(roleForm, row)
  } else {
    Object.assign(roleForm, { id: null, name: '', code: '', description: '', status: 1 })
  }
  roleDialogVisible.value = true
}

const handleSaveRole = async () => {
  await roleFormRef.value.validate()
  submitting.value = true
  try {
    await saveRole(roleForm)
    ElMessage.success('保存成功')
    roleDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '保存失败')
  } finally {
    submitting.value = false
  }
}

const openPermDialog = (row) => {
  currentRoleId.value = row.id
  // 解析已有权限
  try {
    const perms = JSON.parse(row.permissions || '[]')
    Object.keys(permForm).forEach(k => permForm[k] = perms.includes(k) || perms.includes('*'))
  } catch {
    Object.keys(permForm).forEach(k => permForm[k] = false)
  }
  permDialogVisible.value = true
}

const toggleGroup = (group) => {
  const map = {
    report: ['reportView', 'reportWrite', 'reportSubmit', 'reportDelete'],
    team: ['teamView', 'teamReview', 'teamRemind'],
    stats: ['statsView', 'statsExport'],
    system: ['systemUser', 'systemRole', 'systemDept']
  }
  map[group].forEach(k => permForm[k] = permForm[group])
}

const handleSavePerm = async () => {
  const perms = Object.keys(permForm).filter(k => permForm[k])
  try {
    await updateRolePermissions(currentRoleId.value, JSON.stringify(perms))
    ElMessage.success('权限已保存')
    permDialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '保存权限失败')
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定要删除角色"${row.name}"吗？`, '提示', { type: 'warning' })
  try {
    await deleteRole(row.id)
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

.perm-card {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 16px;
  
  .perm-header {
    background: #f5f7fa;
    padding: 10px 16px;
    font-weight: 600;
  }
  
  .perm-body {
    padding: 12px 16px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    
    .el-checkbox {
      margin-left: 16px;
    }
  }
}
</style>


