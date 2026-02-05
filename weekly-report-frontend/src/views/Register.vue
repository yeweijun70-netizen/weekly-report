<template>
  <div class="register-container">
    <div class="register-left">
      <div class="brand">
        <el-icon :size="64"><Document /></el-icon>
        <h1>企业周报管理系统</h1>
        <p>高效协作，精准管理</p>
      </div>
    </div>
    <div class="register-right">
      <div class="register-form">
        <h2>用户注册</h2>
        <el-form ref="formRef" :model="form" :rules="rules" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="form.email" placeholder="邮箱地址" :prefix-icon="Message" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码（不少于6位）" :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Lock" show-password @keyup.enter="handleRegister" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" class="register-btn" @click="handleRegister">注册</el-button>
          </el-form-item>
          <div class="link-login">
            已有账号？<el-link type="primary" @click="router.push('/login')">去登录</el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Message, Lock, Document } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度 2～50 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const result = await userStore.register(form.username, form.email, form.password)
    if (result.success) {
      ElMessage.success('注册成功')
      router.push('/dashboard')
    } else {
      if (result.reason === 'network') {
        ElMessage.error('请求失败，请检查网络与后端服务')
      } else {
        ElMessage.error(result.reason || '注册失败')
      }
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-container {
  display: flex;
  height: 100vh;
}

.register-left {
  flex: 1;
  background: linear-gradient(135deg, #409eff, #0d6efd);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;

  .brand {
    text-align: center;
    h1 { font-size: 2.5rem; margin: 20px 0 10px; }
    p { font-size: 1.1rem; opacity: 0.8; }
  }
}

.register-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.register-form {
  width: 360px;

  h2 {
    font-size: 24px;
    margin-bottom: 30px;
    color: #303133;
  }

  .register-btn {
    width: 100%;
  }

  .link-login {
    text-align: center;
    margin-top: 16px;
    font-size: 14px;
    color: #606266;
  }
}
</style>
