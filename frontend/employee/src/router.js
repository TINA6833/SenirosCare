import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/authStore"; // 引入 Auth Store

// ===== 頁面元件導入 =====

// 首頁
import Ai from './pages/dashboard/ai.vue'

// error
import Error from "./pages/error/error.vue";
import BadRequest from "./pages/error/bad-request.vue";
import ServiceUnavailable from "./pages/error/service-unavailable.vue";
import InternalServer from "./pages/error/internal-server.vue";
import Forbidden from "./pages/error/forbidden.vue";

// Employee
import EmpList from "./pages/employee/empList.vue";
import EmpRole from "./pages/employee/empRole.vue";
import EmpAdmin from "./pages/employee/empAdmin.vue";
import ViewProfile from "./pages/employee/view-profile.vue";

// Member - 會員管理模組
import Member from "./pages/member/member.vue";

// authentication
import SignIn from "./pages/authentication/sign-in.vue";
import ForgotPassword from "./pages/authentication/forgot-password.vue";

// ================================
// rehabus 模組 
// ================================
import RehabusList from './pages/rehabus/rehabus-list.vue';
import RehabusAdd from './pages/rehabus/rehabus-add.vue';
import RehabusEdit from './pages/rehabus/rehabus-edit.vue';
import BusReservation from './pages/rehabus/reservation-list.vue';
import ReservationAdd from './pages/rehabus/reservation-add.vue';
import ReservationEdit from './pages/rehabus/reservation-edit.vue';

// ================================
// 🏠 roomType 模組 - 現有實作的頁面
// ================================
import RoomList from './pages/roomType/RoomList.vue'              // roomType: 房型列表頁面（整合搜尋和匯入匯出功能）
import RoomAdd from './pages/roomType/RoomAdd.vue'                // roomType: 新增房型頁面
import RoomEdit from './pages/roomType/RoomEdit.vue'              // roomType: 編輯房型頁面（整合 RoomTypeFeatureController）
import RoomPreview from './pages/roomType/RoomPreview.vue'        // roomType: 房型詳情預覽（整合收藏統計）
import FacilityList from './pages/roomType/FacilityList.vue'
import ReservationList from './pages/roomType/ReservationList.vue';
import FeatureList from './pages/roomType/FeatureList.vue';
import Dashbroard from './pages/roomType/Dashbroard.vue'; // 房型統計儀表板元件


// device 輔具管理相關頁面
import Device from './pages/device/device.vue';
import DeviceCategory from './pages/device/category.vue';
import Order from './pages/device/order.vue'

// Activity - 活動管理模組
import ActivityBlog from "./pages/activity/activityblog.vue";
import ActivityReservationList from "./pages/activity/activityReservationList.vue";

// Caregiver
import CaregiverList from "./pages/caregiver/CaregiverList.vue";
import AddCareWorker from "./components/Caregiver/AddCareWorker.vue";
import EditCareWorker from "./pages/caregiver/EditCareWorker.vue";
import ViewCareWorker from "./pages/caregiver/ViewCareWorker.vue";

/**
 * 檢查使用者認證狀態的輔助函數
 * 使用 Pinia Auth Store 進行認證檢查
 * @returns {boolean} 是否已通過認證
 */
function isAuthenticated() {
  try {
    // 使用 Pinia Auth Store 檢查認證狀態
    const authStore = useAuthStore();

    // 首先嘗試從 store 檢查
    if (authStore.isAuthenticated) {
      return true;
    }

    // 如果 store 中沒有認證資訊，嘗試初始化
    authStore.initializeAuth();
    return authStore.isAuthenticated;
  } catch (error) {
    console.error("Router: 檢查認證狀態時發生錯誤:", error);
    return false;
  }
}

