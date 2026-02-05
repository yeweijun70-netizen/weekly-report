<template>
  <div class="write-report">
    <div class="card">
      <div class="card-header">
        <div class="title">
          <el-icon><Edit /></el-icon>
          <span>写周报</span>
        </div>
        <div class="header-actions">
          <el-select v-model="selectedWeek" placeholder="选择周次" style="width: 140px" @change="handleWeekChange">
            <el-option 
              v-for="w in weekOptions" 
              :key="w.value" 
              :label="w.label" 
              :value="w.value"
              :disabled="w.disabled"
            >
              <span>{{ w.label }}</span>
              <el-tag v-if="w.submitted" size="small" type="success" style="margin-left: 8px">已提交</el-tag>
              <el-tag v-else-if="w.draft" size="small" type="warning" style="margin-left: 8px">草稿</el-tag>
            </el-option>
          </el-select>
          <el-button type="primary" plain @click="handleImport">
            <el-icon><Download /></el-icon>导入上周计划
          </el-button>
          <el-tooltip content="输入要点后点击，AI 将按固定周报模板（工作表格+问题风险+下周计划）丰富输出" placement="bottom">
            <el-button type="success" plain @click="handleAiDraft" :loading="aiDraftLoading" :disabled="reportSubmitted">
              <el-icon><MagicStick /></el-icon>AI 辅助撰写
            </el-button>
          </el-tooltip>
        </div>
      </div>
      
      <!-- 鍛ㄦ姤淇℃伅鎻愮ず -->
      <div class="week-info">
        <el-alert 
            v-if="isBackfill" 
            :title="`正在填写第${selectedWeek}周周报(${weekRange})`" 
            type="warning" 
            :closable="false"
            show-icon
          />
          <el-alert 
            v-else 
            :title="`当前第${selectedWeek}周(${weekRange})`" 
            type="info" 
            :closable="false"
            show-icon
          />
      </div>

      <p class="ai-hint">在各栏输入简单要点后，点击「AI 辅助撰写」将按固定模板（本周工作完成情况表格、问题/风险与思考、需支持事项、下周核心计划）自动丰富并填入下方各栏。</p>
      <el-form :model="form" label-position="top" class="report-form">
        <el-form-item label="本周工作完成情况">
          <div class="editor-container">
            <Toolbar :editor="thisWeekEditor" :defaultConfig="toolbarConfig" mode="default" class="editor-toolbar" />
            <Editor v-model="form.thisWeekWork" :defaultConfig="editorConfig" mode="default" class="editor-content" @onCreated="onThisWeekCreated" />
          </div>
        </el-form-item>
        
        <el-form-item label="下周工作计划">
          <div class="editor-container">
            <Toolbar :editor="nextWeekEditor" :defaultConfig="toolbarConfig" mode="default" class="editor-toolbar" />
            <Editor v-model="form.nextWeekPlan" :defaultConfig="editorConfig" mode="default" class="editor-content" @onCreated="onNextWeekCreated" />
          </div>
        </el-form-item>
        
        <el-form-item label="需协调事项">
          <div class="editor-container small">
            <Toolbar :editor="coordinationEditor" :defaultConfig="toolbarConfig" mode="default" class="editor-toolbar" />
            <Editor v-model="form.coordination" :defaultConfig="editorConfigSmall" mode="default" class="editor-content-small" @onCreated="onCoordinationCreated" />
          </div>
        </el-form-item>

        <el-form-item>
          <el-space>
            <el-button @click="handleSaveDraft" :loading="saving" :disabled="reportSubmitted">
              <el-icon><FolderOpened /></el-icon>暂存
            </el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitting" :disabled="reportSubmitted">
              <el-icon><Promotion /></el-icon>提交
            </el-button>
            <span v-if="reportSubmitted" class="submitted-tip">
              <el-icon color="#67c23a"><SuccessFilled /></el-icon>
              本周报已提交，不可修改
            </span>
          </el-space>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, shallowRef, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCurrentWeek, getCurrentReport, importLastWeek, saveDraft, submitReport, getMyReports, aiAssist } from '@/api'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import { MagicStick } from '@element-plus/icons-vue'

const currentWeek = ref(1)
const currentYear = ref(2024)
const selectedWeek = ref(1)
const saving = ref(false)
const submitting = ref(false)
const aiDraftLoading = ref(false)
const reportSubmitted = ref(false)
const myReports = ref([])

// 瀵屾枃鏈紪杈戝櫒瀹炰緥
const thisWeekEditor = shallowRef()
const nextWeekEditor = shallowRef()
const coordinationEditor = shallowRef()

