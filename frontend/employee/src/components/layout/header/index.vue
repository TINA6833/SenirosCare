<template>
  <aside class="sidebar" :class="{ 'sidebar-open': isMobileOpen }">
    <!-- Mobile Close -->
    <button type="button" class="sidebar-close-btn" @click="closeSidebar">
      <iconify-icon icon="radix-icons:cross-2" />
    </button>

    <!-- Logo -->
    <router-link to="/" class="sidebar-logo">
      <img src="@/assets/images/logo.png" alt="Logo" class="light-logo" />
      <img src="@/assets/images/logo-light.png" alt="Logo" class="dark-logo" />
      <img src="@/assets/images/logo-icon.png" alt="Logo" class="logo-icon" />
    </router-link>

    <!-- Menu -->
    <div class="sidebar-menu-area">
      <ul class="sidebar-menu">
        <!-- Dashboard 首頁 -->
        <li>
          <router-link to="/" :class="{ active: isDashboardActive }" active-class="active">
            <iconify-icon icon="mdi:chart-box-outline" class="menu-icon" />
            <span>數據分析</span>
          </router-link>
        </li>

        <!-- 後台管理 -->
        <li class="sidebar-menu-group-title">資料管理</li>

        <!-- Member 會員管理 -->
        <li>
          <router-link to="/member" :class="{ active: isMemberActive }" active-class="active">
            <!-- 更換會員管理的 icon -->
            <iconify-icon icon="mdi:account-group-outline" class="menu-icon"></iconify-icon>
            <span>會員管理</span>
          </router-link>
        </li>

        <!-- Employee 員工管理 -->
        <li :class="{ dropdown: true, open: activeDropdown === 'employee' }">
          <a href="javascript:void(0)" @click="toggleDropdown('employee')" :class="{ active: isEmployeeActive }">
            <!-- 更換員工管理的 icon -->
            <iconify-icon icon="mdi:account-tie-outline" class="menu-icon"></iconify-icon>
            <span>員工管理</span>
            <span class="dropdown-arrow" :class="{ rotated: activeDropdown === 'employee' }"></span>
          </a>
          <transition @before-enter="beforeEnter" @enter="enter" @after-enter="afterEnter" @before-leave="beforeLeave"
            @leave="leave" @after-leave="afterLeave">
            <ul v-show="activeDropdown === 'employee'" ref="employeeMenu" class="sidebar-submenu">
              <li v-for="item in employeeItems" :key="item.path"
                :class="['nav-link', { 'active-page': isActive(item.path) }]">
                <router-link :to="item.path">
                  <i class="ri-circle-fill circle-icon" :class="item.colorClass" />{{ item.label }}</router-link>
              </li>
            </ul>
          </transition>
        </li>

        <!-- 復康巴士管理選單 -->
        <li :class="{ dropdown: true, open: activeDropdown === 'rehabus' }">
          <a href="javascript:void(0)" @click="toggleDropdown('rehabus')" :class="{ active: isRehabusActive }">
            <!-- 使用巴士相關的圖示 -->
            <iconify-icon icon="material-symbols:directions-bus-outline" class="menu-icon"></iconify-icon>
            <span>復康巴士管理</span>
            <span class="dropdown-arrow" :class="{ rotated: activeDropdown === 'rehabus' }"></span>
          </a>
          <transition @before-enter="beforeEnter" @enter="enter" @after-enter="afterEnter" @before-leave="beforeLeave"
            @leave="leave" @after-leave="afterLeave">
            <ul v-show="activeDropdown === 'rehabus'" ref="rehabusMenu" class="sidebar-submenu">
              <li v-for="item in rehabusItems" :key="item.path"
                :class="['nav-link', { 'active-page': isActive(item.path) }]">
                <router-link :to="item.path">
                  <i class="ri-circle-fill circle-icon" :class="item.colorClass" />{{ item.label }}</router-link>
              </li>
            </ul>
          </transition>
        </li>

        <!-- 預約管理選單 -->
        <li :class="{ dropdown: true, open: activeDropdown === 'reservation' }">
          <a href="javascript:void(0)" @click="toggleDropdown('reservation')" :class="{ active: isReservationActive }">
            <!-- 使用預約相關的圖示 -->
            <iconify-icon icon="mdi:calendar-clock" class="menu-icon"></iconify-icon>
            <span>預約管理</span>
            <span class="dropdown-arrow" :class="{ rotated: activeDropdown === 'reservation' }"></span>
          </a>
          <transition @before-enter="beforeEnter" @enter="enter" @after-enter="afterEnter" @before-leave="beforeLeave"
            @leave="leave" @after-leave="afterLeave">
            <ul v-show="activeDropdown === 'reservation'" ref="reservationMenu" class="sidebar-submenu">
              <li v-for="item in reservationItems" :key="item.path"
                :class="['nav-link', { 'active-page': isActive(item.path) }]">
                <router-link :to="item.path">
                  <i class="ri-circle-fill circle-icon" :class="item.colorClass" />{{ item.label }}</router-link>
              </li>
            </ul>
          </transition>
        </li>

        <!-- Room Type -->
        <li :class="{ dropdown: true, open: activeDropdown === 'room-type' }">
          <a href="javascript:void(0)" @click="toggleDropdown('room-type')" :class="{ active: isRoomTypeActive }">
            <iconify-icon icon="ph:door" class="menu-icon"></iconify-icon>
            <span>房型管理</span>
            <span class="dropdown-arrow" :class="{ rotated: activeDropdown === 'room-type' }"></span>
          </a>
          <transition @before-enter="beforeEnter" @enter="enter" @after-enter="afterEnter" @before-leave="beforeLeave"
            @leave="leave" @after-leave="afterLeave">
            <ul v-show="activeDropdown === 'room-type'" ref="roomTypeMenu" class="sidebar-submenu">
              <li v-for="item in roomTypeItems" :key="item.path"
                :class="['nav-link', { 'active-page': isActive(item.path) }]">
                <router-link :to="item.path">
                  <i class="ri-circle-fill circle-icon" :class="item.colorClass" />{{ item.label }}</router-link>
              </li>
            </ul>
          </transition>
        </li>

        <!-- deivece 輔具管理項目 -->
        <li :class="{ dropdown: true, open: activeDropdown === 'device' }">
          <a href="javascript:void(0)" @click="toggleDropdown('device')" :class="{ active: isDeviceActive }">
            <iconify-icon icon="ph:wheelchair-duotone" class="menu-icon"></iconify-icon>
            <span>輔具管理</span>
            <span class="dropdown-arrow" :class="{ rotated: activeDropdown === 'device' }"></span>
          </a>
          <transition @before-enter="beforeEnter" @enter="enter" @after-enter="afterEnter" @before-leave="beforeLeave"
            @leave="leave" @after-leave="afterLeave">
            <ul v-show="activeDropdown === 'device'" ref="deviceMenu" class="sidebar-submenu">
              <li v-for="item in deviceItems" :key="item.path"
                :class="['nav-link', { 'active-page': isActive(item.path) }]">
                <router-link :to="item.path">
                  <i class="ri-circle-fill circle-icon" :class="item.colorClass" />{{ item.label }}</router-link>
              </li>
            </ul>
          </transition>
        </li>

        <!-- Activity -->
        <li :class="{ dropdown: true, open: activeDropdown === 'activity' }">
          <a href="javascript:void(0)" @click="toggleDropdown('activity')" :class="{ active: isActivityActive }">
            <!-- 使用活動相關的圖示 -->
            <iconify-icon icon="mdi:calendar-star" class="menu-icon"></iconify-icon>
            <span>活動管理</span>
            <span class="dropdown-arrow" :class="{ rotated: activeDropdown === 'activity' }"></span>
          </a>
          <transition @before-enter="beforeEnter" @enter="enter" @after-enter="afterEnter" @before-leave="beforeLeave"
            @leave="leave" @after-leave="afterLeave">
            <ul v-show="activeDropdown === 'activity'" ref="activityMenu" class="sidebar-submenu">
              <li v-for="item in activityItems" :key="item.path"
                :class="['nav-link', { 'active-page': isActive(item.path) }]">
                <router-link :to="item.path">
                  <i class="ri-circle-fill circle-icon" :class="item.colorClass" />{{ item.label }}</router-link>
              </li>
            </ul>
          </transition>
        </li>

        <!-- 照服員管理選單 -->
        <li :class="{ dropdown: true, open: activeDropdown === 'caregiver' }">
          <a href="javascript:void(0)" @click="toggleDropdown('caregiver')" :class="{ active: isCaregiverActive }">
            <!-- 使用照服員相關的圖示 -->
            <iconify-icon icon="ph:users-three-duotone" class="menu-icon"></iconify-icon>
            <span>照服員管理</span>
            <span class="dropdown-arrow" :class="{ rotated: activeDropdown === 'caregiver' }"></span>
          </a>
          <transition @before-enter="beforeEnter" @enter="enter" @after-enter="afterEnter" @before-leave="beforeLeave"
            @leave="leave" @after-leave="afterLeave">
            <ul v-show="activeDropdown === 'caregiver'" ref="caregiverMenu" class="sidebar-submenu">
              <li v-for="item in caregiverItems" :key="item.path"
                :class="['nav-link', { 'active-page': isActive(item.path) }]">
                <router-link :to="item.path">
                  <i class="ri-circle-fill circle-icon" :class="item.colorClass" />{{ item.label }}</router-link>
              </li>
            </ul>
          </transition>
        </li>
      </ul>
    </div>
  </aside>
