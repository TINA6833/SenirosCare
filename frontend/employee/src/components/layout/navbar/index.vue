<template>
  <div class="navbar-header">
    <div class="row align-items-center justify-content-between">
      <div class="col-auto">
        <div class="d-flex flex-wrap align-items-center gap-4">
          <!-- 桌面端側邊欄切換按鈕 -->
          <button type="button" class="sidebar-toggle" @click="toggleSidebarDesktop">
            <iconify-icon :icon="isSidebarActive ? 'iconoir:arrow-right' : 'heroicons:bars-3-solid'"
              class="icon text-2xl"></iconify-icon>
          </button>

          <!-- 移動端側邊欄切換按鈕 -->
          <button type="button" @click="toggleSidebarMobile" class="sidebar-mobile-toggle">
            <iconify-icon icon="heroicons:bars-3-solid" class="icon"></iconify-icon>
          </button>
          
          <!-- 頁面標題顯示 -->
          <div class="page-title d-none d-md-block">
            <h5 class="mb-0 text-dark fw-semibold">{{ currentPageTitle || '首頁' }}</h5>
          </div>
        </div>
      </div>
      
      <div class="col-auto">
        <div class="d-flex flex-wrap align-items-center gap-3">
          <!-- 主題切換按鈕 -->
          <button type="button" @click="toggleTheme" data-theme-toggle
            class="w-40-px h-40-px bg-neutral-200 rounded-circle d-flex justify-content-center align-items-center"
            :aria-label="`切換到${theme === '🌙' ? '淺色' : '深色'}主題`"
            :title="`切換到${theme === '🌙' ? '淺色' : '深色'}主題`">
            {{ theme }}
          </button>

          <!-- 訊息下拉選單 -->
          <div class="dropdown">
            <button
              class="has-indicator w-40-px h-40-px bg-neutral-200 rounded-circle d-flex justify-content-center align-items-center"
              type="button" data-bs-toggle="dropdown"
              title="訊息">
              <iconify-icon icon="mage:email" class="text-primary-light text-xl"></iconify-icon>
            </button>
            <div class="dropdown-menu to-top dropdown-menu-lg p-0">
              <div
                class="m-16 py-12 px-16 radius-8 bg-primary-50 mb-16 d-flex align-items-center justify-content-between gap-2">
                <div>
                  <h6 class="text-lg text-primary-light fw-semibold mb-0">訊息</h6>
                </div>
                <span
                  class="text-primary-600 fw-semibold text-lg w-40-px h-40-px rounded-circle bg-base d-flex justify-content-center align-items-center">
                  {{ unreadMessages }}
                </span>
              </div>

              <div class="max-h-400-px overflow-y-auto scroll-sm pe-4">
                <!-- 訊息列表 - 暫時保留原有的靜態內容，之後可以改為動態資料 -->
                <a href="javascript:void(0)"
                  class="px-24 py-12 d-flex align-items-start gap-3 mb-2 justify-content-between">
                  <div class="text-black hover-bg-transparent hover-text-primary d-flex align-items-center gap-3">
                    <span class="w-40-px h-40-px rounded-circle flex-shrink-0 position-relative">
                      <img src="@/assets/images/notification/profile-3.png" alt="使用者頭像">
                      <span
                        class="w-8-px h-8-px bg-success-main rounded-circle position-absolute end-0 bottom-0"></span>
                    </span>
                    <div>
                      <h6 class="text-md fw-semibold mb-4">系統管理員</h6>
                      <p class="mb-0 text-sm text-secondary-light text-w-100-px">歡迎使用員工系統...</p>
                    </div>
                  </div>
                  <div class="d-flex flex-column align-items-end">
                    <span class="text-sm text-secondary-light flex-shrink-0">12:30 PM</span>
                    <span
                      class="mt-4 text-xs text-base w-16-px h-16-px d-flex justify-content-center align-items-center bg-warning-main rounded-circle">1</span>
                  </div>
                </a>
              </div>
              
              <div class="text-center py-12 px-16">
                <a href="javascript:void(0)" class="text-primary-600 fw-semibold text-md">查看所有訊息</a>
              </div>
            </div>
          </div><!-- 訊息下拉選單結束 -->

          <!-- 通知下拉選單 -->
          <div class="dropdown">
            <button
              class="has-indicator w-40-px h-40-px bg-neutral-200 rounded-circle d-flex justify-content-center align-items-center"
              type="button" data-bs-toggle="dropdown"
              title="通知">
              <iconify-icon icon="iconoir:bell" class="text-primary-light text-xl"></iconify-icon>
            </button>
            <div class="dropdown-menu to-top dropdown-menu-lg p-0">
              <div
                class="m-16 py-12 px-16 radius-8 bg-primary-50 mb-16 d-flex align-items-center justify-content-between gap-2">
                <div>
                  <h6 class="text-lg text-primary-light fw-semibold mb-0">通知</h6>
                </div>
                <span
                  class="text-primary-600 fw-semibold text-lg w-40-px h-40-px rounded-circle bg-base d-flex justify-content-center align-items-center">
                  {{ unreadNotifications }}
                </span>
              </div>

              <div class="max-h-400-px overflow-y-auto scroll-sm pe-4">
                <!-- 動態通知列表 -->
                <template v-if="notifications.length > 0">
                  <a v-for="notification in notifications" :key="notification.id"
                    href="javascript:void(0)"
                    class="px-24 py-12 d-flex align-items-start gap-3 mb-2 justify-content-between"
                    :class="{ 'bg-neutral-50': !notification.read }"
                    @click="markNotificationAsRead(notification)">
                    <div class="text-black hover-bg-transparent hover-text-primary d-flex align-items-center gap-3">
                      <span
                        class="w-44-px h-44-px rounded-circle d-flex justify-content-center align-items-center flex-shrink-0"
                        :class="notification.iconClass">
                        <iconify-icon :icon="notification.icon" class="icon text-xxl"></iconify-icon>
                      </span>
                      <div>
                        <h6 class="text-md fw-semibold mb-4">{{ notification.title }}</h6>
                        <p class="mb-0 text-sm text-secondary-light text-w-200-px">{{ notification.message }}</p>
                      </div>
                    </div>
                    <span class="text-sm text-secondary-light flex-shrink-0">{{ notification.timeAgo }}</span>
                  </a>
                </template>
                
                <!-- 無通知時顯示 -->
                <div v-else class="text-center py-4">
                  <iconify-icon icon="iconoir:bell-off" class="text-4xl text-neutral-400 mb-2"></iconify-icon>
                  <p class="text-neutral-500 mb-0">暫無通知</p>
                </div>
              </div>

              <div class="text-center py-12 px-16">
                <a href="javascript:void(0)" class="text-primary-600 fw-semibold text-md">查看所有通知</a>
              </div>
            </div>
          </div><!-- 通知下拉選單結束 -->

          <!-- 使用者個人資料下拉選單 -->
          <div class="dropdown">
            <button class="d-flex justify-content-center align-items-center rounded-circle" 
              type="button" data-bs-toggle="dropdown"
              :title="`${employeeName} 的選單`">
              <img 
                :src="userAvatar || defaultAvatar" 
                :alt="employeeName || '使用者頭像'" 
                class="w-40-px h-40-px object-fit-cover rounded-circle border border-2 border-white shadow-sm"
                @error="handleAvatarError">
            </button>
            
            <div class="dropdown-menu to-top dropdown-menu-sm">
              <!-- 使用者資訊頭部 -->
              <div
                class="py-12 px-16 radius-8 bg-primary-50 mb-16 d-flex align-items-center justify-content-between gap-2">
                <div>
                  <h6 class="text-lg text-primary-light fw-semibold mb-2">
                    {{ employeeName || '載入中...' }}
                  </h6>
                  <span class="text-secondary-light fw-medium text-sm">
                    {{ userRoleDisplay }}
                  </span>
                  <div class="text-xs text-muted mt-1">
                    {{ employeeEmail || '載入中...' }}
                  </div>
                </div>
                <button type="button" class="hover-text-danger" @click="closeUserDropdown">
                  <iconify-icon icon="radix-icons:cross-1" class="icon text-xl"></iconify-icon>
                </button>
              </div>
              
              <!-- 選單項目 -->
              <ul class="to-top-list">
                <li>
                  <router-link
                    class="dropdown-item text-black px-0 py-8 hover-bg-transparent hover-text-primary d-flex align-items-center gap-3"
                    to="/view-profile">
                    <iconify-icon icon="solar:user-linear" class="icon text-xl"></iconify-icon> 
                    我的個人資料
                  </router-link>
                </li>
                <li>
                  <a class="dropdown-item text-black px-0 py-8 hover-bg-transparent hover-text-danger d-flex align-items-center gap-3"
                    href="javascript:void(0)"
                    @click="handleLogout">
                    <iconify-icon icon="lucide:power" class="icon text-xl"></iconify-icon> 
                    登出系統
                  </a>
                </li>
              </ul>
            </div>
          </div><!-- 使用者下拉選單結束 -->
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useTheme } from '@/composables/useTheme.js'
import { useAuthStore } from '@/stores/authStore'
import { useConfirmDialog } from '@/composables/useConfirmDialog' // 新增：匯入確認對話框
// 匯入預設頭像圖片
import defaultAvatarImg from '@/assets/images/notification/profile-3.png'

