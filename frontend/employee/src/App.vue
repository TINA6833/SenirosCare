<template>
  <div id="app">
    <Header v-if="showLayout" />
    <main :class="showLayout ? 'dashboard-main' : ''">
      <Navbar v-if="showLayout" />
      <router-view />
    </main>
    <!-- 通知訊息 -->
    <ToastContainer />
    <ConfirmDialog />
  </div>
</template>

<script>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from './components/layout/header/index.vue'
import Navbar from './components/layout/navbar/index.vue'
import ToastContainer from '@/components/ToastContainer.vue';

export default {
  name: 'App',
  components: {
    Header,
    Navbar,
    ToastContainer,
  },
  setup() {
    const route = useRoute()
    const showLayout = computed(() => route.meta.layout !== false)
    return { showLayout }
  }
}
</script>