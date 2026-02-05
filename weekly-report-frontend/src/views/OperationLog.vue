<template>
  <div class="operation-log">
    <div class="card">
      <div class="page-header">
        <h2><el-icon><Document /></el-icon> 操作日志管理</h2>
      </div>
      <div class="search-bar">
        <el-input v-model="searchUsername" placeholder="操作人" clearable style="width: 140px" @clear="loadData" @keyup.enter="loadData" />
        <el-input v-model="searchModule" placeholder="模块" clearable style="width: 140px" @clear="loadData" @keyup.enter="loadData" />
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          clearable
          @change="loadData"
        />
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>
      <el-table :data="list" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="操作人" width="100" />
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="operation" label="操作" min-width="140" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="方法" width="70" />
        <el-table-column prop="requestUrl" label="请求URL" min-width="180" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="120" />
        <el-table-column prop="durationMs" label="耗时(ms)" width="90" />
        <el-table-column prop="createTime" label="操作时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadData"
        @size-change="loadData"
        style="margin-top: 16px; justify-content: flex-end;"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { getOperationLogPage } from '@/api'
import { formatDateTime } from '@/utils/date'

const loading = ref(false)
const list = ref([])
const searchUsername = ref('')
const searchModule = ref('')
const dateRange = ref(null)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

async function loadData() {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      username: searchUsername.value || undefined,
      module: searchModule.value || undefined,
      startTime: dateRange && dateRange[0] ? dateRange[0] : undefined,
      endTime: dateRange && dateRange[1] ? dateRange[1] : undefined
    }
    const res = await getOperationLogPage(params, { silent: true })
    if (res?.code === 200) {
      list.value = res.data?.records || []
      pagination.total = res.data?.total || 0
    }
  } finally {
    loading.value = false
  }
}

watch(dateRange, () => { pagination.current = 1; loadData() }, { deep: true })

onMounted(() => loadData())
</script>

<style lang="scss" scoped>
.card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.page-header {
  margin-bottom: 16px;
  h2 { font-size: 18px; color: #303133; display: flex; align-items: center; gap: 8px; }
}
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
</style>