// ===== Composables =====
const { theme, toggleTheme } = useTheme()
const authStore = useAuthStore()
const route = useRoute()
const { showConfirmDialog } = useConfirmDialog() // 新增：使用確認對話框

// ===== 響應式資料 =====
const isSidebarActive = ref(false)
const unreadMessages = ref(0 // 未讀訊息數量
)
const unreadNotifications = ref(0 // 未讀通知數量
)

// 預設頭像
const defaultAvatar = defaultAvatarImg

// 通知資料 (模擬資料，實際應該從 API 取得)
const notifications = ref([
  {
    id: 1,
    title: '歡迎使用系統',
    message: '歡迎使用員工管理系統，祝您使用愉快！',
    timeAgo: '1小時前',
    icon: 'bitcoin-icons:verify-outline',
    iconClass: 'bg-success-subtle text-success-main',
    read: false
  },
  {
    id: 2,
    title: '系統維護通知',
    message: '系統將於今晚進行例行維護，請提前保存工作',
    timeAgo: '3小時前',
    icon: 'solar:settings-outline',
    iconClass: 'bg-warning-subtle text-warning-main',
    read: true
  }
])

// ===== 計算屬性 =====

/**
 * 取得員工姓名
 */
const employeeName = computed(() => authStore.employeeName)

