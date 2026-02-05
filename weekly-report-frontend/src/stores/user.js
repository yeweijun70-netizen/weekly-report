import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo } from '@/api'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

    const isLoggedIn = computed(() => {
        return !!token.value
    })

    /**
     * @returns {{ success: true } | { success: false, reason: 'network' | 'credentials' }}
     */
    async function login(email, password) {
        try {
            console.log('开始登录：', email, password)
            const res = await loginApi({ email, password })
            console.log('登录响应:', res)
            if (res.code === 200 && res.data && res.data.token) {
                token.value = res.data.token
                userInfo.value = res.data.user
                localStorage.setItem('token', res.data.token)
                localStorage.setItem('userInfo', JSON.stringify(res.data.user))
                console.log('登录成功，token已保存')
                return { success: true }
            } else {
                console.error('登录失败：', res.message || '响应格式错误')
                return { success: false, reason: 'credentials' }
            }
        } catch (error) {
            console.error('登录请求失败:', error)
            return { success: false, reason: 'network' }
        }
    }

    /**
     * 注册并自动登录
     * @returns {{ success: true } | { success: false, reason: 'network' | string }}
     */
    async function register(username, email, password) {
        try {
            const res = await registerApi({ username, email, password })
            if (res.code === 200 && res.data && res.data.token) {
                token.value = res.data.token
                userInfo.value = res.data.user
                localStorage.setItem('token', res.data.token)
                localStorage.setItem('userInfo', JSON.stringify(res.data.user))
                return { success: true }
            }
            return { success: false, reason: res.message || '注册失败' }
        } catch (error) {
            const msg = error?.response?.data?.message || error?.message
            return { success: false, reason: msg || 'network' }
        }
    }

    async function fetchUserInfo() {
        try {
            const res = await getUserInfo()
            if (res.code === 200) {
                userInfo.value = res.data
                localStorage.setItem('userInfo', JSON.stringify(res.data))
            }
        } catch (error) {
            console.error('获取用户信息失败:', error)
        }
    }

    function logout() {
        token.value = ''
        userInfo.value = {}
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        console.log('已登出，token已清除')
    }

    function clearToken() {
        token.value = ''
        userInfo.value = {}
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        console.log('token已清除')
    }

    return {
        token,
        userInfo,
        isLoggedIn,
        login,
        register,
        fetchUserInfo,
        logout,
        clearToken
    }
})


