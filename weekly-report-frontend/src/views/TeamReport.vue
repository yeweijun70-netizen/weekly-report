<template>
  <div class="team-report">
    <el-row :gutter="20">
      <!-- 组织架构 -->
      <el-col :span="6">
        <div class="card tree-card">
          <h3><el-icon><OfficeBuilding /></el-icon> 组织架构</h3>
          <el-input v-model="treeFilterText" placeholder="搜索部门..." class="tree-search" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-tree 
            ref="treeRef"
            :data="departments" 
            :props="{ label: 'name', children: 'children' }" 
            :filter-node-method="filterNode"
            default-expand-all 
            highlight-current 
            node-key="id"
            @node-click="handleNodeClick"
          >
            <template #default="{ node, data }">
              <span class="tree-node">
                <el-icon v-if="data.children?.length"><FolderOpened /></el-icon>
                <el-icon v-else><User /></el-icon>
                <span>{{ node.label }}</span>
                <el-tag v-if="data.userCount" size="small" type="info">{{ data.userCount }}</el-tag>
              </span>
            </template>
          </el-tree>
          <div class="tree-footer" v-if="selectedDept">
            <span>宸查€? {{ selectedDept.name }}</span>
            <el-button link type="primary" @click="clearDeptFilter">娓呴櫎</el-button>
          </div>
        </div>
      </el-col>

      <!-- 周报列表 -->
      <el-col :span="18">
        <div class="card">
          <div class="card-header">
            <h3><el-icon><Document /></el-icon> 团队周报</h3>
          </div>

          <!-- 筛选条件 -->
          <div class="filter-bar">
            <el-date-picker 
              v-model="dateRange" 
              type="daterange" 
              range-separator="至" 
              start-placeholder="开始日期" 
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              :shortcuts="dateShortcuts"
              @change="loadReports"
            />
            <el-select v-model="filterWeek" placeholder="选择周次" clearable style="width: 140px" @change="loadReports">
              <el-option v-for="w in weekOptions" :key="w.value" :label="w.label" :value="w.value" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="提交状态" clearable style="width: 120px" @change="loadReports">
              <el-option label="已提交" :value="1" />
              <el-option label="未提交" :value="0" />
            </el-select>
            <el-input v-model="filterKeyword" placeholder="搜索员工..." clearable style="width: 160px" @keyup.enter="loadReports" @clear="loadReports">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="loadReports"><el-icon><Search /></el-icon>搜索</el-button>
            <el-button @click="resetFilters"><el-icon><RefreshLeft /></el-icon>重置</el-button>
          </div>

          <!-- 缁熻淇℃伅 -->
          <div class="stats-bar">
            <el-tag type="info">共 {{ pagination.total }} 条</el-tag>
            <el-tag type="success">已提交 {{ stats.submitted }}</el-tag>
            <el-tag type="warning">未提交 {{ stats.pending }}</el-tag>
            <el-tag>提交率 {{ stats.rate }}%</el-tag>
          </div>

          <el-table :data="reports" stripe v-loading="loading">
            <el-table-column prop="username" label="姓名" width="100" />
            <el-table-column prop="departmentName" label="部门" width="150" />
            <el-table-column prop="weekNumber" label="周次" width="80">
              <template #default="{ row }">第{{ row.weekNumber }}周</template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
                  {{ row.status === 1 ? '已提交' : '未提交' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="submitTime" label="提交时间" width="180">
              <template #default="{ row }">{{ formatDateTime(row.submitTime) }}</template>
            </el-table-column>
            <el-table-column label="评分" width="150">
              <template #default="{ row }">
                <el-rate v-if="row.score" :model-value="row.score" disabled show-score text-color="#ff9900" />
                <span v-else class="no-score">未评分</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button link type="primary" :disabled="row.status !== 1" @click="viewDetail(row)">
                  <el-icon><View /></el-icon>查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <el-pagination 
            v-model:current-page="pagination.current" 
            v-model:page-size="pagination.size" 
            :total="pagination.total" 
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper" 
            @current-change="loadReports" 
            @size-change="loadReports"
            class="pagination"
          />
        </div>
      </el-col>
    </el-row>

    <!-- 璇︽儏寮圭獥 -->
    <el-dialog v-model="dialogVisible" :title="`周报详情 - ${currentReport.username} (第${currentReport.weekNumber}周)`" width="700px">
      <div class="report-detail">
        <div class="section">
          <h4><el-icon><Document /></el-icon> 鏈懆宸ヤ綔</h4>
          <div class="content" v-html="currentReport.thisWeekWork || '无'"></div>
        </div>
        <div class="section">
          <h4><el-icon><Calendar /></el-icon> 下周计划</h4>
          <div class="content" v-html="currentReport.nextWeekPlan || '无'"></div>
        </div>
        <div class="section">
          <h4><el-icon><ChatLineRound /></el-icon> 需协调事项</h4>
          <div class="content" v-html="currentReport.coordination || '无'"></div>
        </div>
        
        <el-divider />
        
        <div class="review-section">
          <h4><el-icon><Star /></el-icon> 评分评价</h4>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="form-item">
                <label>评分</label>
                <el-rate v-model="reviewForm.score" :max="5" allow-half show-score :colors="['#99A9BF', '#F7BA2A', '#FF9900']" />
              </div>
            </el-col>
            <el-col :span="16">
              <div class="form-item">
                <label>评价</label>
                <el-input v-model="reviewForm.comment" type="textarea" :rows="3" placeholder="请输入评价..." />
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleReview" :loading="reviewing">保存评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getDepartmentTree, getTeamReports, getCurrentWeek, getReport, reviewReport } from '@/api'
import { formatDateTime } from '@/utils/date'

const loading = ref(false)
const reviewing = ref(false)
const dialogVisible = ref(false)
const treeRef = ref()
const treeFilterText = ref('')

const currentWeek = ref(1)
const currentYear = ref(2024)
const departments = ref([])
const reports = ref([])
const currentReport = ref({})
const selectedDept = ref(null)

// 绛涢€夋潯浠?
const dateRange = ref(null)
const filterWeek = ref(null)
const filterStatus = ref(null)
const filterKeyword = ref('')

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 缁熻
const stats = reactive({
  submitted: 0,
  pending: 0,
  rate: 0
})

const reviewForm = reactive({
  score: 0,
  comment: ''
})

// 鍛ㄦ閫夐」
const weekOptions = computed(() => {
  const options = []
  for (let i = 1; i <= currentWeek.value; i++) {
      options.push({ label: `第${i}周`, value: i })
    }
  return options.reverse()
})

// 鏃ユ湡蹇嵎閫夐」
const dateShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

// 鏍戣妭鐐硅繃婊?
watch(treeFilterText, (val) => {
  treeRef.value?.filter(val)
})

const filterNode = (value, data) => {
  if (!value) return true
  return data.name.includes(value)
}

onMounted(async () => {
  try {
    const weekRes = await getCurrentWeek({ silent: true })
    if (weekRes?.code === 200 && weekRes.data) {
      currentWeek.value = weekRes.data.weekNumber
      currentYear.value = weekRes.data.year
    }
  } catch (_) { /* 使用默认 */ }
  try {
    const deptRes = await getDepartmentTree({ silent: true })
    if (deptRes?.code === 200) {
      departments.value = deptRes.data || []
    }
  } catch (_) { /* 部门树加载失败 */ }
  loadReports()
})

const loadReports = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size
    }
    if (filterWeek.value) {
      params.weekNumber = filterWeek.value
      params.year = currentYear.value
    }
    if (selectedDept.value) {
      params.departmentId = selectedDept.value.id
    }
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    if (filterStatus.value !== null && filterStatus.value !== '') {
      params.status = filterStatus.value
    }
    if (filterKeyword.value) {
      params.keyword = filterKeyword.value
    }
    const res = await getTeamReports(params, { silent: true })
    if (res?.code === 200) {
      reports.value = res.data?.records || []
      pagination.total = res.data?.total || 0
      stats.submitted = reports.value.filter(r => r.status === 1).length
      stats.pending = reports.value.filter(r => r.status === 0).length
      stats.rate = pagination.total > 0 ? Math.round((stats.submitted / pagination.total) * 100) : 0
    }
  } catch (_) {
    reports.value = []
  } finally {
    loading.value = false
  }
}