/**
 * 取得員工信箱
 */
const employeeEmail = computed(() => authStore.employeeEmail)

/**
 * 取得使用者頭像 URL
 */
const userAvatar = computed(() => {
  const avatar = authStore.employeeAvatar
  // 如果有頭像路徑，確保是完整的 URL
  if (avatar && !avatar.startsWith('http')) {
    return `http://localhost:8080${avatar}` // 根據後端配置調整
  }
  return avatar
})

/**
 * 取得使用者角色顯示文字
 */
const userRoleDisplay = computed(() => {
  const roles = authStore.employeeRoles
  if (!roles || roles.length === 0) return '一般員工'
  
  // 取第一個角色並格式化顯示
  const primaryRole = roles[0]
  if (primaryRole?.role_name) {
    const roleName = primaryRole.role_name.replace('ROLE_', '')
    const roleMapping = {
      'ADMIN': '系統管理員',
      'MANAGER': '主管',
      'USER': '一般員工',
      'EMPLOYEE': '員工'
    }
    return roleMapping[roleName] || roleName
  }
  
  return '一般員工'
})

/**
 * 取得當前頁面標題
 */
const currentPageTitle = computed(() => route.meta.title || '')

// ===== 方法定義 =====

/**
 * 切換桌面端側邊欄顯示/隱藏
 */
function toggleSidebarDesktop() {
  isSidebarActive.value = !isSidebarActive.value
  document.querySelector('.sidebar')?.classList.toggle('active')
  document.querySelector('.dashboard-main')?.classList.toggle('active')
}

/**
 * 切換移動端側邊欄顯示
 */
function toggleSidebarMobile() {
  document.querySelector('.sidebar')?.classList.add('sidebar-open')
  document.body.classList.add('overlay-active')
}

/**
 * 處理頭像載入錯誤
 */
function handleAvatarError(event) {
  console.log('Navbar: 頭像載入失敗，使用預設頭像')
  event.target.src = defaultAvatar
}

/**
 * 關閉使用者下拉選單
 */
