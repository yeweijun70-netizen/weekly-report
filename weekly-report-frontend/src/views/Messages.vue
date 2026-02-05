<template>
  <div class="messages-page">
    <div class="card">
      <div class="page-header">
        <h2><el-icon><Bell /></el-icon> 娑堟伅涓績</h2>
        <div class="header-actions">
          <el-button @click="markAllRead" :disabled="unreadCount === 0">
            <el-icon><Check /></el-icon>全部已读
          </el-button>
          <el-button type="danger" plain @click="clearAll" :disabled="messages.length === 0">
            <el-icon><Delete /></el-icon>娓呯┖娑堟伅
          </el-button>
        </div>
      </div>

      <el-tabs v-model="activeTab" class="message-tabs">
        <el-tab-pane name="all">
          <template #label>
            <span>全部消息 <el-badge :value="messages.length" :max="99" /></span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="unread">
          <template #label>
            <span>鏈 <el-badge :value="unreadCount" :max="99" type="danger" /></span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="评分通知" name="score" />
        <el-tab-pane label="评价通知" name="comment" />
        <el-tab-pane label="系统通知" name="system" />
      </el-tabs>

      <div class="message-list" v-if="filteredMessages.length > 0">
        <div 
          v-for="msg in filteredMessages" 
          :key="msg.id" 
          class="message-item"
          :class="{ unread: !msg.read }"
        >
          <div class="message-icon" :class="msg.type">
            <el-icon><component :is="getMessageIcon(msg.type)" /></el-icon>
          </div>
          <div class="message-body" @click="handleMessage(msg)">
            <div class="message-header">
              <span class="title">{{ msg.title }}</span>
              <span class="time">{{ msg.time }}</span>
            </div>
            <div class="message-content">{{ msg.content }}</div>
          </div>
          <div class="message-actions">
            <el-button link type="primary" v-if="!msg.read" @click="markRead(msg)">鏍囦负宸茶</el-button>
            <el-button link type="danger" @click="deleteMessage(msg)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="鏆傛棤娑堟伅" />

      <el-pagination 
        v-if="filteredMessages.length > 0"
        v-model:current-page="pagination.current" 
        :total="pagination.total" 
        layout="total, prev, pager, next" 
        class="pagination"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, WarningFilled, StarFilled, Bell } from '@element-plus/icons-vue'

const messageIcons = {
  comment: ChatDotRound,
  reminder: WarningFilled,
  score: StarFilled,
  system: Bell
}

const activeTab = ref('all')

const pagination = ref({
  current: 1,
  total: 10
})

// 模拟消息数据
const messages = ref([
  { id: 1, type: 'comment', title: '收到新评价', content: '李四对您第2周的周报进行了评价：工作完成度高，继续保持！', time: '10分钟前', read: false },
  { id: 2, type: 'reminder', title: '周报提醒', content: '本周周报即将截止（周五17:00），请尽快提交', time: '1小时前', read: false },
  { id: 3, type: 'score', title: '周报评分', content: '您第2周的周报获得4.5分（满分5分），评价人：李四', time: '昨天 15:30', read: false },
  { id: 4, type: 'system', title: '系统维护通知', content: '系统将于今晚22:00-23:00进行维护升级，届时服务将暂时不可用', time: '2天前', read: true },
  { id: 5, type: 'comment', title: '收到新评价', content: '王五对您第3周的周报进行了评价：计划安排合理，执行有力！', time: '3天前', read: true },
  { id: 6, type: 'score', title: '周报评分', content: '您第1周的周报获得4.8分（满分5分），评价人：李四', time: '上周', read: true },
  { id: 7, type: 'system', title: '功能更新通知', content: '周报系统新增统计分析功能，可查看个人评分趋势和团队提交率', time: '上周', read: true }
])

const unreadCount = computed(() => messages.value.filter(m => !m.read).length)

const filteredMessages = computed(() => {
  let list = messages.value
  if (activeTab.value === 'unread') {
    list = list.filter(m => !m.read)
  } else if (activeTab.value === 'score') {
    list = list.filter(m => m.type === 'score')
  } else if (activeTab.value === 'comment') {
    list = list.filter(m => m.type === 'comment')
  } else if (activeTab.value === 'system') {
    list = list.filter(m => m.type === 'system' || m.type === 'reminder')
  }
  return list
})

const getMessageIcon = (type) => {
  return messageIcons[type] || Bell
}

const handleMessage = (msg) => {
  msg.read = true
  // TODO: 璺宠浆鍒板搴旈〉闈?
}

const markRead = (msg) => {
  msg.read = true
  ElMessage.success('已标记为已读')
}

const markAllRead = () => {
  messages.value.forEach(m => m.read = true)
  ElMessage.success('已全部标记为已读')
}

const deleteMessage = async (msg) => {
  await ElMessageBox.confirm('确定删除这条消息吗？', '提示', { type: 'warning' })
  const index = messages.value.findIndex(m => m.id === msg.id)
  if (index > -1) {
    messages.value.splice(index, 1)
    ElMessage.success('删除成功')
  }
}

const clearAll = async () => {
await ElMessageBox.confirm('确定清空所有消息吗？', '提示', { type: 'warning' })
    messages.value = []
    ElMessage.success('已清空所有消息')
}
</script>

<style lang="scss" scoped>
.messages-page {
  max-width: 900px;
  margin: 0 auto;
}

.card {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  h2 {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 18px;
    color: #303133;
  }
  
  .header-actions {
    display: flex;
    gap: 12px;
  }
}

.message-tabs {
  margin-bottom: 20px;
  
  :deep(.el-badge) {
    margin-left: 4px;
  }
}

.message-list {
  .message-item {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    padding: 16px;
    border-radius: 8px;
    border: 1px solid #ebeef5;
    margin-bottom: 12px;
    transition: all 0.3s;
    
    &:hover {
      box-shadow: 0 2px 12px rgba(0,0,0,0.08);
    }
    
    &.unread {
      background: rgba(64, 158, 255, 0.03);
      border-color: rgba(64, 158, 255, 0.3);
      
      .title {
        font-weight: 600;
      }
    }
  }
  
  .message-icon {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    flex-shrink: 0;
    
    &.comment { color: #409eff; background: rgba(64, 158, 255, 0.1); }
    &.reminder { color: #e6a23c; background: rgba(230, 162, 60, 0.1); }
    &.score { color: #67c23a; background: rgba(103, 194, 58, 0.1); }
    &.system { color: #909399; background: rgba(144, 147, 153, 0.1); }
  }
  
  .message-body {
    flex: 1;
    cursor: pointer;
    
    .message-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      
      .title {
        font-size: 15px;
        color: #303133;
      }
      
      .time {
        font-size: 12px;
        color: #909399;
      }
    }
    
    .message-content {
      font-size: 14px;
      color: #606266;
      line-height: 1.6;
    }
  }
  
  .message-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
}

.pagination {
  margin-top: 20px;
  justify-content: center;
}
</style>