const handleNodeClick = (data) => {
  selectedDept.value = data
  pagination.current = 1
  loadReports()
}

const clearDeptFilter = () => {
  selectedDept.value = null
  treeRef.value?.setCurrentKey(null)
  loadReports()
}

const resetFilters = () => {
  dateRange.value = null
  filterWeek.value = null
  filterStatus.value = null
  filterKeyword.value = ''
  selectedDept.value = null
  treeRef.value?.setCurrentKey(null)
  pagination.current = 1
  loadReports()
}

const viewDetail = async (row) => {
  try {
    const res = await getReport(row.id, { silent: true })
    if (res?.code === 200 && res.data) {
      currentReport.value = res.data
      reviewForm.score = res.data.score != null ? Number(res.data.score) : 0
      reviewForm.comment = res.data.comment || ''
      dialogVisible.value = true
    }
  } catch (_) { /* 由拦截器提示 */ }
}

const handleReview = async () => {
  reviewing.value = true
  try {
    await reviewReport(currentReport.value.id, reviewForm)
    ElMessage.success('评价已保存')
    dialogVisible.value = false
    loadReports()
  } finally {
    reviewing.value = false
  }
}
</script>

<style lang="scss" scoped>
.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  
  h3 {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    margin-bottom: 16px;
    color: #303133;
  }
  
  .card-header {
    margin-bottom: 16px;
  }
}

.tree-card {
  position: sticky;
  top: 20px;
  
  .tree-search {
    margin-bottom: 12px;
  }
  
  .tree-footer {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #ebeef5;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 13px;
    color: #606266;
  }
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  
  .el-tag {
    margin-left: auto;
  }
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  align-items: center;
}

.stats-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.no-score {
  color: #909399;
  font-size: 13px;
}

.report-detail {
  .section {
    margin-bottom: 20px;
    
    h4 {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #409eff;
      font-size: 14px;
      margin-bottom: 10px;
    }
    
    .content {
      background: #f5f7fa;
      padding: 12px 16px;
      border-radius: 6px;
      line-height: 1.8;
      min-height: 60px;
      
      :deep(p) {
        margin: 0.5em 0;
      }
      
      :deep(ul), :deep(ol) {
        padding-left: 20px;
      }
    }
  }
  
  .review-section {
    h4 {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #e6a23c;
      font-size: 14px;
      margin-bottom: 16px;
    }
  }
  
  .form-item {
    label {
      display: block;
      font-weight: 600;
      margin-bottom: 8px;
      color: #606266;
    }
  }
}
</style>