function closeUserDropdown() {
  // 觸發 Bootstrap 下拉選單關閉
  const dropdown = document.querySelector('.dropdown.show')
  if (dropdown) {
    dropdown.classList.remove('show')
    dropdown.querySelector('.dropdown-menu')?.classList.remove('show')
  }
}

/**
 * 標記通知為已讀
 * @param {Object} notification - 通知物件
 */
function markNotificationAsRead(notification) {
  if (!notification.read) {
    notification.read = true
    updateUnreadCounts()
    console.log('Navbar: 標記通知為已讀', notification.id)
    
    // TODO: 這裡應該呼叫 API 更新後端的已讀狀態
    // await notificationService.markAsRead(notification.id)
  }
}

/**
 * 更新未讀數量
 */
function updateUnreadCounts() {
  unreadNotifications.value = notifications.value.filter(n => !n.read).length
  // unreadMessages.value 可以根據實際訊息資料更新
}

/**
 * 處理登出 - 修改：使用新的確認對話框
 */
async function handleLogout() {
  try {
    // 使用新的確認對話框取代 alert
    const confirmed = await showConfirmDialog({
      title: '登出系統',
      message: `${employeeName.value}，您確定要登出系統嗎？登出後您需要重新登入才能使用系統功能。`,
      type: 'warning',
      confirmText: '確認登出',
      cancelText: '取消',
      icon: 'lucide:power'
    });
    
    
    // 使用 Auth Store 的登出方法
    authStore.logout()
    
    // 關閉所有開啟的下拉選單
    closeUserDropdown()
    
    console.log('Navbar: 登出成功')
    
  } catch (error) {
    // 使用者取消登出或發生錯誤
    if (error === false) {
      console.log('Navbar: 使用者取消登出')
      return
    }
    
    console.error('Navbar: 登出失敗', error)
    
    // 可以使用 useToast 顯示錯誤訊息
    const { showToast } = await import('@/composables/useToast')
    showToast({
      title: '登出失敗',
      message: '登出過程中發生錯誤，請稍後再試',
      type: 'error'
    })
  }
}

// ===== 生命週期 =====

/**
 * 元件掛載時執行
 */
onMounted(() => {
  // 初始化未讀數量
  updateUnreadCounts()
  
  // 初始化 Auth Store（如果還沒初始化）
  if (!authStore.isAuthenticated) {
    authStore.initializeAuth()
  }
  
  console.log('Navbar: 導航欄元件已掛載')
})
</script>

<style scoped>
/* 側邊欄切換按鈕樣式 */
.sidebar-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  background: transparent;
  border: none;
  color: inherit;
  transition: all 0.3s ease;
}

.sidebar-toggle:hover {
  background-color: rgba(0, 0, 0, 0.05);
  border-radius: 6px;
}

/* 頁面標題樣式 */
.page-title h5 {
  color: var(--bs-dark, #333);
  font-weight: 600;
}

/* 使用者頭像邊框效果 */
.dropdown button img {
  transition: all 0.2s ease;
}

.dropdown button:hover img {
  transform: scale(1.05);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

/* 下拉選單項目 hover 效果改善 */
.dropdown-item:hover {
  background-color: rgba(13, 110, 253, 0.05);
  color: var(--bs-primary);
}

/* 通知和訊息按鈕 hover 效果 */
.has-indicator:hover {
  background-color: rgba(13, 110, 253, 0.1) !important;
  transform: translateY(-1px);
  transition: all 0.2s ease;
}

/* 主題切換按鈕 hover 效果 */
[data-theme-toggle]:hover {
  background-color: rgba(13, 110, 253, 0.1) !important;
  transform: scale(1.05);
  transition: all 0.2s ease;
}

/* 響應式調整 */
@media (max-width: 768px) {
  .page-title {
    display: none !important;
  }
  
  .navbar-header .col-auto:first-child .gap-4 {
    gap: 0.5rem !important;
  }
  
  .navbar-header .col-auto:last-child .gap-3 {
    gap: 0.5rem !important;
  }
}

/* 深色主題支援 */
[data-theme="dark"] .page-title h5 {
  color: var(--bs-light, #f8f9fa);
}

[data-theme="dark"] .sidebar-toggle:hover {
  background-color: rgba(255, 255, 255, 0.1);
}
</style>