</template>


<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const activeDropdown = ref(null);
const isMobileOpen = ref(false);

// 首頁

// 判斷首頁選單是否應該高亮顯示
const isDashboardActive = computed(() => {
  return route.path === '/' || route.path === '/dashboard' || route.path === '/home';
});

// 判斷會員管理選單是否應該高亮顯示 
const isMemberActive = computed(() => {
  return route.path.startsWith('/member') || route.path === '/member' || route.path.includes('/member');
});

// 員工管理選單項目
const employeeItems = [
  { path: '/employee/list', label: '員工列表', colorClass: 'text-success-600 w-auto' },
  { path: '/employee/role', label: '權限管理', colorClass: 'text-warning-main w-auto' },
  { path: '/employee/admin', label: '停權管理', colorClass: 'text-danger-main w-auto' },
];

// 判斷員工管理選單是否應該高亮顯示
const isEmployeeActive = computed(() => employeeItems.some(item => isActive(item.path)));

const toggleDropdown = (name) => {
  activeDropdown.value = activeDropdown.value === name ? null : name;
  localStorage.setItem('activeDropdown', activeDropdown.value || '');
};

// 復康巴士管理選單項目
const rehabusItems = [
  { path: '/rehabus/list', label: '復康巴士列表', colorClass: 'text-primary-600 w-auto' },
  { path: '/rehabus/add', label: '新增復康巴士', colorClass: 'text-success-main w-auto' },
];

