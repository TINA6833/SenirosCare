<template>
    <div>
        <MainNavbar />
        <div style="height: 140px;"></div>

        <!-- 頁首大圖區 -->
        <div class="flex items-center gap-4 flex-wrap bg-overlay p-14 sm:p-16 before:bg-title before:bg-opacity-70"
            :style="{ backgroundImage: 'url(' + bg + ')' }">
            <div class="text-center w-full">
                <h2 class="text-white text-8 md:text-[40px] font-normal leading-none text-center">
                    活動
                </h2>
                <ul
                    class="flex items-center justify-center gap-[10px] text-base md:text-lg leading-none font-normal text-white mt-3 md:mt-4">
                    <li><router-link to="/">首頁</router-link></li>
                    <li>/</li>
                    <li class="text-primary">活動列表</li>
                </ul>
            </div>
        </div>

        <!-- Swiper：活動精選 -->
        <div class="s-py-100-50 overflow-hidden" data-aos="fade-up">
            <div class="relative">
                <div>
                    <Swiper :modules="[Autoplay, Navigation]" :slides-per-view="3" :space-between="20" :loop="true"
                        :navigation="true" :autoplay="{ delay: 3000 }" :breakpoints="{
                            320: { slidesPerView: 1, spaceBetween: 0 },
                            576: { slidesPerView: 2, spaceBetween: 0 },
                            1024: { slidesPerView: 3, spaceBetween: 0 },
                            1200: { slidesPerView: 4, spaceBetween: 0 }
                        }" class="owl-carousel mx-auto block">
                        <SwiperSlide v-for="act in activities.slice(0, 8)" :key="'slide-' + act.id"
                            class="relative before:absolute before:top-0 before:left-0 before:w-full before:h-full before:bg-title before:bg-opacity-70 group before:opacity-0 before:duration-300 hover:before:opacity-100 overflow-hidden before:z-10 block">
                            <!-- 這整塊都可點，會把 activity 透過 state 帶到詳情頁 -->
                            <router-link class="block"
                                :to="{ name: 'ActivityDetails', params: { id: act.id }, state: { activity: act } }">
                                <img class="w-full h-[220px] object-cover transform duration-300 group-hover:scale-110"
                                    :src="act.imageUrl || fallbackImg" :alt="act.name" />
                                <div
                                    class="absolute z-20 w-full h-full flex top-0 left-0 flex-col items-center justify-center px-5">
                                    <h3
                                        class="text-white leading-tight font-semibold transform -translate-y-5 opacity-0 duration-300 group-hover:-translate-y-0 group-hover:opacity-100 text-center">
                                        {{ act.name }}
                                    </h3>
                                    <span
                                        class="text-white leading-none mt-3 transform translate-y-5 opacity-0 duration-300 group-hover:translate-y-0 group-hover:opacity-100">
                                        {{ formatDate(act.date) }} ｜ {{ act.location || '—' }}
                                    </span>
                                </div>
                            </router-link>
                        </SwiperSlide>
                    </Swiper>
                </div>
            </div>
        </div>

        <!-- 篩選列 -->
        <div class="s-py-50-100" data-aos="fade-up">
            <div class="container-fluid">
                <!-- 活動狀態切換按鈕 -->
                <div class="flex items-center justify-center gap-4 mb-8">
                    <button 
                        @click="activityStatusFilter = 'available'" 
                        class="px-6 py-3 rounded-md transition-all"
                        :class="activityStatusFilter === 'available' ? 
                            'bg-primary text-white font-medium' : 
                            'bg-gray-100 text-gray-600 hover:bg-gray-200'"
                    >
                        可報名活動
                    </button>
                    <button 
                        @click="activityStatusFilter = 'ended'" 
                        class="px-6 py-3 rounded-md transition-all"
                        :class="activityStatusFilter === 'ended' ? 
                            'bg-primary text-white font-medium' : 
                            'bg-gray-100 text-gray-600 hover:bg-gray-200'"
                    >
                        已結束活動
                    </button>
                </div>

                <div class="flex items-center lg:justify-center gap-6 flex-wrap">
                    <!-- 類別 -->
                    <div class="flex items-start sm:items-center gap-[15px] flex-wrap sm:flex-nowrap sm:max-w-[420px] w-full flex-col sm:flex-row">
                        <h4 class="font-medium leading-none text-xl flex-none">活動分類</h4>
                        <div class="nice-select bg-white dark:bg-dark outline-select small-select"
                            :class="openCategory ? 'open' : ''" @click="openCategory = !openCategory">
                            <span class="current">{{ selectedCategory || '全部' }}</span>
                            <ul class="list">
                                <li class="option" @click.stop="selectCategory('')">全部</li>
                                <li class="option" v-for="c in categories" :key="c" @click.stop="selectCategory(c)">
                                    {{ c }}
                                </li>
                            </ul>
                        </div>
                    </div>

                    <div class="hidden 2xl:block">
                        <svg width="24" height="41" viewBox="0 0 24 41" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <line x1="0.566987" y1="40.6689" x2="23.567" y2="0.831777" stroke="#BB976D" />
                        </svg>
                    </div>

                    <!-- 排序 -->
                    <div class="flex items-start sm:items-center gap-[15px] flex-wrap sm:flex-nowrap sm:max-w-[347px] w-full flex-col sm:flex-row">
                        <h4 class="font-medium leading-none text-xl flex-none">排序</h4>
                        <div class="nice-select bg-white dark:bg-dark outline-select small-select"
                            :class="openSort ? 'open' : ''" @click="openSort = !openSort">
                            <span class="current">{{ selectedSort.label }}</span>
                            <ul class="list">
                                <li v-for="opt in sortOptions" :key="opt.value" class="option"
                                    @click.stop="selectSort(opt)">
                                    {{ opt.label }}
                                </li>
                            </ul>
                        </div>
                    </div>

                    <div class="hidden 2xl:block">
                        <svg width="24" height="41" viewBox="0 0 24 41" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <line x1="0.566987" y1="40.6689" x2="23.567" y2="0.831777" stroke="#BB976D" />
                        </svg>
                    </div>

                    <!-- 我的預約按鈕 -->
                    <div class="flex items-center">
                        <router-link to="/activity/my-reservations" 
                            class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark">
                            我的預約
                        </router-link>
                    </div>
                </div>

                <!-- 活動卡片 Grid -->
                <div class="max-w-[1720px] mx-auto mt-8 md:mt-12" data-aos="fade-up" data-aos-delay="200">
                    <!-- 載入/錯誤 -->
                    <div v-if="loading" class="text-center py-10">載入中…</div>
                    <div v-else-if="error" class="text-center py-10 text-red-600">錯誤：{{ error }}</div>
                    <!-- 無資料提示 -->
                    <div v-else-if="filteredActivities.length === 0" class="text-center py-16">
                        <div class="text-5xl text-gray-300 mb-4">
                            <i class="fas fa-calendar-xmark"></i>
                        </div>
                        <p class="text-xl text-gray-500">
                            {{ activityStatusFilter === 'available' ? '目前沒有可報名的活動' : '目前沒有已結束的活動' }}
                        </p>
                    </div>

                    <div v-else
                        class="grid sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-5 lg:gap-[30px]">
                        <!-- 已結束活動視覺標記 -->
                        <article v-for="act in filteredActivities" :key="act.id"
                            class="border p-3 rounded relative group overflow-hidden"
                            :class="{'opacity-75': activityStatusFilter === 'ended'}">
                            <!-- 已結束活動狀態標籤 -->
                            <div v-if="activityStatusFilter === 'ended'" 
                                class="absolute top-0 right-0 bg-gray-700 text-white px-3 py-1 text-xs z-20">
                                已結束
                            </div>
                            
                            <!-- 圖片也可點 -->
                            <router-link
                                :to="{ name: 'ActivityDetails', params: { id: act.id }, state: { activity: act } }"
                                class="block">
                                <img class="w-full h-[180px] object-cover rounded" 
                                    :src="act.imageUrl || fallbackImg" :alt="act.name" />
                            </router-link>

                            <div class="mt-3">
                                <!-- 標題可點 -->
                                <h3 class="text-lg font-semibold leading-tight line-clamp-2">
                                    <router-link class="hover:underline"
                                        :to="{ name: 'ActivityDetails', params: { id: act.id }, state: { activity: act } }">
                                        {{ act.name }}
                                    </router-link>
                                </h3>

                                <p class="text-sm mt-1 opacity-80">
                                    {{ formatDate(act.date) }}｜{{ act.time || '時間未定' }}
                                </p>
                                <p class="text-sm mt-1 opacity-80">
                                    {{ act.location || '地點未定' }}
                                </p>
                                <p class="text-xs mt-1 opacity-60">
                                    目前 {{ act.current ?? 0 }}/{{ act.limit ?? '-' }}
                                </p>

                                <!-- 詳情按鈕也可點 -->
                                <router-link
                                    :to="{ name: 'ActivityDetails', params: { id: act.id }, state: { activity: act } }"
                                    class="px-6 py-3 rounded-md transition-all bg-primary text-white font-medium hover:bg-primary-dark mt-3 inline-block">
                                    查看詳情
                                </router-link>
                            </div>
                        </article>
                    </div>
                </div>
            </div>
        </div>

        <FooterThree />
        <ScrollToTop />
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

