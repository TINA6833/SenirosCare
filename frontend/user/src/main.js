import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'

import '@fortawesome/fontawesome-free/css/all.min.css'

import './assets/css/style.css'
import 'aos/dist/aos.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.mount('#app')
