<template>
  <div class="dashboard">
    <!-- 缁熻鍗＄墖 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <div class="stat-card">
          <div class="icon bg-primary"><el-icon :size="28"><SuccessFilled /></el-icon></div>
          <div class="info">
            <span class="number">{{ stats.submitted }}</span>
            <span class="label">已提交周报</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="icon bg-warning"><el-icon :size="28"><Clock /></el-icon></div>
          <div class="info">
            <span class="number">{{ stats.pending }}</span>
            <span class="label">待提交</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="icon bg-success"><el-icon :size="28"><Star /></el-icon></div>
          <div class="info">
            <span class="number">{{ stats.avgScore }}</span>
            <span class="label">平均评分</span>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="icon bg-info"><el-icon :size="28"><Calendar /></el-icon></div>
          <div class="info">
            <span class="number">第{{ currentWeek }}周</span>
            <span class="label">当前周次</span>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 鍘嗗彶鍛ㄦ姤 -->
      <el-col :span="16">
        <div class="card">
          <h3>我的历史周报</h3>
          <el-table :data="reports" stripe>
            <el-table-column prop="weekNumber" label="周次" width="100">
              <template #default="{ row }">第{{ row.weekNumber }}周</template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="180">
              <template #default="{ row }">{{ formatDateTime(row.submitTime) }}</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'warning'">
                  {{ row.status === 1 ? '已提交' : '草稿' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="score" label="评分" width="120">
              <template #default="{ row }">
                <span v-if="row.score"><el-icon color="#ffc107"><StarFilled /></el-icon> {{ row.score }}</span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewReport(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>

      <!-- 寰呭姙浜嬮」 -->
      <el-col :span="8">
        <div class="card">
          <h3>待办事项</h3>
          <div class="todo-list">
            <div class="todo-item warning" v-if="!currentReportSubmitted">
              <el-icon><WarningFilled /></el-icon>
              <span>本周周报尚未提交</span>
            </div>
            <div class="todo-item info">
              <el-icon><ChatDotRound /></el-icon>
              <span>收到1条新评价</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 周报详情弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" top="5vh">
      <div class="report-detail" v-if="currentReport">
        <div class="report-meta">
          <el-descriptions :column="3" border size="small">
            <el-descriptions-item label="周次">第{{ currentReport.weekNumber }}周</el-descriptions-item>
            <el-descriptions-item label="年份">{{ currentReport.year }}年</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="currentReport.status === 1 ? 'success' : 'warning'" size="small">
                {{ currentReport.status === 1 ? '已提交' : '草稿' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">{{ currentReport.submitTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="评分">
              <span v-if="currentReport.score">
                <el-rate :model-value="currentReport.score" disabled show-score text-color="#ff9900" />
              </span>
              <span v-else>未评分</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="report-section">
          <h4><el-icon><Document /></el-icon> 本周工作完成情况</h4>
          <div class="content" v-html="currentReport.thisWeekWork || '无内容'"></div>
        </div>

        <div class="report-section">
          <h4><el-icon><Calendar /></el-icon> 下周工作计划</h4>
          <div class="content" v-html="currentReport.nextWeekPlan || '无内容'"></div>
        </div>

        <div class="report-section">
          <h4><el-icon><ChatLineRound /></el-icon> 需协调事项</h4>
          <div class="content" v-html="currentReport.coordination || '无内容'"></div>
        </div>

        <div class="report-section" v-if="currentReport.comment">
          <h4><el-icon><Comment /></el-icon> 评价</h4>
          <div class="content comment">{{ currentReport.comment }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToEdit" v-if="currentReport?.status === 0">去编辑</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyReports, getCurrentWeek, getCurrentReport, getReport } from '@/api'
import { formatDateTime } from '@/utils/date'

const router = useRouter()

const currentWeek = ref(1)
const currentYear = ref(2024)
const currentReportSubmitted = ref(false)
const reports = ref([])
const dialogVisible = ref(false)
const currentReport = ref(null)

const dialogTitle = computed(() => {
  if (!currentReport.value) return '周报详情'
  return `第${currentReport.value.weekNumber}周周报详情`
})

const stats = reactive({
  submitted: 0,
  pending: 0,
  avgScore: 0
})

onMounted(async () => {
  try {
    const weekRes = await getCurrentWeek({ silent: true })
    if (weekRes?.code === 200 && weekRes.data) {
      currentWeek.value = weekRes.data.weekNumber
      currentYear.value = weekRes.data.year
    }
  } catch (_) { /* 使用默认周次 */ }
  try {
    const reportsRes = await getMyReports({ silent: true })
    if (reportsRes?.code === 200) {
      reports.value = reportsRes.data || []
      stats.submitted = reports.value.filter(r => r.status === 1).length
      stats.pending = reports.value.filter(r => r.status === 0).length
      const scoredReports = reports.value.filter(r => r.score != null)
      if (scoredReports.length > 0) {
        stats.avgScore = (scoredReports.reduce((sum, r) => sum + Number(r.score), 0) / scoredReports.length).toFixed(1)
      }
    }
  } catch (_) { /* 列表加载失败 */ }
  try {
    const currentRes = await getCurrentReport({ year: currentYear.value, weekNumber: currentWeek.value }, { silent: true })
    if (currentRes?.code === 200 && currentRes.data) {
      currentReportSubmitted.value = currentRes.data.status === 1
    }
  } catch (_) { /* 当前周报状态忽略 */ }
})

const viewReport = async (row) => {
  try {
    const res = await getReport(row.id, { silent: true })
    if (res?.code === 200) {
      currentReport.value = res.data
      dialogVisible.value = true
    }
  } catch (_) {
    // 接口报错由 axios 拦截器统一提示
  }
}

const goToEdit = () => {
  dialogVisible.value = false
  router.push('/write-report')
}
</script>

<style lang="scss" scoped>
.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  
  .icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    
    &.bg-primary { background: linear-gradient(135deg, #409eff, #0d6efd); }
    &.bg-warning { background: linear-gradient(135deg, #e6a23c, #f59e0b); }
    &.bg-success { background: linear-gradient(135deg, #67c23a, #22c55e); }
    &.bg-info { background: linear-gradient(135deg, #909399, #6b7280); }
  }
  
  .info {
    display: flex;
    flex-direction: column;
    
    .number {
      font-size: 24px;
      font-weight: 700;
      color: #303133;
    }
    
    .label {
      font-size: 13px;
      color: #909399;
    }
  }
}

.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  
  h3 {
    font-size: 16px;
    margin-bottom: 16px;
    color: #303133;
  }
}

.todo-list {
  .todo-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px;
    border-radius: 6px;
    margin-bottom: 10px;
    font-size: 14px;
    
    &.warning {
      background: rgba(230, 162, 60, 0.1);
      color: #e6a23c;
    }
    
    &.info {
      background: rgba(64, 158, 255, 0.1);
      color: #409eff;
    }
  }
}

.report-detail {
  .report-meta {
    margin-bottom: 20px;
  }
  
  .report-section {
    margin-bottom: 20px;
    
    h4 {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #409eff;
      font-size: 14px;
      margin-bottom: 10px;
      padding-bottom: 8px;
      border-bottom: 1px solid #ebeef5;
    }
    
    .content {
      background: #f5f7fa;
      padding: 16px;
      border-radius: 6px;
      line-height: 1.8;
      min-height: 80px;
      font-size: 14px;
      color: #606266;
      
      :deep(p) {
        margin: 0.5em 0;
      }
      
      :deep(ul), :deep(ol) {
        padding-left: 20px;
        margin: 0.5em 0;
      }
      
      &.comment {
        background: rgba(230, 162, 60, 0.1);
        border-left: 3px solid #e6a23c;
      }
    }
  }
}
</style>


