<template>
  <el-container class="layout-container">
    <!-- 左侧边栏-->
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <el-icon :size="24"><Document /></el-icon>
        <span>周报系统</span>
      </div>
      <el-menu :default-active="route.path" router background-color="#1e3a5f" text-color="rgba(255,255,255,0.7)" active-text-color="#fff">
        <el-menu-item index="/dashboard">
            <el-icon><DataBoard /></el-icon>
            <span>工作台</span>
          </el-menu-item>
          <el-menu-item index="/write-report">
            <el-icon><Edit /></el-icon>
            <span>写周报</span>
          </el-menu-item>
          <el-menu-item index="/team-report">
            <el-icon><User /></el-icon>
            <span>团队周报</span>
          </el-menu-item>
          <el-menu-item index="/analytics">
            <el-icon><TrendCharts /></el-icon>
            <span>统计分析</span>
          </el-menu-item>
          <el-sub-menu index="system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/user-management">用户管理</el-menu-item>
            <el-menu-item index="/role-management">角色管理</el-menu-item>
            <el-menu-item index="/org-management">组织机构管理</el-menu-item>
            <el-menu-item index="/operation-log">操作日志管理</el-menu-item>
          </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <h3>{{ route.meta.title }}</h3>
        </div>
        <div class="header-right">
          <el-tag type="primary">第{{ currentWeek }}周</el-tag>
          
          <!-- AI 助手 -->
          <el-tooltip content="AI 助手" placement="bottom">
            <el-button circle class="ai-btn" @click="aiDrawerVisible = true">
              <el-icon :size="20"><ChatLineRound /></el-icon>
            </el-button>
          </el-tooltip>
          <!-- 消息通知 -->
          <el-popover placement="bottom" :width="360" trigger="click">
            <template #reference>
              <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="notification-badge">
                <el-icon :size="20" class="notification-icon"><Bell /></el-icon>
              </el-badge>
            </template>
            <div class="notification-panel">
              <div class="panel-header">
                <span>消息通知</span>
                <el-button link type="primary" @click="markAllRead" v-if="unreadCount > 0">全部已读</el-button>
              </div>
              <el-tabs v-model="messageTab">
                <el-tab-pane label="全部" name="all">
                  <div class="message-list" v-if="messages.length > 0">
                    <div 
                      v-for="msg in messages" 
                      :key="msg.id" 
                      class="message-item"
                      :class="{ unread: !msg.read }"
                      @click="handleMessage(msg)"
                    >
                      <el-icon :class="msg.type"><component :is="getMessageIcon(msg.type)" /></el-icon>
                      <div class="message-content">
                        <div class="message-title">{{ msg.title }}</div>
                        <div class="message-desc">{{ msg.content }}</div>
                        <div class="message-time">{{ msg.time }}</div>
                      </div>
                    </div>
                  </div>
                  <el-empty v-else description="无消息" :image-size="60" />
                </el-tab-pane>
                <el-tab-pane label="未读" name="unread">
                  <div class="message-list" v-if="unreadMessages.length > 0">
                    <div 
                      v-for="msg in unreadMessages" 
                      :key="msg.id" 
                      class="message-item unread"
                      @click="handleMessage(msg)"
                    >
                      <el-icon :class="msg.type"><component :is="getMessageIcon(msg.type)" /></el-icon>
                      <div class="message-content">
                        <div class="message-title">{{ msg.title }}</div>
                        <div class="message-desc">{{ msg.content }}</div>
                        <div class="message-time">{{ msg.time }}</div>
                      </div>
                    </div>
                  </div>
                  <el-empty v-else description="没有未读消息" :image-size="60" />
                </el-tab-pane>
              </el-tabs>
              <div class="panel-footer">
                <el-button link type="primary" @click="router.push('/messages')">查看全部消息</el-button>
              </div>
            </div>
          </el-popover>
          
          <!-- 用户下拉菜单 -->
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="userStore.userInfo.avatar">
                {{ userStore.userInfo.username?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区域-->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>

    <!-- AI 助手抽屉 -->
    <el-drawer v-model="aiDrawerVisible" title="AI 助手" direction="rtl" size="400px" class="ai-drawer">
      <div class="ai-chat">
        <div class="ai-messages" ref="aiMessagesRef">
          <div v-if="aiMessages.length === 0" class="ai-placeholder">输入问题，我会帮你解答周报系统相关操作～</div>
          <div v-for="(msg, i) in aiMessages" :key="i" :class="['ai-msg', msg.role]">
            <span class="ai-msg-label">{{ msg.role === 'user' ? '我' : '助手' }}</span>
            <div class="ai-msg-content">{{ msg.content }}</div>
          </div>
          <div v-if="aiLoading" class="ai-msg assistant">
            <span class="ai-msg-label">助手</span>
            <div class="ai-msg-content"><el-icon class="is-loading"><Loading /></el-icon> 思考中…</div>
          </div>
        </div>
        <div class="ai-input-row">
          <el-input v-model="aiInput" placeholder="输入问题…" clearable @keyup.enter="sendAiMessage" :disabled="aiLoading" />
          <el-button type="primary" @click="sendAiMessage" :loading="aiLoading">发送</el-button>
        </div>
      </div>
    </el-drawer>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCurrentWeek } from '@/api'
import { ChatDotRound, WarningFilled, StarFilled, Bell, ChatLineRound, Loading } from '@element-plus/icons-vue'
import { aiAssist } from '@/api'

const messageIcons = {
    comment: ChatDotRound,
    reminder: WarningFilled,
    score: StarFilled,
    system: Bell
}

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const currentWeek = ref(1)
const messageTab = ref('all')

// AI 助手
const aiDrawerVisible = ref(false)
const aiInput = ref('')
const aiMessages = ref([])
const aiLoading = ref(false)
const aiMessagesRef = ref(null)

const sendAiMessage = async () => {
  const text = (aiInput.value || '').trim()
  if (!text || aiLoading.value) return
  aiMessages.value.push({ role: 'user', content: text })
  aiInput.value = ''
  aiLoading.value = true
  // 多轮：把当前会话中「本条之前」的历史发给后端，模型能理解追问、指代
  const history = aiMessages.value.slice(0, -1).map(m => ({ role: m.role, content: m.content }))
  const payload = {
    message: text,
    history,
    context: { currentWeek: currentWeek.value }
  }
  try {
    const res = await aiAssist(payload, { silent: true })
    const reply = (res?.data?.reply != null) ? String(res.data.reply) : (res?.message || '暂无回复')
    aiMessages.value.push({ role: 'assistant', content: reply })
  } catch (e) {
    aiMessages.value.push({ role: 'assistant', content: '请求失败：' + (e?.response?.data?.message || e?.message || '网络错误') })
  } finally {
    aiLoading.value = false
    await nextTick()
    if (aiMessagesRef.value) aiMessagesRef.value.scrollTop = aiMessagesRef.value.scrollHeight
  }
}

// 模拟消息数据
const messages = ref([
  { id: 1, type: 'comment', title: '收到新评价', content: '李四对您的周报进行了评价：工作完成度高，继续保持', time: '10分钟前', read: false },
  { id: 2, type: 'reminder', title: '周报提醒', content: '本周周报即将截止，请尽快提交', time: '1小时前', read: false },
  { id: 3, type: 'score', title: '周报评分', content: '您第2周的周报获得4.5分', time: '昨天', read: true },
  { id: 4, type: 'system', title: '系统通知', content: '系统将于今晚22:00进行维护升级', time: '2天前', read: true }
])

const unreadMessages = computed(() => messages.value.filter(m => !m.read))
const unreadCount = computed(() => unreadMessages.value.length)

const getMessageIcon = (type) => {
  return messageIcons[type] || Bell
}

const handleMessage = (msg) => {
  msg.read = true
  // TODO: 璺宠浆鍒板搴旈〉闈?
}

const markAllRead = () => {
  messages.value.forEach(m => m.read = true)
}

onMounted(async () => {
  try {
    const res = await getCurrentWeek({ silent: true })
    if (res?.code === 200 && res.data) {
      currentWeek.value = res.data.weekNumber
    }
  } catch (_) {
    currentWeek.value = 1
  }
})

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    router.push('/profile')
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, #1e3a5f, #0d2137);
  
  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    color: #fff;
    font-size: 18px;
    font-weight: 600;
    border-bottom: 1px solid rgba(255,255,255,0.1);
  }
  
  .el-menu {
    border-right: none;
  }
}

