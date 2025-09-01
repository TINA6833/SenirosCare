import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// Add this import for Iconify:
import { Icon } from '@iconify/vue'

// Importing Bootstrap and other global CSS files
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap'
import 'vue-slick-carousel/dist/vue-slick-carousel.css'
import 'vue-slick-carousel/dist/vue-slick-carousel-theme.css'

// Your other CSS files
import '@/assets/css/style.css'
import '@/assets/css/remixicon.css'
// 匯入全域元件
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
// Create and mount Vue app
const app = createApp(App)

// Register Iconify globally
app.component('iconify-icon', Icon)

// 建立 Pinia 實例
const pinia = createPinia()

// 在開發環境中啟用 Pinia DevTools
if (import.meta.env.DEV) {
  pinia.use(({ store }) => {
    // 讓 DevTools 能夠追蹤 store 的變化
    store.$subscribe((mutation, state) => {
      console.log('Pinia Store 狀態變更:', {
        storeId: mutation.storeId,
        type: mutation.type,
        state: state
      });
    });
  });
}
app.component('ConfirmDialog', ConfirmDialog)
app.use(pinia)
app.use(router)

app.mount('#app')

