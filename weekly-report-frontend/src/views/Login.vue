<template>
  <div class="login-container">
    <div class="login-left">
      <div class="brand">
        <el-icon :size="64"><Document /></el-icon>
          <h1>企业周报管理系统</h1>
          <p>高效协作，精准管理</p>
      </div>
    </div>
    <div class="login-right">
      <div class="login-form">
        <h2>用户登录</h2>
        <el-form ref="formRef" :model="form" :rules="rules" size="large">
          <el-form-item prop="email">
            <el-input v-model="form.email" placeholder="邮箱地址" :prefix-icon="Message" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password @keyup.enter="handleLogin" />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="form.remember">记住我</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" class="login-btn" @click="handleLogin">登录</el-button>
          </el-form-item>
          <div class="link-register">
            没有账号？<el-link type="primary" @click="router.push('/register')">去注册</el-link>
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
import { Message, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)

const form = reactive({
  email: 'admin@company.com',
  password: '123456',
  remember: true
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const result = await userStore.login(form.email, form.password)
    if (result.success) {
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      if (result.reason === 'network') {
        ElMessage.error('请求失败，请确认后端已启动（http://localhost:8080），并检查浏览器控制台网络请求')
      } else {
        ElMessage.error('邮箱或密码错误')
      }
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  height: 100vh;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #409eff, #0d6efd);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;

  .brand {
    text-align: center;
    
    h1 {
      font-size: 2.5rem;
      margin: 20px 0 10px;
    }
    
    p {
      font-size: 1.1rem;
      opacity: 0.8;
    }
  }
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.login-form {
  width: 360px;
  
  h2 {
    font-size: 24px;
    margin-bottom: 30px;
    color: #303133;
  }
  
  .login-btn {
    width: 100%;
  }

  .link-register {
    text-align: center;
    margin-top: 16px;
    font-size: 14px;
    color: #606266;
  }
}
</style>