.header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  padding: 0 20px;
  
  .header-left h3 {
    font-size: 16px;
    color: #303133;
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .notification-badge {
      cursor: pointer;
      
      .notification-icon {
        color: #606266;
        transition: color 0.3s;
        
        &:hover {
          color: #409eff;
        }
      }
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      color: #606266;
      
      .username {
        max-width: 100px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}

.main {
  background: #f5f7fa;
  padding: 20px;
}

// 消息通知面板样式
.notification-panel {
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 12px;
    border-bottom: 1px solid #ebeef5;
    margin-bottom: 12px;
    
    span {
      font-weight: 600;
      font-size: 14px;
    }
  }
  
  .message-list {
    max-height: 300px;
    overflow-y: auto;
  }
  
  .message-item {
    display: flex;
    gap: 12px;
    padding: 12px;
    border-radius: 6px;
    cursor: pointer;
    transition: background 0.3s;
    
    &:hover {
      background: #f5f7fa;
    }
    
    &.unread {
      background: rgba(64, 158, 255, 0.05);
      
      .message-title {
        font-weight: 600;
      }
    }
    
    .el-icon {
      font-size: 20px;
      padding: 8px;
      border-radius: 50%;
      
      &.comment { color: #409eff; background: rgba(64, 158, 255, 0.1); }
      &.reminder { color: #e6a23c; background: rgba(230, 162, 60, 0.1); }
      &.score { color: #67c23a; background: rgba(103, 194, 58, 0.1); }
      &.system { color: #909399; background: rgba(144, 147, 153, 0.1); }
    }
    
    .message-content {
      flex: 1;
      min-width: 0;
      
      .message-title {
        font-size: 14px;
        color: #303133;
        margin-bottom: 4px;
      }
      
      .message-desc {
        font-size: 12px;
        color: #909399;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .message-time {
        font-size: 12px;
        color: #c0c4cc;
        margin-top: 4px;
      }
    }
  }
  
  .panel-footer {
    text-align: center;
    padding-top: 12px;
    border-top: 1px solid #ebeef5;
    margin-top: 12px;
  }
}

.header-right .ai-btn {
  color: #606266;
  &:hover {
    color: #409eff;
  }
}

.ai-drawer .ai-chat {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  min-height: 200px;
  max-height: calc(100vh - 180px);
}
.ai-placeholder {
  color: #909399;
  font-size: 13px;
  text-align: center;
  padding: 24px 12px;
}
.ai-msg {
  margin-bottom: 16px;
  &.user .ai-msg-content { background: #ecf5ff; color: #303133; margin-left: 0; margin-right: 40px; }
  &.assistant .ai-msg-content { background: #f4f4f5; color: #303133; margin-left: 40px; margin-right: 0; }
}
.ai-msg-label {
  font-size: 12px;
  color: #909399;
  display: block;
  margin-bottom: 4px;
}
.ai-msg-content {
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}
.ai-input-row {
  display: flex;
  gap: 8px;
  padding: 12px;
  border-top: 1px solid #ebeef5;
}
.ai-input-row .el-input { flex: 1; }
</style>


