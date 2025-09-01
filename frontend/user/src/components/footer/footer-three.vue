<template>
    <div class="s-pt-100 bg-snow dark:bg-title 2xl:pb-[100px]">
        <div class="container-fluid max-w-[1720px] mx-auto">
            <div class="flex flex-wrap items-start justify-between gap-10">
                <!-- 公司簡介與聯絡資訊 -->
                <div class="max-w-[400px] w-full text-left sm:text-center 2xl:text-left sm:mx-auto 2xl:mx-0 flex-shrink-0">
                    <router-link class="cursor-pointer block" to="/" aria-label="樂齡e站">
                        <img :src="logoDark" alt="樂齡e站" class="dark:hidden w-full max-w-[200px]" />
                        <img :src="logoLight" alt="樂齡e站" class="dark:block hidden w-full max-w-[200px]" />
                    </router-link>
                    <div class="mt-4 text-title dark:text-white text-sm">
                        <div class="flex items-center gap-2">
                            <i class="fas fa-phone-alt"></i>
                            <a href="tel:02-12345678">03-1234-5678</a>
                        </div>
                        <div class="flex items-center gap-2">
                            <i class="fas fa-map-marker-alt"></i>
                            桃園市中壢區新生北路二段421號
                        </div>
                        <div class="flex items-center gap-2">
                            <i class="fas fa-envelope"></i>
                            <a href="mailto:service@aging.com.tw">service@aging.com.tw</a>
                        </div>
                    </div>
                </div>

                <!-- 服務導覽與客戶服務 -->
                <div class="flex flex-wrap gap-10 flex-grow">
                    <!-- 服務導覽 -->
                    <div class="flex-1">
                        <h4 class="mb-5 text-xl">服務導覽</h4>
                        <ul class="text-title dark:text-white flex flex-col gap-4">
                            <li><router-link to="/products">輔具買賣</router-link></li>
                            <li><router-link to="/rooms">房型介紹</router-link></li>
                            <li><router-link to="/bus">租巴士服務</router-link></li>
                            <li><router-link to="/events">活動資訊</router-link></li>
                            <li><router-link to="/caregivers">照服員專區</router-link></li>
                        </ul>
                    </div>

                    <!-- 客戶服務 -->
                    <div class="flex-1">
                        <h4 class="mb-5 text-xl">客戶服務</h4>
                        <ul class="text-title dark:text-white flex flex-col gap-4">
                            <li><router-link to="/faq">常見問題</router-link></li>
                            <li><router-link to="/contact">聯絡我們</router-link></li>
                            <li><router-link to="/visit">預約參觀</router-link></li>
                            <li><router-link to="/feedback">意見反應</router-link></li>
                        </ul>
                    </div>
                </div>

                <!-- 訂閱電子報 -->
                <div class="max-w-[300px] w-full flex-shrink-0">
                    <h4 class="mb-5 text-xl">訂閱電子報</h4>
                    <div class="flex flex-col gap-2">
                        <input class="w-full h-12 bg-transparent border border-primary p-4 outline-none dark:text-white"
                            type="text" placeholder="輸入您的Email">
                        <button class="w-full h-12 bg-primary text-white rounded transition hover:bg-primary-dark">
                            <span>訂閱</span>
                        </button>
                    </div>
                    <div class="text-xs text-gray-400 mt-2">掌握最新活動與優惠資訊</div>
                </div>
            </div>

            <!-- 版權與專題用途說明 -->
            <p class="text-title dark:text-white pb-1 mt-12 text-center">
                © {{year}} 樂齡e站 | 專業養老院與長照服務
            </p>
            <p class="text-xs text-gray-500 text-center mb-5">
                此專題僅限資展國際報告使用
            </p>
        </div>
    </div>
</template>

<script setup>
import logoDark from '@/assets/img/svg/logo.png'
import logoLight from '@/assets/img/svg/logo-light.svg'
import { ref } from 'vue'
const year = ref(new Date().getFullYear())
</script>

<style scoped>
/* 調整間距與排版 */
.flex-wrap {
    flex-wrap: wrap;
}

.gap-10 {
    gap: 10px; /* 縮小間距 */
}

.flex-1 {
    flex: 1; /* 讓服務導覽與客戶服務平分空間 */
}

.max-w-[200px] {
    max-width: 200px;
}

.max-w-[300px] {
    max-width: 300px;
}
</style>