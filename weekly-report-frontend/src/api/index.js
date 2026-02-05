import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 线上部署时建议通过 Vercel 环境变量 VITE_API_BASE 配置后端地址
// - 本地开发默认仍走 Vite 代理：/api -> http://localhost:8080/api
// - 线上示例：VITE_API_BASE=https://your-backend-domain/api
const request = axios.create({
    baseURL: import.meta.env?.VITE_API_BASE || '/api',
    timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => Promise.reject(error)
)

// 响应拦截器：2xx 但 body 中 code 存在且不为 200 时视为业务错误并 reject（code 为 200 或 "200" 均视为成功）
request.interceptors.response.use(
    response => {
        const data = response.data
        if (response.status >= 200 && response.status < 300 && data && typeof data === 'object' && data.code !== undefined) {
            const code = Number(data.code)
            if (code !== 200) {
                const err = new Error(data.message || '请求失败')
                err.response = { status: data.code, data }
                err.config = response.config
                return Promise.reject(err)
            }
        }
        return data
    },
    error => {
        const silent = error.config?.silent === true
        if (error.response) {
            const { status, data } = error.response
            const msg = data?.message || '请求失败'
            if (status === 401) {
                if (router.currentRoute.value.path !== '/login') {
                    localStorage.removeItem('token')
                    localStorage.removeItem('userInfo')
                    router.push('/login')
                    ElMessage.error('登录已过期，请重新登录')
                }
            } else if (!silent) {
                ElMessage.error(msg)
            }
        } else if (!silent) {
            ElMessage.error('网络连接失败')
        }
        return Promise.reject(error)
    }
)

// 认证模块（登录/注册使用 silent，由页面统一提示）
export const login = async (data) => {
    try {
        const res = await request.post('/auth/login', data, { silent: true })
        return res
    } catch (error) {
        throw error
    }
}
export const register = async (data) => {
    try {
        const res = await request.post('/auth/register', data, { silent: true })
        return res
    } catch (error) {
        throw error
    }
}
export const getUserInfo = () => request.get('/auth/info')
export const logout = () => request.post('/auth/logout')

// 用户管理
export const getUserPage = (params, config) => request.get('/user/page', { params, ...config })
export const getUser = (id) => request.get(`/user/${id}`)
export const saveUser = (data) => request.post('/user', data)
export const updateUser = (data) => request.put('/user', data)
export const deleteUser = (id) => request.delete(`/user/${id}`)

// 角色管理
export const getRoleList = (config) => request.get('/role/list', config)
export const getRole = (id) => request.get(`/role/${id}`)
export const saveRole = (data) => request.post('/role', data)
export const updateRolePermissions = (id, permissions) => request.put(`/role/${id}/permissions`, { permissions })
export const deleteRole = (id) => request.delete(`/role/${id}`)

// 部门/组织机构管理
export const getDepartmentTree = (config) => request.get('/department/tree', config)
export const saveDepartment = (data) => request.post('/department', data)
export const deleteDepartment = (id) => request.delete(`/department/${id}`)

// 操作日志
export const getOperationLogPage = (params, config) => request.get('/operation-log/page', { params, ...config })

// 周报管理（第二个参数为 axios config，可传 { silent: true } 避免失败时全局弹窗）
export const getCurrentWeek = (config) => request.get('/report/current-week', config)
export const getMyReports = (config) => request.get('/report/my-list', config)
export const getCurrentReport = (params, config) => request.get('/report/current', { params, ...config })
export const importLastWeek = (params, config) => request.get('/report/import-last-week', { params, ...config })
export const saveDraft = (data) => request.post('/report/draft', data)
export const submitReport = (data) => request.post('/report/submit', data)
export const getTeamReports = (params, config) => request.get('/report/team', { params, ...config })
export const getReport = (id, config) => request.get(`/report/${id}`, config)
export const reviewReport = (id, data) => request.post(`/report/${id}/review`, data)

// 统计模块
export const getSubmitRate = (params, config) => request.get('/statistics/submit-rate', { params, ...config })
export const getScoreTrend = (config) => request.get('/statistics/score-trend', config)

// AI 助手（对话接口，建议 timeout 调大以等待模型响应）
export const aiAssist = (data, config) => request.post('/ai/assist', data, { timeout: 60000, ...config })

export default request