import MainNavbar from '@/components/navbar/main-navbar.vue'
import FooterThree from '@/components/footer/footer-three.vue'
import ScrollToTop from '@/components/scroll-to-top.vue'

import bg from '@/assets/img/shortcode/breadcumb.jpg'
const fallbackImg = 'https://via.placeholder.com/600x400?text=Activity'

// Swiper
import { Swiper, SwiperSlide } from 'swiper/vue'
import { Autoplay, Navigation } from 'swiper/modules'
import 'swiper/swiper-bundle.css'

// AOS 動畫
import Aos from 'aos'
import 'aos/dist/aos.css'

// 使用 Service 層而非直接呼叫 API
import { activityService } from '@/service/activityService'

const activities = ref([])
const loading = ref(true)
const error = ref('')

// 篩選/排序
const openCategory = ref(false)
const selectedCategory = ref('')
const categories = ref(['課程', '展覽', '團康', '講座']) // 先寫死；之後可打 /api/activity-categories

const openSort = ref(false)
const sortOptions = [
    { value: 'date-asc', label: '日期（舊→新）' },
    { value: 'date-desc', label: '日期（新→舊）' },
    { value: 'name-asc', label: '名稱（A→Z）' },
    { value: 'name-desc', label: '名稱（Z→A）' }
]
const selectedSort = ref(sortOptions[1])

