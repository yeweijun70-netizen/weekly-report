<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 宸︿晶涓汉淇℃伅鍗＄墖 -->
      <el-col :span="8">
        <div class="card profile-card">
          <div class="avatar-section">
            <el-avatar :size="100" :src="userInfo.avatar">
              {{ userInfo.username?.charAt(0) }}
            </el-avatar>
            <el-upload
              class="avatar-uploader"
              action="#"
              :show-file-list="false"
              :before-upload="handleAvatarUpload"
            >
              <el-button size="small" type="primary" plain>鏇存崲澶村儚</el-button>
            </el-upload>
          </div>
          <div class="info-section">
            <h2>{{ userInfo.username }}</h2>
            <p class="role"><el-tag type="primary">{{ userInfo.roleName || '员工' }}</el-tag></p>
            <p class="dept"><el-icon><OfficeBuilding /></el-icon> {{ userInfo.departmentName || '未设置' }}</p>
            <p class="email"><el-icon><Message /></el-icon> {{ userInfo.email }}</p>
          </div>
          <el-divider />
          <div class="stats-section">
            <div class="stat-item">
              <span class="value">{{ stats.totalReports }}</span>
              <span class="label">提交周报</span>
            </div>
            <div class="stat-item">
              <span class="value">{{ stats.avgScore }}</span>
              <span class="label">平均评分</span>
            </div>
            <div class="stat-item">
              <span class="value">{{ stats.streak }}</span>
              <span class="label">连续提交</span>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 鍙充晶鍐呭鍖?-->
      <el-col :span="16">
        <el-tabs v-model="activeTab" class="profile-tabs">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="info">
            <div class="card">
              <el-form :model="profileForm" label-width="100px" class="profile-form">
                <el-form-item label="用户名">
                  <el-input v-model="profileForm.username" placeholder="请输入用户名" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="profileForm.email" disabled />
                  <div class="form-tip">邮箱不可修改</div>
                </el-form-item>
                <el-form-item label="手机号">
                  <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item label="部门">
                  <el-input :value="userInfo.departmentName" disabled />
                </el-form-item>
                <el-form-item label="角色">
                  <el-input :value="userInfo.roleName" disabled />
                </el-form-item>
                <el-form-item label="个人简介">
                  <el-input v-model="profileForm.bio" type="textarea" :rows="3" placeholder="介绍一下自己..." />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="saveProfile" :loading="saving">保存修改</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 淇敼瀵嗙爜 -->
          <el-tab-pane label="淇敼瀵嗙爜" name="password">
            <div class="card">
              <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" class="profile-form">
                <el-form-item label="当前密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入当前密码" show-password />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
                </el-form-item>
                <el-form-item label="纭瀵嗙爜" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="changePassword" :loading="changingPassword">淇敼瀵嗙爜</el-button>
                </el-form-item>
              </el-form>
            </div>
          </el-tab-pane>

          <!-- 我的周报 -->
          <el-tab-pane label="我的周报" name="reports">
            <div class="card">
              <el-table :data="myReports" stripe v-loading="loadingReports">
                <el-table-column prop="weekNumber" label="周次" width="100">
                  <template #default="{ row }">第{{ row.weekNumber }}周</template>
                </el-table-column>
                <el-table-column prop="year" label="骞翠唤" width="80" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
                      {{ row.status === 1 ? '已提交' : '草稿' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="submitTime" label="提交时间" width="180">
                  <template #default="{ row }">{{ formatDateTime(row.submitTime) }}</template>
                </el-table-column>
                <el-table-column prop="score" label="评分" width="100">
                  <template #default="{ row }">
                    <span v-if="row.score" class="score">
                      <el-icon color="#ffc107"><StarFilled /></el-icon> {{ row.score }}
                    </span>
                    <span v-else class="no-score">-</span>
                  </template>
                </el-table-column>
                <el-table-column label="操作">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="viewReport(row)">鏌ョ湅</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </el-tab-pane>

          <!-- 娑堟伅通知设置 -->
          <el-tab-pane label="通知设置" name="notifications">
            <div class="card">
              <div class="notification-settings">
                <div class="setting-item">
                  <div class="setting-info">
                    <h4>周报提醒</h4>
                    <p>姣忓懆浜斾笅鍗堟彁閱掓偍提交周报</p>
                  </div>
                  <el-switch v-model="notificationSettings.weeklyReminder" />
                </div>
                <el-divider />
                <div class="setting-item">
                  <div class="setting-info">
                    <h4>评分通知</h4>
                    <p>当您的周报被评分时通知您</p>
                  </div>
                  <el-switch v-model="notificationSettings.scoreNotify" />
                </div>
                <el-divider />
                <div class="setting-item">
                  <div class="setting-info">
                    <h4>评价通知</h4>
                    <p>当您收到周报评价时通知您</p>
                  </div>
                  <el-switch v-model="notificationSettings.commentNotify" />
                </div>
                <el-divider />
                <div class="setting-item">
                  <div class="setting-info">
                    <h4>系统公告</h4>
                    <p>鎺ユ敹绯荤粺缁存姢銆佹洿鏂扮瓑鍏憡</p>
                  </div>
                  <el-switch v-model="notificationSettings.systemNotify" />
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getMyReports } from '@/api'
import { formatDateTime } from '@/utils/date'

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const activeTab = ref('info')
const saving = ref(false)
const changingPassword = ref(false)
const loadingReports = ref(false)
const passwordFormRef = ref()

// 缁熻鏁版嵁
const stats = reactive({
  totalReports: 12,
  avgScore: 4.5,
  streak: 8
})

// 涓汉琛ㄥ崟
const profileForm = reactive({
  username: userStore.userInfo.username || '',
  email: userStore.userInfo.email || '',
  phone: '',
  bio: ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 我的周报
const myReports = ref([])

// 通知设置
const notificationSettings = reactive({
  weeklyReminder: true,
  scoreNotify: true,
  commentNotify: true,
  systemNotify: false
})

onMounted(async () => {
  loadReports()
})

const loadReports = async () => {
  loadingReports.value = true
  try {
    const res = await getMyReports({ silent: true })
    if (res.code === 200) {
      myReports.value = res.data || []
      stats.totalReports = myReports.value.filter(r => r.status === 1).length
      
      const scored = myReports.value.filter(r => r.score)
      if (scored.length > 0) {
        stats.avgScore = (scored.reduce((sum, r) => sum + r.score, 0) / scored.length).toFixed(1)
      }
    }
  } finally {
    loadingReports.value = false
  }
}

const handleAvatarUpload = (file) => {
  // TODO: 上传头像
  ElMessage.info('头像上传功能开发中')
  return false
}

const saveProfile = async () => {
  saving.value = true
  try {
    // TODO: 调用API保存
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('保存成功')
  } finally {
    saving.value = false
  }
}

const changePassword = async () => {
  await passwordFormRef.value.validate()
  changingPassword.value = true
  try {
    // TODO: 璋冪敤API淇敼瀵嗙爜
    await new Promise(resolve => setTimeout(resolve, 500))
    ElMessage.success('密码修改成功')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } finally {
    changingPassword.value = false
  }
}

const viewReport = (row) => {
  // TODO: 查看周报璇︽儏
}
</script>

<style lang="scss" scoped>
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
}

.card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.profile-card {
  text-align: center;
  
  .avatar-section {
    margin-bottom: 20px;
    
    .el-avatar {
      font-size: 36px;
      background: linear-gradient(135deg, #409eff, #0d6efd);
    }
    
    .avatar-uploader {
      margin-top: 12px;
    }
  }
  
  .info-section {
    h2 {
      font-size: 20px;
      margin-bottom: 8px;
    }
    
    .role {
      margin-bottom: 16px;
    }
    
    p {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      color: #606266;
      margin: 8px 0;
      font-size: 14px;
    }
  }
  
  .stats-section {
    display: flex;
    justify-content: space-around;
    
    .stat-item {
      text-align: center;
      
      .value {
        display: block;
        font-size: 24px;
        font-weight: 700;
        color: #409eff;
      }
      
      .label {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

.profile-tabs {
  :deep(.el-tabs__content) {
    padding: 0;
  }
}

.profile-form {
  max-width: 500px;
  
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 4px;
  }
}

.notification-settings {
  .setting-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    
    .setting-info {
      h4 {
        font-size: 14px;
        color: #303133;
        margin-bottom: 4px;
      }
      
      p {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

.score {
  display: flex;
  align-items: center;
  gap: 4px;
}

.no-score {
  color: #909399;
}
</style>