// 判斷復康巴士管理選單是否應該高亮顯示
const isRehabusActive = computed(() => rehabusItems.some(item => isActive(item.path)) ||
  route.path.startsWith('/rehabus/edit/'));

onMounted(() => {
  const savedDropdown = localStorage.getItem('activeDropdown');
  if (savedDropdown) {
    activeDropdown.value = savedDropdown;
  }
});

// 預約管理選單項目
const reservationItems = [
  { path: '/reservation/list', label: '預約列表', colorClass: 'text-primary-600 w-auto' },
  { path: '/reservation/add', label: '新增預約', colorClass: 'text-success-main w-auto' },
];

// 判斷預約管理選單是否應該高亮顯示
const isReservationActive = computed(() => reservationItems.some(item => isActive(item.path)) ||
  route.path.startsWith('/reservation/edit/'));

// 請建立房型管理的 roomTypeItems 陣列，格式同 employeeItems
const roomTypeItems = [
  { path: '/roomType/RoomList', label: '房型列表', colorClass: 'text-primary-600 w-auto' },
  { path: '/roomType/FacilityList', label: '設施列表', colorClass: 'text-success-main w-auto' },
  { path: '/roomType/FeatureList', label: '特徵標籤管理', colorClass: 'text-warning-main w-auto' },
  { path: '/roomType/ReservationList', label: '預約管理', colorClass: 'text-danger-main w-auto' },
  { path: '/roomType/CommentList', label: '留言管理', colorClass: 'text-warning-main w-auto' },
  { path: '/roomType/Dashboard', label: '統計儀表板', colorClass: 'text-info-main w-auto' },
];


