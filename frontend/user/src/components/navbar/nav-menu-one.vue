<!-- eslint-disable vue/no-mutating-props -->
<template>
    <div class="flex items-center gap-4 sm:gap-6">
        <!-- [重點] 根據登入狀態顯示不同的使用者圖示 -->
        <router-link 
            to="/my-account" 
            class="text-title dark:text-white transition-all duration-300 hover:text-primary hidden lg:block relative group" 
            aria-label="會員中心"
        >
            <!-- [重點] 如果已登入且有頭像，顯示使用者頭像 -->
            <div v-if="isAuthenticated && userAvatar" class="relative">
                <img 
                    :src="userAvatar" 
                    :alt="currentUser?.memberName || '會員頭像'"
                    class="w-8 h-8 rounded-full object-cover border-2 border-transparent group-hover:border-primary transition-all duration-300"
                    @error="handleAvatarError"
                />
            </div>
            
            <!-- [重點] 如果未登入或沒有頭像，顯示預設圖示 -->
            <div v-else class="relative">
                <i class="mdi mdi-account-circle text-[32px] group-hover:text-primary transition-colors duration-300"></i>
            </div>
            
            <!-- [重點] Hover 提示保留 -->
            <div class="absolute top-full left-1/2 transform -translate-x-1/2 mt-2 px-2 py-1 bg-title dark:bg-white text-white dark:text-title text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity duration-300 whitespace-nowrap z-50">
                {{ isAuthenticated ? (currentUser?.memberName || '會員中心') : '請先登入' }}
            </div>
        </router-link>

        <!-- [重點] 手機版選單切換按鈕 -->
        <button class="hamburger" :class="toggle ? 'opened' : ''" @click="handleToggle">
            <svg class="stroke-current text-title dark:text-white" width="40" viewBox="0 0 100 100">
                <path class="line line1" d="M 20,29.000046 H 80.000231 C 80.000231,29.000046 94.498839,28.817352 94.532987,66.711331 94.543142,77.980673 90.966081,81.670246 85.259173,81.668997 79.552261,81.667751 75.000211,74.999942 75.000211,74.999942 L 25.000021,25.000058" />
                <path class="line line2" d="M 20,50 H 80" />
                <path class="line line3" d="M 20,70.999954 H 80.000231 C 80.000231,70.999954 94.498839,71.182648 94.532987,33.288669 94.543142,22.019327 90.966081,18.329754 85.259173,18.331003 79.552261,18.332249 75.000211,25.000058 75.000211,25.000058 L 25.000021,74.999942" />
            </svg>
        </button>

        <!-- [重點] 分隔線 -->
        <div class="w-[1px] bg-title/20 dark:bg-white/20 h-7 hidden sm:block"></div>

        <!-- [重點] 主題切換器 -->
        <SwitcherS/>
    </div>
</template>

<script setup>
    // [重點] Script 部分已移除願望清單和購物車相關邏輯
    import { ref, computed, watch, defineProps, defineEmits } from 'vue';
    import { useAuth } from '@/composables/useAuth';
    import SwitcherS from '../switcher-s.vue';

    // [重點] 定義元件 props
    const props = defineProps({
        toggle: {
            type: Boolean,
            default: false
        }
    });

    // [重點] 定義事件發射器
    const emit = defineEmits(['toggle-change']);

    // [重點] 定義響應式變數
    const avatarError = ref(false);

    // [重點] 使用認證相關 Composables
    const { isAuthenticated, currentUser } = useAuth();

    // [重點] 計算屬性：使用者頭像
    const userAvatar = computed(() => {
        if (!isAuthenticated.value || !currentUser.value || avatarError.value) {
            return null;
        }
        return currentUser.value.imagePath || null;
    });

    // [重點] 頭像載入失敗處理
    function handleAvatarError() {
        console.log('使用者頭像載入失敗，切換到預設圖示');
        avatarError.value = true;
    }

    // [重點] 手機版選單切換
    function handleToggle() {
        emit('toggle-change', !props.toggle);
    }

    // [重點] 監聽使用者變化，重置頭像錯誤狀態
    watch(currentUser, (newUser) => {
        if (newUser) {
            avatarError.value = false;
        }
    });

</script>

<style scoped>
/* [重點] 頭像相關樣式 */
.group:hover img {
    transform: scale(1.05);
}

/* [重點] 提示框樣式 */
.group:hover .absolute.top-full {
    pointer-events: none;
}

/* [重點] 下拉選單動畫 */
.wishlist_popup,
.hdr_cart_popup {
    animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
    from {
        opacity: 0;
        transform: translateY(-10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>
