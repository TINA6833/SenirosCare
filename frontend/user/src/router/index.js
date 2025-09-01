import { createRouter, createWebHistory } from "vue-router";
import IndexThree from "@/views/index/main-index.vue";
// Member
import AuthCallback from '@/views/auth/oauth2-callback.vue'
import MyAccount from '@/views/member/my-account.vue'

//device
import ProductList from '@/views/device/ProductList.vue'
import ProductDetail from '@/views/device/ProductDetail.vue'
import Wishlist from '@/views/device/Wishlist.vue'
import Cart from '@/views/device/Cart.vue'
import Orders from '@/views/device/Orders.vue'
import OrderDetail from '@/views/device/OrderDetail.vue'
import Checkout from '@/views/device/Checkout.vue'

// RoomType
import RoomFacilityPage from '@/views/roomType/RoomFacility.vue'
import RoomTypePage from '@/views/roomType/RoomType.vue'
import ContactPage from '@/views/roomType/contact-page.vue'

// Activity
import ActivityList from '@/views/activity/ActivityList.vue'
import ActivityDetails from '@/views/activity/ActivityDetails.vue'
import ActivityMyReservations from "@/views/activity/ActivityMyReservations.vue";

// Bus
import BusDetail from '@/views/rehabus/busDetail.vue';

// Caregiver - 保留現有的直接引入方式
import CaregiverList from '@/views/caregiver/CaregiverList.vue'
import CaregiverBooking from '@/views/caregiver/CaregiverBooking.vue'
import CaregiverReviews from '@/views/caregiver/CaregiverReviews.vue'


const routes = [
  {
    path: "/",
    name: "Home",
    component: IndexThree,
  },
  {
    path: "/index-v3",
    name: "IndexThree",
    component: IndexThree,
  },
  {
    path: "/auth/callback",
    name: "AuthCallback",
    component: AuthCallback,
  },
  {
    path: '/my-account',
    name: "MyAccount",
    component: MyAccount
  },
  // Device
  {
    path: "/products",
    name: "ProductList",
    component: ProductList,
  },
  {
    path: "/product-details/:id",
    name: "ProductDetail",
    component: ProductDetail,
    props: true
  },
  { path: '/wishlist', name: 'Wishlist', component: Wishlist },
  { path: '/cart', name: 'Cart', component: Cart },
  { path: '/orders', name: 'Orders', component: Orders },
  { path: '/orders/:id', name: 'OrderDetail', component: OrderDetail, props: true },
  { path: '/checkout', name: 'Checkout', component: Checkout },
  // Roomtype
  {
    path: "/facility",
    name: "Facility",
    component: RoomFacilityPage,
  },
  {
    path: "/roomType",
    name: "roomType",
    component: RoomTypePage,
  },
  {
    path: '/RoomType-details/:id',
    name: 'RoomTypeDetails',
    component: () => import('@/views/roomType/RoomType-details.vue')
  },
  // Activity
  {
    path: '/activity', name: 'ActivityList', component: ActivityList
  },
  {
    path: '/activitydetails/:id', name: 'ActivityDetails', component: ActivityDetails, props: true

  },
  
  {
    path: '/activity/my-reservations',
    name: 'ActivityMyReservations',
    component: ActivityMyReservations
  },
  {
    // 聯絡我們頁面
    path: '/contact',
    name: 'Contact',
    component: ContactPage
  },
  //Rehabus 詳細頁（用 :id）
  {
    path: "/rehabus",
    name: "復康巴士",
    component: () => import("@/views/rehabus/rehabusList.vue"),
  },
  {
    path: "/rehabus/:id",
    name: "bus-detail",
    component: BusDetail,
    props: true
  },
  // Caregiver
   {
  path: '/caregiver/:caregiverId/booking',
  name: 'CaregiverBooking',
  component: () => import('@/views/caregiver/CaregiverBooking.vue'),
  meta: { 
    title: "預約照服員",
    requiresAuth: true 
  }
},
  {
    path: "/caregiver",
    name: "CaregiverList",
    component: CaregiverList,
    meta: {
      title: "照服員列表",
      breadcrumb: "照服員",
      description: "瀏覽所有可預約的照服員",
      requiresAuth: false
    }
  },
  {
    // 照服員詳細頁面
    path: '/caregiver/:caregiverId(\\d+)',
    name: 'CaregiverDetail',
    component: () => import('@/views/caregiver/CaregiverDetail.vue'),
    props: true,
    meta: {
      title: '照服員詳情',
      breadcrumb: '照服員詳情',
      description: '查看照服員詳細資料與服務資訊',
      requiresAuth: false
    },
    beforeEnter: (to, from, next) => {
      // 驗證照服員ID是否為有效數字
      const caregiverId = parseInt(to.params.caregiverId)
      if (isNaN(caregiverId) || caregiverId <= 0) {
        next('/caregiver')
        return
      }
      next()
    }
  },
  {
    // 照服員預約頁面
    path: "/caregiver/:caregiverId(\\d+)/booking",
    name: "CaregiverBooking", 
    component: CaregiverBooking,
    props: true,
    meta: {
      title: "預約照服員",
      breadcrumb: "預約服務",
      description: "預約照服員服務",
      requiresAuth: true // 需要登入才能預約
    },
    beforeEnter: (to, from, next) => {
      // 驗證照服員ID是否為有效數字
      const caregiverId = parseInt(to.params.caregiverId)
      if (isNaN(caregiverId) || caregiverId <= 0) {
        next('/caregiver')
        return
      }
      next()
    }
  },
  {
    // 照服員評論頁面
    path: "/caregiver/:caregiverId(\\d+)/reviews",
    name: "CaregiverReviews",
    component: CaregiverReviews,
    props: true,
    meta: {
      title: "照服員評論",
      breadcrumb: "評論",
      description: "查看照服員評論與評分",
      requiresAuth: false
    },
    beforeEnter: (to, from, next) => {
      // 驗證照服員ID是否為有效數字
      const caregiverId = parseInt(to.params.caregiverId)
      if (isNaN(caregiverId) || caregiverId <= 0) {
        next('/caregiver')
        return
      }
      next()
    }
  },
  
  // Member Appointments 相關路由
  {
    path: '/my-appointments',
    name: 'MemberAppointments',
    component: () => import('@/views/caregiver/MemberAppointments.vue'),
    meta: {
      title: "我的預約",
      breadcrumb: "我的預約", 
      description: "查看與管理我的預約記錄",
      requiresAuth: true
    }
  },
  {
    path: '/appointments/:id(\\d+)',
    name: 'AppointmentDetail',
    component: () => import('@/views/caregiver/AppointmentDetail.vue'),
    props: true,
    meta: {
      title: "預約詳情",
      breadcrumb: "預約詳情",
      description: "查看預約詳細資訊",
      requiresAuth: true
    },
    beforeEnter: (to, from, next) => {
      // 驗證預約ID是否為有效數字
      const id = parseInt(to.params.id)
      if (isNaN(id) || id <= 0) {
        next('/my-appointments')
        return
      }
      next()
    }
  }
  
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;