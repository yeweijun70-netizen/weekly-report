<template>
  <div class="analytics">
    <el-row :gutter="20">
      <el-col :span="12">
        <div class="card">
          <h3>部门提交率</h3>
          <div ref="pieChartRef" class="chart"></div>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="card">
          <h3>员工评分趋势</h3>
          <div ref="lineChartRef" class="chart"></div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { getCurrentWeek, getSubmitRate, getScoreTrend } from '@/api'

const pieChartRef = ref()
const lineChartRef = ref()
let pieChart = null
let lineChart = null

onMounted(async () => {
  let year = 2024
  let weekNumber = 1
  try {
    const weekRes = await getCurrentWeek({ silent: true })
    if (weekRes?.data) {
      year = weekRes.data.year ?? 2024
      weekNumber = weekRes.data.weekNumber ?? 1
    }
  } catch (_) { /* 使用默认 */ }

  pieChart = echarts.init(pieChartRef.value)
  let submitted = 0
  let pending = 0
  try {
    const rateRes = await getSubmitRate({ year, weekNumber }, { silent: true })
    submitted = Number(rateRes?.data?.submitted ?? 0)
    pending = Number(rateRes?.data?.pending ?? 0)
  } catch (_) {
    submitted = 0
    pending = 0
  }
  if (submitted + pending === 0) {
    submitted = 1
    pending = 0
  }
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}: {d}%' },
      data: [
        { value: submitted, name: '已提交', itemStyle: { color: '#409eff' } },
        { value: pending, name: '未提交', itemStyle: { color: '#f56c6c' } }
      ]
    }]
  })

  lineChart = echarts.init(lineChartRef.value)
  let weeks = ['第1周', '第2周', '第3周', '第4周']
  let avgScores = [4.2, 4.5, 4.3, 4.8]
  try {
    const trendRes = await getScoreTrend({ silent: true })
    if (trendRes?.data?.weeks?.length) weeks = trendRes.data.weeks
    if (trendRes?.data?.avgScores?.length) avgScores = trendRes.data.avgScores.map(Number)
  } catch (_) { /* 使用默认 */ }
  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { bottom: 0 },
    xAxis: { type: 'category', data: weeks },
    yAxis: { type: 'value', min: 0, max: 5 },
    series: [{
      name: '平均评分',
      type: 'line',
      smooth: true,
      data: avgScores,
      itemStyle: { color: '#409eff' }
    }]
  })

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  pieChart?.dispose()
  lineChart?.dispose()
  window.removeEventListener('resize', handleResize)
})

const handleResize = () => {
  pieChart?.resize()
  lineChart?.resize()
}
</script>

<style lang="scss" scoped>
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

.chart {
  height: 350px;
}
</style>


