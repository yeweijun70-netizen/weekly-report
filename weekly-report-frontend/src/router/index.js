import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
    {
        path: '/',
        redirect: '/dashboard'
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: { title: '登录' }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/Register.vue'),
        meta: { title: '注册' }
    },
    {
        path: '/',
        component: () => import('@/views/Layout.vue'),
        meta: { requiresAuth: true },
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/Dashboard.vue'),
                meta: { title: '工作台' }
            },
            {
                path: 'write-report',
                name: 'WriteReport',
                component: () => import('@/views/WriteReport.vue'),
                meta: { title: '写周报' }
            },
            {
                path: 'team-report',
                name: 'TeamReport',
                component: () => import('@/views/TeamReport.vue'),
                meta: { title: '团队周报' }
            },
            {
                path: 'analytics',
                name: 'Analytics',
                component: () => import('@/views/Analytics.vue'),
                meta: { title: '统计分析' }
            },
            {
                path: 'user-management',
                name: 'UserManagement',
                component: () => import('@/views/UserManagement.vue'),
                meta: { title: '用户管理' }
            },
            {
                path: 'role-management',
                name: 'RoleManagement',
                component: () => import('@/views/RoleManagement.vue'),
                meta: { title: '角色管理' }
            },
            {
                path: 'org-management',
                name: 'OrgManagement',
                component: () => import('@/views/OrgManagement.vue'),
                meta: { title: '组织机构管理' }
            },
            {
                path: 'operation-log',
                name: 'OperationLog',
                component: () => import('@/views/OperationLog.vue'),
                meta: { title: '操作日志管理' }
            },
            {
                path: 'messages',
                name: 'Messages',
                component: () => import('@/views/Messages.vue'),
                meta: { title: '消息中心' }
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/views/Profile.vue'),
                meta: { title: '个人中心' }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    document.title = to.meta.title ? `${to.meta.title} - 周报系统` : '周报系统'

    const userStore = useUserStore()

    if (to.path === '/login' || to.path === '/register') {
        if (userStore.isLoggedIn) {
            next('/dashboard')
        } else {
            next()
        }
    } else {
        if (userStore.isLoggedIn) {
            next()
        } else {
            next('/login')
        }
    }
})

export default router