function selectCategory(c) {
    selectedCategory.value = c
    openCategory.value = false
}
function selectSort(opt) {
    selectedSort.value = opt
    openSort.value = false
}

// [重點] 新增活動狀態篩選
const activityStatusFilter = ref('available') // 預設顯示可報名活動

// [重點] 修改 filteredActivities 計算屬性來處理活動狀態篩選
const filteredActivities = computed(() => {
    let list = [...activities.value]
    
    // [重點] 先依據活動狀態篩選
    if (activityStatusFilter.value === 'available') {
        list = list.filter(a => a.status === true)
    } else if (activityStatusFilter.value === 'ended') {
        list = list.filter(a => a.status === false)
    }
    
    // 再依據類別篩選
    if (selectedCategory.value) {
        list = list.filter(a => (a.category || '') === selectedCategory.value)
    }
    
    // 最後排序
    switch (selectedSort.value.value) {
        case 'date-asc':
            list.sort((a, b) => (a.date || '').localeCompare(b.date || ''))
            break
        case 'date-desc':
            list.sort((a, b) => (b.date || '').localeCompare(a.date || ''))
            break
        case 'name-asc':
            list.sort((a, b) => (a.name || '').localeCompare(b.name || ''))
            break
        case 'name-desc':
            list.sort((a, b) => (b.name || '').localeCompare(a.name || ''))
            break
    }
    return list
})

function formatDate(d) {
    if (!d) return '日期未定'
    return d
}

onMounted(async () => {
    Aos.init()
    try {
        // 使用 activityService 取得處理過的活動資料
        activities.value = await activityService.getAllActivities()
    } catch (e) {
        console.error(e)
        error.value = e?.message || '載入失敗'
    } finally {
        loading.value = false
    }
})
</script>

<style scoped>
/* [重點] 活動切換按鈕樣式 */
.activity-status-btn {
    transition: all 0.3s ease;
}
</style>