// 判斷房型管理選單是否應該高亮顯示
const isRoomTypeActive = computed(() => roomTypeItems.some(item => isActive(item.path)));

// 新增輔具管理選單項目
const deviceItems = [
  { path: '/device/list', label: '輔具列表', colorClass: 'text-primary-600 w-auto' },
  { path: '/device/category', label: '輔具分類', colorClass: 'text-warning-main w-auto' },
  // 新增訂單管理
  { path: '/device/order', label: '訂單管理', colorClass: 'text-success-main w-auto' },
];

// 判斷輔具管理選單是否應該高亮顯示
const isDeviceActive = computed(() => deviceItems.some(item => isActive(item.path)));


// 活動管理選單項目
const activityItems = [
  { path: '/activity/list', label: '活動列表', colorClass: 'text-success-600 w-auto' },
  { path: '/activity/reservation', label: '活動報名管理', colorClass: 'text-warning-600 w-auto' },
];

// 判斷活動管理選單是否應該高亮顯示
const isActivityActive = computed(() => activityItems.some(item => isActive(item.path)));

// 照服員管理選單項目
const caregiverItems = [
  { path: '/caregiver/list', label: '照服員列表', colorClass: 'text-primary-600 w-auto' },
  { path: '/caregiver/appointments', label: '預約訂單列表', colorClass: 'text-success-main w-auto' }, 
  { path: '/caregiver/schedule', label: '照服員班表', colorClass: 'text-warning-main w-auto' },
];

// 判斷照服員管理選單是否應該高亮顯示
const isCaregiverActive = computed(() => caregiverItems.some(item => isActive(item.path)));

const closeSidebar = () => {
  isMobileOpen.value = false;
  document.body.classList.remove('overlay-active');
  const asideEl = document.querySelector('aside.sidebar');
  if (asideEl) {
    asideEl.classList.remove('sidebar-open');
  }
};

const goToRoute = (path) => {
  activeDropdown.value = null;
  closeSidebar()
  localStorage.removeItem('activeDropdown');
  router.push(path);
};

const isActive = (path) => route.path === path;

function beforeEnter(el) {
  el.style.height = '0px';
  el.style.opacity = '0';
  el.style.overflow = 'hidden';
}

function enter(el) {
  el.style.transition = 'height 0.7s ease';
  el.style.height = el.scrollHeight + 'px';
  el.style.opacity = '1';
}

function afterEnter(el) {
  el.style.height = 'auto';
  el.style.overflow = '';
  el.style.transition = '';
}

function beforeLeave(el) {
  el.style.height = el.scrollHeight + 'px';
  el.style.opacity = '1';
  el.style.overflow = 'hidden';
}

function leave(el) {
  el.style.transition = 'height 0.7s ease';
  requestAnimationFrame(() => {
    el.style.height = '0px';
    el.style.opacity = '0';
  });
}

function afterLeave(el) {
  el.style.height = '';
  el.style.opacity = '';
  el.style.transition = '';
  el.style.overflow = '';
}
</script>