const form = reactive({
  id: null,
  thisWeekWork: '',
  nextWeekPlan: '',
  coordination: '',
  year: 2024,
  weekNumber: 1
})

// 鏄惁鏄ˉ鍐?
const isBackfill = computed(() => selectedWeek.value < currentWeek.value)

// 鍛ㄦ閫夐」
const weekOptions = computed(() => {
  const options = []
  for (let i = 1; i <= currentWeek.value; i++) {
    const report = myReports.value.find(r => r.weekNumber === i && r.year === currentYear.value)
    options.push({
      value: i,
      label: `第${i}周`,
      submitted: report?.status === 1,
      draft: report?.status === 0,
      disabled: false
    })
  }
  return options.reverse()
})

// 鍛ㄨ寖鍥磋绠?
const weekRange = computed(() => {
  const startDate = new Date(currentYear.value, 0, 1 + (selectedWeek.value - 1) * 7)
  const endDate = new Date(startDate)
  endDate.setDate(endDate.getDate() + 4)
  const format = (d) => `${d.getMonth() + 1}.${d.getDate()}`
  return `${currentYear.value}.${format(startDate)} - ${currentYear.value}.${format(endDate)}`
})

// 宸ュ叿鏍忛厤缃?
const toolbarConfig = {
  excludeKeys: [
    'fullScreen',
    'group-video',
    'insertVideo',
    'uploadVideo',
    'codeBlock',
    'insertTable',
    'group-image'
  ]
}

// 编辑器配置?
const editorConfig = {
  placeholder: '请输入内容...',
  MENU_CONF: {},
  scroll: true,
  autoFocus: false
}

const editorConfigSmall = {
  ...editorConfig,
  placeholder: '如有需要协调或支持的事项请在此说明...'
}

// 编辑器创建回调?
const onThisWeekCreated = (editor) => {
  thisWeekEditor.value = editor
}

const onNextWeekCreated = (editor) => {
  nextWeekEditor.value = editor
}

const onCoordinationCreated = (editor) => {
  coordinationEditor.value = editor
}

// 缁勪欢閿€姣佹椂閿€姣佺紪杈戝櫒
onBeforeUnmount(() => {
  thisWeekEditor.value?.destroy()
  nextWeekEditor.value?.destroy()
  coordinationEditor.value?.destroy()
})

const loadReport = async (year, weekNumber) => {
  try {
    const reportRes = await getCurrentReport({ year, weekNumber }, { silent: true })
    if (reportRes?.code === 200 && reportRes.data) {
      Object.assign(form, reportRes.data)
      reportSubmitted.value = reportRes.data.status === 1
    } else {
      form.id = null
      form.thisWeekWork = ''
      form.nextWeekPlan = ''
      form.coordination = ''
      reportSubmitted.value = false
    }
  } catch (_) {
    form.id = null
    form.thisWeekWork = ''
    form.nextWeekPlan = ''
    form.coordination = ''
    reportSubmitted.value = false
  }
  form.year = year
  form.weekNumber = weekNumber
}

// 切换周次
const handleWeekChange = async (week) => {
  await loadReport(currentYear.value, week)
}

onMounted(async () => {
  try {
    const weekRes = await getCurrentWeek({ silent: true })
    if (weekRes?.code === 200 && weekRes.data) {
      currentWeek.value = weekRes.data.weekNumber
      currentYear.value = weekRes.data.year
      selectedWeek.value = currentWeek.value
      form.year = currentYear.value
      form.weekNumber = currentWeek.value
    }
  } catch (_) { /* 使用默认 */ }
  try {
    const reportsRes = await getMyReports({ silent: true })
    if (reportsRes?.code === 200) {
      myReports.value = reportsRes.data || []
    }
  } catch (_) { /* 列表失败 */ }
  try {
    await loadReport(currentYear.value, currentWeek.value)
  } catch (_) { /* 当前周报加载失败 */ }
})

const handleImport = async () => {
  const res = await importLastWeek({ year: currentYear.value, weekNumber: selectedWeek.value })
  if (res.code === 200 && res.data) {
    form.thisWeekWork = res.data
    ElMessage.success('已导入上周计划')
  } else {
    ElMessage.warning('没有找到上周计划')
  }
}

