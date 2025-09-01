<template>
    <div>
        <MainNavbar />
        <div style="height: 140px;"></div>
        <!-- 重點註解：地點資訊頁標題與麵包屑 -->
        <div class="flex items-center gap-4 flex-wrap bg-overlay p-14 sm:p-16 before:bg-title before:bg-opacity-70"
            :style="{ backgroundImage: 'url(' + bg + ')' }">
            <div class="text-center w-full">
                <h2 class="text-white text-8 md:text-[40px] font-normal leading-none text-center">地點資訊</h2>
                <ul
                    class="flex items-center justify-center gap-[10px] text-base md:text-lg leading-none font-normal text-white mt-3 md:mt-4">
                    <li><router-link to="/">首頁</router-link></li>
                    <li>/</li>
                    <li class="text-primary">地圖</li>
                </ul>
            </div>
        </div>

        <!-- 重點註解：主要內容區塊，左側照片，右側地圖資訊 -->
        <div class="s-pb-100 s-pt-100">
            <div class="container-fluid">
                <div class="max-w-[1720px] mx-auto flex flex-col lg:flex-row justify-between gap-8">
                    <!-- 左側：照片（已換成指定路徑圖片） -->
                    <div class="max-w-[894px] w-full hidden lg:block" data-aos="zoom-in">
                        <img class="w-full rounded-lg shadow" src="@/views/roomType/image/養老院.jpg" alt="養老院">
                    </div>
                    <!-- 右側：整合式地圖資訊卡 -->
                    <div class="max-w-[725px] w-full mx-auto lg:mx-0">
                        <div class="bg-white rounded-lg shadow p-8" data-aos="fade-up">
                            <h3 class="leading-none font-semibold text-2xl mb-6 text-primary">Google 地圖資訊</h3>
                            <ul class="space-y-3 text-base md:text-lg font-medium">
                                <li>
                                    <span class="text-gray-700">地點名稱：</span>
                                    <span class="font-bold text-title">SeniorsCare 養老院</span>
                                </li>
                                <li>
                                    <span class="text-gray-700">地址：</span>
                                    <span class="font-bold text-title">桃園市中壢區新生路二段421號</span>
                                </li>
                                <li>
                                    <span class="text-gray-700">電話：</span>
                                    <span class="font-mono text-title">03-1234-5678</span>
                                </li>
                                <li>
                                    <span class="text-gray-700">Email：</span>
                                    <span class="font-mono text-title">service@aging.com.tw</span>
                                </li>
                                <li>
                                    <span class="text-gray-700">參訪時間：</span>
                                    <span class="inline-block bg-primary text-white px-3 py-1 rounded ml-2">
                                        周一至周日 09:00 - 16:30
                                    </span>
                                </li>
                            </ul>
                            <p class="mt-6 text-gray-600">下方可直接操作地圖，查看詳細位置。</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 重點註解：Google 地圖互動區 -->
        <div class="s-pb-100" data-aos="fade-up">
            <div class="container-fluid">
                <div class="max-w-[1720px] mx-auto">
                    <div id="google-map" style="width:100%;height:600px"></div>
                </div>
            </div>
        </div>

        <FooterThree />
        <ScrollToTop />
    </div>
</template>

<script setup>
// 重點註解：引入必要元件與地圖 API
import { onMounted } from 'vue'
import Aos from 'aos'
import MainNavbar from '@/components/navbar/main-navbar.vue'
import FooterThree from '@/components/footer/footer-three.vue' // ★修正路徑
import ScrollToTop from '@/components/scroll-to-top.vue'
import bg from '@/assets/img/shortcode/breadcumb.jpg'
import { loadGoogleMap } from '@/service/googleMapsService'

onMounted(() => {
    Aos.init()
    // 重點註解：直接載入 Google 地圖，座標與名稱固定
    loadGoogleMap('google-map', 24.985128, 121.221719, 'SeniorsCare 養老院')
})
</script>
