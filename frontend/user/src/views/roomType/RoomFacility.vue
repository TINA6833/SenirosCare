<template>
    <div>
        <!-- 重點註解：統一導覽列元件，維持專案風格 -->
        <MainNavbar />

        <!-- 頂部留白，避免導覽列壓住內容 -->
        <div style="height: 140px;"></div>

        <!-- 主要內容區塊，依模板風格設計 -->
        <div class="flex items-center gap-4 flex-wrap bg-overlay p-14 sm:p-16 before:bg-title before:bg-opacity-70"
            :style="{ backgroundImage: 'url(' + bg + ')' }">
            <div class="text-center w-full">
                <h2 class="text-white text-8 md:text-[40px] font-normal leading-none text-center">設施介紹</h2>
                <ul
                    class="flex items-center justify-center gap-[10px] text-base md:text-lg leading-none font-normal text-white mt-3 md:mt-4">
                    <li><router-link to="/">首頁</router-link></li>
                    <li>/</li>
                    <li class="text-primary">設施介紹</li>
                </ul>
            </div>
        </div>

        <div class="s-py-100 overflow-hidden relative">
            <div class="container-fluid">
                <div class="max-w-[1720px] mx-auto" data-aos="fade-up" data-aos-delay="100">
                    <!-- 重點註解：只顯示 is_available 為 true 的設施，並依圖片大小排序 -->
                    <div
                        class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 lg:gap-6 md:gap-4 gap-2 -m-[10px] md:-m-[15px] mt-5">
                        <div class="portfolio1-item Sofa" v-for="(item, index) in sortedFacilities" :key="index">
                            <!-- 點擊圖片放大，不作頁面跳轉 -->
                            <div class="portfolio-card relative before:absolute before:top-0 before:left-0 before:w-full before:h-full before:opacity-0 before:duration-300 hover:before:opacity-100 group overflow-hidden block cursor-pointer"
                                @click="openImage(item)">
                                <!-- 圖片路徑直接用 Service 轉換後的 image 欄位 -->
                                <img class="w-full object-cover transition-transform duration-300 hover:scale-105"
                                    :src="item.image" alt="Portfolio" />
                                <span
                                    class="absolute left-7 bottom-7 z-10 transform translate-y-8 duration-300 opacity-0 group-hover:translate-y-0 group-hover:opacity-100 block">
                                    <span
                                        class="block text-lg md:text-xl font-semibold leading-none text-white mt-3 md:mt-4">
                                        {{ item.name }}
                                    </span>
                                    <span class="text-base md:text-lg font-normal text-white">
                                        {{ item.description }}
                                    </span>
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- 放大圖片的遮罩與彈窗 -->
        <div v-if="showImage" class="fixed inset-0 bg-black bg-opacity-70 flex items-center justify-center z-50"
            @click="closeImage">
            <div class="bg-white rounded-lg p-6 max-w-lg w-full relative" @click.stop>
                <img :src="selectedFacility.image" alt="放大設施圖片" class="w-full rounded mb-4" />
                <h3 class="text-xl font-bold mb-2">{{ selectedFacility.name }}</h3>
                <p class="text-gray-700">{{ selectedFacility.description }}</p>
                <button class="absolute top-2 right-2 text-gray-500 hover:text-gray-900 text-2xl"
                    @click="closeImage">&times;</button>
            </div>
        </div>

        <!-- 頁尾元件，依模板設計 -->
        <FooterThree class="bg-gray-100 py-8" />
        <ScrollToTop />
    </div>
</template>

<script setup>
// 重點註解：導覽列元件統一使用 main-navbar.vue
import MainNavbar from '@/components/navbar/main-navbar.vue';
import ScrollToTop from '@/components/scroll-to-top.vue';

// 已移除未使用的 FooterOne import
import bg from '@/assets/img/shortcode/breadcumb.jpg'
import Aos from 'aos';
import { onMounted, ref, computed } from 'vue';
import { getAllFacilities } from '@/service/facilityService'
import FooterThree from '@/components/footer/footer-three.vue';

// 取得設施資料（資料庫）
const allFacilities = ref([])

// 放大圖片狀態
const showImage = ref(false)
const selectedFacility = ref({})

// 只顯示 isAvailable 為 true 並依圖片大小排序
const sortedFacilities = computed(() => {
    const available = allFacilities.value.filter(item => item.isAvailable === true)
    return [...available].sort((a, b) => a.image.length - b.image.length)
})

// 點擊圖片放大
function openImage(facility) {
    selectedFacility.value = facility
    showImage.value = true
}
// 點擊遮罩關閉放大圖
function closeImage() {
    showImage.value = false
}

onMounted(async () => {
    Aos.init()
    // 從資料庫取得設施資料（已由 Service 轉型，image 欄位為完整網址）
    allFacilities.value = await getAllFacilities()
})
</script>