// ===== 路由定義 =====
const routes = [
  // 首頁
  {
    path: "/",
    component: Ai,
    meta: {
      requiresAuth: true,
      title: "首頁",
    },
  },
  // 員工列表頁面 Employee
  {
    path: "/employee/list",
    component: EmpList,
    meta: {
      requiresAuth: true,
      title: "員工列表",
    },
  },
  {
    path: "/employee/role",
    component: EmpRole,
    meta: {
      requiresAuth: true,
      title: "員工角色管理",
    },
  },
  {
    path: "/employee/admin",
    component: EmpAdmin,
    meta: {
      requiresAuth: true,
      title: "員工管理",
    },
  },
  {
    path: "/view-profile",
    component: ViewProfile,
    meta: {
      requiresAuth: true,
      title: "檢視個人資料",
    },
  },

  // Member - 會員管理路由
  {
    path: "/member",
    component: Member,
    meta: {
      requiresAuth: true,
      title: "會員管理",
    },
  },

  // Rehabus (復康巴士管理)
  {
    path: '/rehabus/list',
    component: RehabusList,
    meta: {
      title: '復康巴士列表'
    }
  },
  {
    path: '/rehabus/add',
    component: RehabusAdd,
    meta: {
      title: '新增復康巴士'
    }
  },
  {
    path: '/rehabus/edit/:id',
    component: RehabusEdit,
    meta: {
      title: '編輯復康巴士'
    }
  },

  // Reservation (預約管理)
  {
    path: '/reservation/list',
    component: BusReservation,
    meta: {
      title: '預約列表'
    }
  },
  {
    path: '/reservation/add',
    component: ReservationAdd,
    meta: {
      title: '新增預約'
    }
  },
  {
    name: 'reservation-edit',
    path: '/reservation/edit/:id(\\d+)',   // 只接受數字 id，避免亂跳
    component: ReservationEdit,
    props: route => ({ id: Number(route.params.id) }), // 直接以 props 傳給頁面
    meta: { title: '編輯預約' }
  },

  // ================================
  // 🏠 roomType 模組 - 房型管理路由（已實作）
  // ================================
  {
    path: '/room-types',
    redirect: '/room-types/list',
    meta: {
      title: '房型管理',
      module: 'roomType',
      description: 'roomType 模組主路由，重新導向到房型列表'
    }
  },
  {
    path: '/room-types/list',
    component: RoomList,
    meta: {
      title: '房型列表',
      module: 'roomType',
      apis: ['RoomTypeController.getAllRoomTypes', 'RoomTypeController.searchRoomTypes', 'RoomTypeController.importCSV', 'RoomTypeController.exportCSV'],
      description: 'roomType: 房型列表頁面，整合搜尋和匯入匯出功能',
      breadcrumb: ['房型管理', '房型列表']
    }
  },
  {
    path: '/room-types/add',
    component: RoomAdd,
    meta: {
      title: '新增房型',
      module: 'roomType',
      apis: ['RoomTypeController.addRoomType', 'FeatureController.getAllFeatures'],
      description: 'roomType: 新增房型頁面，支援圖片上傳和特徵關聯',
      breadcrumb: ['房型管理', '新增房型']
    }
  },
  {
    path: '/room-types/:id/edit',
    component: RoomEdit,
    props: true,
    meta: {
      title: '編輯房型',
      module: 'roomType',
      apis: ['RoomTypeController.updateRoomType', 'RoomTypeFeatureController', 'FeatureController.getAllFeatures'],
      description: 'roomType: 編輯房型頁面，整合特徵關聯管理',
      breadcrumb: ['房型管理', '編輯房型']
    }
  },
  {
    path: '/room-types/:id/preview',
    component: RoomPreview,
    props: true,
    meta: {
      title: '房型詳情',
      module: 'roomType',
      apis: ['RoomTypeController.getRoomTypeById', 'FavoriteRoomController.getFavoriteCountByRoomType', 'RoomCommentController.getCommentsByRoomType'],
      description: 'roomType: 房型詳情頁面，整合收藏統計和評論顯示',
      breadcrumb: ['房型管理', '房型詳情']
    }
  },
  {
    path: '/roomType/FacilityList',
    component: FacilityList,
    meta: {
      title: '設施列表',
      module: 'roomType',
      description: 'roomType: 設施列表頁面',
      breadcrumb: ['房型管理', '設施列表']
    }
  },
  {
    path: '/roomType/ReservationList',
    component: ReservationList,
    meta: {
      title: '預約管理',
      module: 'roomType',
      description: 'roomType: 預約狀態管理頁面',
      breadcrumb: ['房型管理', '預約管理']
    }
  },
  {
    path: '/roomType/CommentList',
    component: () => import('@/pages/roomType/CommentList.vue'),
    meta: {
      title: '留言管理',
      module: 'roomType',
      description: 'roomType: 留言審核管理頁面',
      breadcrumb: ['房型管理', '留言管理']
    }
  },
  {
    path: '/roomType/FeatureList',
    component: FeatureList,
    meta: {
      title: '特徵標籤管理',
      module: 'roomType',
      description: 'roomType: 特徵標籤管理頁面',
      breadcrumb: ['房型管理', '特徵標籤管理']
    }
  },


  // ================================
  // 🏠 roomType 模組 - 新增統計儀表板路由
  // ================================
  {
    path: '/roomType/Dashboard',
    component: Dashbroard,
    meta: {
      title: '統計儀表板',
      module: 'roomType',
      description: '房型統計儀表板頁面',
      breadcrumb: ['房型管理', '統計儀表板']
    }
  },


  // ================================
  // 🔄 roomType 模組向下相容路由重新導向
  // ================================
  { path: '/roomType/RoomList', redirect: '/room-types/list', meta: { deprecated: true, newPath: '/room-types/list' } },
  { path: '/roomType/RoomAdd', redirect: '/room-types/add', meta: { deprecated: true, newPath: '/room-types/add' } },
  { path: '/roomType/RoomEdit/:id', redirect: to => `/room-types/${to.params.id}/edit`, meta: { deprecated: true } },
  { path: '/roomType/RoomPreview/:id', redirect: to => `/room-types/${to.params.id}/preview`, meta: { deprecated: true } },


  // device 輔具管理相關路由 - 需要認證
  {
    path: '/device/list',
    component: Device,
    name: 'DeviceList',
    meta: {
      title: '輔具列表'
    }
  },

  {
    path: '/device/category',
    component: DeviceCategory,
    name: 'DeviceCategory',
    meta: {
      title: '輔具分類管理'
    }
  },
  {
    path: '/device/order',
    component: Order,
    name: 'OrderList',
    meta: {
      title: '訂單列表'
    }
  },


  // Activity - 活動管理路由 - 需要認證
  {
    path: "/activity/list",
    component: ActivityBlog,
    meta: {
      requiresAuth: true,
      title: "活動管理",
    },
  },
  {
    path: "/activity/reservation",
    component: ActivityReservationList,
    meta: {
      requiresAuth: true,
      title: "活動報名管理",
    },
  },

  // caregiver - 需要認證
  // caregiver - 需要認證
 {
    path: '/caregiver/list',
    name: 'CaregiverList',
    component: () => import('@/pages/caregiver/CaregiverList.vue')
  },
  {
    path: '/caregiver/appointments', // 對應 order-list
    name: 'AppointmentList',
    component: () => import('@/pages/caregiver/AppointmentList.vue')
  },
  {
    path: '/caregiver/appointments/:id', // 預約詳情頁面
    name: 'AppointmentDetail',
    component: () => import('@/pages/caregiver/AppointmentDetail.vue'),
    props: true,
    meta: {
      requiresAuth: true,
      title: "預約詳情",
    }
  },
  {
    path: '/caregiver/appointments/:id/edit', // 編輯預約頁面
    name: 'AppointmentEdit',
    component: () => import('@/pages/caregiver/AppointmentEdit.vue'),
    props: true,
    meta: {
      requiresAuth: true,
      title: "編輯預約訂單",
    }
  },
  {
    path: '/caregiver/schedule',
    name: 'ScheduleList', 
    component: () => import('@/pages/caregiver/ScheduleList.vue')
  },
  {
    path: "/add-care-worker",
    name: "AddCareWorker",
    component: AddCareWorker,
    meta: {
      requiresAuth: true,
      title: "新增照服員",
    },
  },
  {
    path: "/care-worker/edit/:id",
    name: "EditCareWorker",
    component: EditCareWorker,
    props: true,
    meta: {
      requiresAuth: true,
      title: "編輯照服員",
    },
  },
  {
    path: "/care-worker/edit",
    name: "EditCareWorkerRedirect",
    redirect: "/caregiver/list",
    meta: {
      requiresAuth: true,
    },
  },
  {
    path: "/care-worker/:id",
    name: "ViewCareWorker",
    component: ViewCareWorker,
    props: true,
    meta: {
      requiresAuth: true,
      title: "照服員詳細資料",
    },
  },
  {
    path: "/care-worker",
    name: "ViewCareWorkerRedirect",
    redirect: "/caregiver/list",
    meta: {
      requiresAuth: true,
    },
  },
  
  // Authentication - 登入頁面
  {
    path: "/sign-in",
    component: SignIn,
    meta: {
      layout: false,
      requiresAuth: false,
      title: "員工登入",
    },
  },
  {
    path: "/forgot-password",
    component: ForgotPassword,
    meta: {
      layout: false,
      requiresAuth: false,
      title: "忘記密碼",
    },
  },

  // error - status code pages
  {
    path: "/error",
    component: Error,
    meta: {
      requiresAuth: false,
      title: "頁面不存在 (404)",
    },
  },
  {
    path: "/bad-request",
    component: BadRequest,
    meta: {
      requiresAuth: false,
      title: "請求參數錯誤 (400)",
    },
  },
  {
    path: "/service-unavailable",
    component: ServiceUnavailable,
    meta: {
      requiresAuth: false,
      title: "服務無法使用 (503)",
    },
  },
  {
    path: "/internal-server",
    component: InternalServer,
    meta: {
      requiresAuth: false,
      title: "伺服器錯誤 (500)",
    },
  },
  {
    path: "/forbidden",
    component: Forbidden,
    meta: {
      requiresAuth: false,
      title: "權限不足 (403)",
    },
  },
  // 404 頁面處理
  {
    path: "/:pathMatch(.*)*",
    redirect: "/error",
    meta: {
      requiresAuth: false,
    },
  },
];

// ===== 建立路由實例 =====
const router = createRouter({
  history: createWebHistory(),
  routes,
});

// ===== 全域路由守衛 =====

/**
 * 全域前置守衛 - 在每次路由跳轉前檢查認證狀態
 */
router.beforeEach((to, from, next) => {
  console.log(`Router: 路由跳轉 ${from.path} -> ${to.path}`);

  // 檢查目標路由是否需要認證（預設需要認證，除非明確設定為 false）
  const requiresAuth = to.meta.requiresAuth !== false;

  if (requiresAuth) {
    // 需要認證的路由
    if (isAuthenticated()) {
      next();
    } else {
      // 如果當前不是在登入頁面，則重導向到登入頁面
      if (to.path !== "/sign-in") {
        next("/sign-in");
      } else {
        next();
      }
    }
  } else {
    // 不需要認證的頁面

    // 如果已經登入且嘗試訪問登入頁面，重導向到首頁
    if (to.path === "/sign-in" && isAuthenticated()) {
      next("/");
    } else {
      next();
    }
  }
});

/**
 * 全域後置守衛 - 設定頁面標題
 */
router.afterEach((to) => {
  // 設定頁面標題
  const title = to.meta.title || "員工管理系統";
  document.title = title;
});

export default router;