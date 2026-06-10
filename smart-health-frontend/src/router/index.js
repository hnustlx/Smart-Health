import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getToken, getUser, setToken, setUser } from '../utils/auth'

const skipAuth = import.meta.env.VITE_SKIP_AUTH === 'true'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue'),
    meta: { public: true }
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../views/admin/AdminLoginView.vue'),
    meta: { public: true, adminPublic: true }
  },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/HomeView.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/ProfileView.vue')
      },
      {
        path: 'weight',
        name: 'Weight',
        component: () => import('../views/WeightView.vue')
      },
      {
        path: 'plan',
        name: 'Plan',
        component: () => import('../views/PlanView.vue')
      },
      {
        path: 'history',
        name: 'History',
        component: () => import('../views/HistoryView.vue')
      },
      {
        path: 'chat',
        name: 'Chat',
        component: () => import('../views/ChatView.vue')
      },
      {
        path: 'vip',
        name: 'VipUpgrade',
        component: () => import('../views/VipUpgradeView.vue')
      },
      {
        path: 'ai-config',
        name: 'AiConfig',
        component: () => import('../views/AiConfigView.vue')
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, roles: ['ADMIN'], adminArea: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('../views/admin/AdminDashboardView.vue')
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('../views/admin/AdminUsersView.vue')
      },
      {
        path: 'users/:id',
        name: 'AdminUserDetail',
        component: () => import('../views/admin/AdminUserDetailView.vue')
      },
      {
        path: 'users/:id/weights',
        name: 'AdminUserWeights',
        component: () => import('../views/admin/AdminUserWeightsView.vue')
      },
      {
        path: 'users/:id/plans',
        name: 'AdminUserPlans',
        component: () => import('../views/admin/AdminUserPlansView.vue')
      },
      {
        path: 'knowledge',
        name: 'AdminKnowledge',
        component: () => import('../views/admin/AdminKnowledgeView.vue')
      },
      {
        path: 'vip-codes',
        name: 'AdminVipCodes',
        component: () => import('../views/admin/AdminVipCodesView.vue')
      },
      {
        path: 'ai-configs',
        name: 'AdminAiConfig',
        component: () => import('../views/admin/AdminAiConfigView.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.meta.public) {
    if (to.meta.adminPublic && getToken() && getUser()?.role === 'ADMIN') {
      return '/admin'
    }
    return true
  }

  let token = getToken()
  if (skipAuth && !token) {
    token = 'frontend-dev-token'
    setToken(token)
    setUser({
      userId: 0,
      username: 'frontend_dev',
      role: 'USER'
    })
  }
  if (skipAuth) {
    const user = getUser()
    if (user?.username === 'frontend_dev' && user.role !== 'USER' && !sessionStorage.getItem('vip_preview_enabled')) {
      setUser({
        ...user,
        role: 'USER'
      })
    }
  }

  if (!token) {
    if (to.path.startsWith('/admin')) {
      return { path: '/admin/login', query: { redirect: to.fullPath } }
    }
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  const roles = to.meta.roles
  if (roles?.length) {
    const user = getUser()
    if (!roles.includes(user?.role)) {
      ElMessage.warning('无权限访问该页面')
      if (to.path.startsWith('/admin')) {
        return '/admin/login'
      }
      return '/home'
    }
  }

  return true
})

export default router