// 富文本转纯文本，供发给 AI 时使用
function stripHtml(html) {
  if (!html || typeof html !== 'string') return ''
  const div = document.createElement('div')
  div.innerHTML = html
  return (div.textContent || div.innerText || '').trim()
}
// AI 辅助撰写：把当前三栏内容作为草稿发给后端，AI 总结丰富后模板化输出并填回
const handleAiDraft = async () => {
  const draftThis = stripHtml(form.thisWeekWork)
  const draftNext = stripHtml(form.nextWeekPlan)
  const draftCoord = stripHtml(form.coordination)
  if (!draftThis && !draftNext && !draftCoord) {
    ElMessage.warning('请先在下方至少一栏输入简单要点，再点击 AI 辅助撰写')
    return
  }
  aiDraftLoading.value = true
  try {
    const res = await aiAssist({
      type: 'report',
      context: { currentWeek: selectedWeek.value },
      draft: {
        thisWeekWork: draftThis,
        nextWeekPlan: draftNext,
        coordination: draftCoord
      }
    }, { silent: true, timeout: 60000 })
    const raw = res?.data?.reply
    if (!raw || typeof raw !== 'string') {
      ElMessage.warning('未获取到草稿内容')
      return
    }
    let thisWeek = ''
    let nextWeek = ''
    let coordination = ''
    try {
      const str = raw.trim().replace(/^```(?:json)?\s*/i, '').replace(/\s*```$/i, '').trim()
      const data = JSON.parse(str)
      thisWeek = data.thisWeekWork != null ? String(data.thisWeekWork) : ''
      nextWeek = data.nextWeekPlan != null ? String(data.nextWeekPlan) : ''
      coordination = data.coordination != null ? String(data.coordination) : ''
    } catch (_) {
      form.thisWeekWork = raw
      ElMessage.info('已填入「本周工作」栏，请按需拆分到其他栏或修改')
      return
    }
    form.thisWeekWork = thisWeek
    form.nextWeekPlan = nextWeek
    form.coordination = coordination
    ElMessage.success('AI 草稿已填入，请修改后暂存或提交')
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '生成失败')
  } finally {
    aiDraftLoading.value = false
  }
}

const handleSaveDraft = async () => {
  saving.value = true
  try {
    await saveDraft(form)
    ElMessage.success('暂存成功')
    try {
      const reportsRes = await getMyReports({ silent: true })
      if (reportsRes?.code === 200) {
        myReports.value = reportsRes.data || []
      }
    } catch (_) {
      // 刷新列表失败不影响“暂存成功”提示，不弹请求失败
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '暂存失败')
  } finally {
    saving.value = false
  }
}

const handleSubmit = async () => {
  const confirmMsg = isBackfill.value 
    ? `确认提交第${selectedWeek.value}周的补写周报，提交后将无法修改` 
    : '确认提交周报，提交后将无法修改'
  
  await ElMessageBox.confirm(confirmMsg, '提示', { type: 'warning' })
  
  submitting.value = true
  try {
    await submitReport(form)
    ElMessage.success('提交成功')
    reportSubmitted.value = true
    try {
      const reportsRes = await getMyReports({ silent: true })
      if (reportsRes?.code === 200) {
        myReports.value = reportsRes.data || []
      }
    } catch (_) {
      // 刷新列表失败不影响“提交成功”提示，不弹请求失败
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || e?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss">
/* 鍏ㄥ眬鏍峰紡锛屼笉浣跨敤scoped锛岀‘淇漌angEditor鏍峰紡鐢熸晥 */
.write-report {
  .ai-hint {
    margin: 0 20px 12px;
    padding: 8px 12px;
    background: #f0f9ff;
    border-radius: 6px;
    font-size: 13px;
    color: #606266;
  }
  .card {
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.06);
    
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 20px;
      border-bottom: 1px solid #ebeef5;
      
      .title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 16px;
        font-weight: 600;
        color: #303133;
      }
      
      .header-actions {
        display: flex;
        gap: 12px;
        align-items: center;
      }
    }
  }
  
  .week-info {
    padding: 16px 20px 0;
  }

  .report-form {
    padding: 20px;
    
    .el-form-item__label {
      font-weight: 600;
      color: #303133;
    }
  }

  .editor-container {
    border: 1px solid #dcdfe6;
    border-radius: 4px;
    overflow: hidden;
    width: 100%;
    
    .editor-toolbar {
      border-bottom: 1px solid #dcdfe6 !important;
      background: #f5f7fa !important;
    }
    
    .editor-content {
      min-height: 300px !important;
      background: #fff;
      
      .w-e-text-container {
        min-height: 300px !important;
      }
    }
    
    .editor-content-small {
      min-height: 200px !important;
      background: #fff;
      
      .w-e-text-container {
        min-height: 200px !important;
      }
    }
  }
  
  .submitted-tip {
    display: flex;
    align-items: center;
    gap: 4px;
    color: #67c23a;
    font-size: 14px;
  }
}
</style>


