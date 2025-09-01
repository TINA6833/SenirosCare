import { createRouter, createWebHistory } from "vue-router";
import IndexThree from "@/views/index/main-index.vue";
import LoginPage from '@/views/member/login-page.vue'

// 新增商品頁面
import ProductList from '@/views/device/ProductList.vue'
import ProductDetail from '@/views/device/ProductDetail.vue'
import Wishlist from '@/views/device/Wishlist.vue'
import Cart from '@/views/device/Cart.vue'
import Orders from '@/views/device/Orders.vue'
import OrderDetail from '@/views/device/OrderDetail.vue'
import Checkout from '@/views/device/Checkout.vue'

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
    path: "/login",
    name: "Login",
    component: LoginPage,
  },
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
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;
