<template>
    <div>
        <!-- 重點註解：主導航欄統一使用 MainNavbar -->
        <MainNavbar />

        <!-- 重點註解：頂部留白，避免導覽列壓住內容 -->
        <div style="height: 140px;"></div>

        <!-- 重點註解：使用 AuthPrompt 包裝房型頁面，需要登入才能查看房型和進行預約 -->
        <AuthPrompt title="房型介紹" message="請先登入以查看房型資訊並進行預約" login-button-text="使用 Line 登入" :show-toast="true">
            <!-- 重點註解：原房型頁面內容，只有在登入後才會顯示 -->

            <!-- 主要內容區塊 -->
            <div class="flex items-center gap-4 flex-wrap bg-overlay p-14 sm:p-16 before:bg-title before:bg-opacity-70"
                :style="{ backgroundImage: 'url(' + bg + ')' }">
                <div class="text-center w-full">
                    <h2 class="text-white text-8 md:text-[40px] font-normal leading-none text-center">房型介紹</h2>
                    <ul
                        class="flex items-center justify-center gap-[10px] text-base md:text-lg leading-none font-normal text-white mt-3 md:mt-4">
                        <li><router-link to="/">首頁</router-link></li>
                        <li>/</li>
                        <li class="text-primary">房型介紹</li>
                    </ul>
                </div>
            </div>

            <div class="s-py-100-50 overflow-hidden" data-aos="fade-up">
                <div class="relative">
                    <!-- 重點註解：輪播圖間隔縮小，視覺更緊湊 -->
                    <Swiper :modules="[Autoplay, Navigation]" :slides-per-view="3" :space-between="0"
                        :loop="sliderImagesToShow.length >= 4" :navigation="true"
                        :autoplay="{ delay: 3000, disableOnInteraction: false }" :breakpoints="{
                            320: { slidesPerView: 1, spaceBetween: 0 },
                            576: { slidesPerView: 2, spaceBetween: 0 },
                            1024: { slidesPerView: 3, spaceBetween: 0 },
                            1200: { slidesPerView: 4, spaceBetween: 0 }
                        }" class="owl-carousel mx-auto block">
                        <!-- 重點註解：輪播圖只顯示前六張圖片 -->
                        <SwiperSlide v-for="(item, index) in sliderImagesToShow.slice(0, 6)" :key="index"
                            @click="goToDetails(item.id)" style="cursor:pointer">
                            <img class="w-full h-60 object-cover rounded shadow duration-300 group-hover:scale-110"
                                :src="item.image" alt="房型圖片" style="width:100%;height:240px;" />
                            <div
                                class="absolute z-20 w-full h-full flex top-0 left-0 flex-col items-center justify-center px-5">
                                <h3
                                    class="text-white leading-tight font-semibold transform -translate-y-5 opacity-0 duration-300 group-hover:-translate-y-0 group-hover:opacity-100">
                                    {{ item.name }}</h3>
                            </div>
                        </SwiperSlide>
                    </Swiper>
                </div>
            </div>

            <div class="s-py-50-100" data-aos="fade-up">
                <div class="container-fluid">
                    <div class="flex items-center justify-between gap-4 flex-wrap">
                        <!-- 排序選項 -->
                        <div
                            class="flex items-start sm:items-center gap-[8px] flex-wrap sm:flex-nowrap sm:max-w-[347px] w-full flex-col sm:flex-row">
                            <h4 class="font-medium leading-none text-xl flex-none">排序</h4>
                            <div class="nice-select bg-white dark:bg-dark outline-select small-select"
                                :class="isOpen2 ? 'open' : ''" @click="toggleDropdown2">
                                <span class="current">{{ selectedOption2 }}</span>
                                <ul class="list">
                                    <li v-for="(item, index) in options2" :key="index" class="option"
                                        @click="handleSelect2(item, $event)">
                                        {{ item }}
                                    </li>
                                </ul>
                            </div>
                        </div>
                        <div class="hidden 2xl:block">
                            <svg width="24" height="41" viewBox="0 0 24 41" fill="none"
                                xmlns="http://www.w3.org/2000/svg">
                                <line x1="0.566987" y1="40.6689" x2="23.567" y2="0.831777" stroke="#BB976D" />
                            </svg>
                        </div>
                        <!-- 價格範圍選項 -->
                        <div
                            class="sm:max-w-[411px] w-full flex items-start sm:items-center gap-[8px] flex-wrap sm:flex-nowrap flex-col sm:flex-row">
                            <h4 class="font-medium leading-none text-xl flex-none">搜尋價格</h4>
                            <div class="nice-select bg-white dark:bg-dark outline-select small-select"
                                :class="isPriceOpen ? 'open' : ''" @click="togglePriceDropdown">
                                <span class="current">{{ selectedPriceLabel }}</span>
                                <ul class="list">
                                    <li v-for="(range, idx) in priceRanges" :key="idx" class="option"
                                        @click="handlePriceSelect(range, $event)">
                                        {{ range.label }}
                                    </li>
                                </ul>
                            </div>
                        </div>
                        <!-- 蒐藏房型按鈕 -->
                        <!-- 重點註解：蒐藏房型按鈕 icon 依狀態切換紅心 -->
                        <button
                            class="px-6 py-2 bg-primary text-white rounded hover:bg-primary-dark transition text-base flex items-center whitespace-nowrap"
                            @click="onFavoriteClick" style="min-width: 140px;">
                            <i class="mdi text-xl mr-2"
                                :class="showFavoriteOnly ? 'mdi-cards-heart text-red-500' : 'mdi-cards-heart-outline'"></i>
                            已收藏房型
                        </button>
                    </div>
                    <div class="max-w-[1720px] mx-auto mt-8 md:mt-12" data-aos="fade-up" data-aos-delay="200">
                        <!-- 圖片加大，防止圖標超出，一排三個，gap縮窄一半 -->
                        <!-- 重點註解：只顯示 isAvailable 為 true 的房型，並將會員收藏 id 傳給子元件 -->
                        <LayoutOne
                            :classList="'grid sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-3 xl:grid-cols-3 gap-2.5 lg:gap-[15px]'"
                            :productList="showFavoriteOnly
                                ? filteredList.filter(item => favoriteIds.includes(item.id) && item.isAvailable)
                                : filteredList.filter(item => item.isAvailable)" :favoriteIds="favoriteIds"
                            :memberId="memberId" @updateFavorite="updateFavorite" @view="goToDetails"
                            @imageClick="goToDetails" />
                    </div>
                </div>
            </div>
        </AuthPrompt>

        <FooterThree />
        <ScrollToTop />
    </div>
</template>

<script setup>
// 重點註解：會員使用當前登入使用者，進入頁面自動取得該會員已收藏房型，愛心狀態即時同步

import MainNavbar from '@/components/navbar/main-navbar.vue';
import LayoutOne from '@/components/roomType/layout-one.vue';
import FooterThree from '@/components/footer/footer-three.vue';
import ScrollToTop from '@/components/scroll-to-top.vue';
// 重點註解：引入 AuthPrompt 元件進行登入驗證
import AuthPrompt from '@/components/auth/AuthPrompt.vue';

import bg from '@/assets/img/shortcode/breadcumb.jpg'
import { Swiper, SwiperSlide } from 'swiper/vue';
import { Autoplay, Navigation } from 'swiper/modules';
import 'swiper/swiper-bundle.css';
import Aos from 'aos';

import { onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { getAllRoomTypes, getRoomTypesByPriceRange } from '@/service/roomTypeService'
// 重點註解：引入收藏 service
import { getFavoritesByMember } from '@/service/favoriteService'
// 重點註解：引入認證功能取得當前登入使用者
import { useAuth } from '@/composables/useAuth'
// 重點註解：引入 memberService 取得當前登入使用者 ID
import { memberService } from '@/service/memberService'

const router = useRouter();

// 重點註解：取得認證相關功能
const { currentUser } = useAuth()

const sliderImages = ref([])
const sliderImagesToShow = ref([])

const filteredList = ref([])

// 價格區間下拉選單資料
const priceRanges = [
    { label: '不限', min: 0, max: 100000 },
    { label: '5000 - 15000', min: 5000, max: 15000 },
    { label: '15001 - 30000', min: 15001, max: 30000 },
    { label: '30001 - 50000', min: 30001, max: 50000 },
    { label: '50001 以上', min: 50001, max: 100000 }
]
const selectedPriceLabel = ref(priceRanges[0].label)
const selectedPriceRange = ref(priceRanges[0])
const isPriceOpen = ref(false)

// 重點註解：memberId 將從當前登入使用者取得，而非固定值
const memberId = ref(null)
const favoriteIds = ref([]) // 收藏的房型 id
const showFavoriteOnly = ref(false)

// 重點註解：載入會員收藏清單
async function loadMemberFavorites() {
    if (!memberId.value) return

    try {
        const favRes = await getFavoritesByMember(memberId.value)
        if (favRes.success) {
            favoriteIds.value = favRes.data.map(f => Number(f.id))
        }
    } catch (error) {
        console.error('載入會員收藏清單失敗:', error)
        favoriteIds.value = []
    }
}

// 重點註解：載入當前登入使用者的 memberId
async function loadCurrentMemberId() {
    try {
        const memberData = await memberService.getCurrentMember()
        memberId.value = memberData.memberId
        console.log('取得當前使用者 ID:', memberId.value)

        // 重點註解：取得使用者 ID 後載入收藏清單
        await loadMemberFavorites()
    } catch (error) {
        console.error('取得當前使用者 ID 失敗:', error)
        memberId.value = null
        favoriteIds.value = []
    }
}

// 重點註解：監聽 currentUser 的變化，登入後自動載入使用者 ID 和收藏清單
watch(currentUser, (newUser) => {
    if (newUser) {
        loadCurrentMemberId()
    } else {
        // 重點註解：登出時清空資料
        memberId.value = null
        favoriteIds.value = []
    }
}, { immediate: true })

onMounted(async () => {
    Aos.init()
    // 取得所有房型資料
    const roomTypes = await getAllRoomTypes()
    sliderImages.value = roomTypes.slice(0, 6) // ★重點註解：只取前六張圖片
    filteredList.value = roomTypes

    // 若資料少於 4，重複資料直到長度 >= 4
    let arr = [...sliderImages.value]
    while (arr.length < 4 && arr.length > 0) {
        arr = arr.concat(arr)
    }
    sliderImagesToShow.value = arr
})

const goToDetails = (id) => {
    router.push(`/RoomType-details/${id}`)
}

const isOpen2 = ref(false)
const options2 = [
    "基本排序",
    "依人數排序",
    "依價格排序"
];
const selectedOption2 = ref(options2[0])

const toggleDropdown2 = () => {
    isOpen2.value = !isOpen2.value
}

// 根據排序選項即時排序 filteredList
const handleSelect2 = (option, event) => {
    event.stopPropagation();
    selectedOption2.value = option
    isOpen2.value = false

    let sorted = [...filteredList.value]
    if (option === "依人數排序") {
        sorted.sort((a, b) => a.capacity - b.capacity)
    } else if (option === "基本排序") {
        sorted.sort((a, b) => a.id - b.id)
    } else if (option === "依價格排序") {
        sorted.sort((a, b) => a.price - b.price)
    }
    filteredList.value = sorted
}

const togglePriceDropdown = () => {
    isPriceOpen.value = !isPriceOpen.value
}

// 選擇價格區間後自動搜尋並更新房型列表
const handlePriceSelect = async (range, event) => {
    event.stopPropagation()
    selectedPriceLabel.value = range.label
    selectedPriceRange.value = range
    isPriceOpen.value = false
    const { min, max } = range
    const list = await getRoomTypesByPriceRange(min, max)
    filteredList.value = list
}

// 點擊蒐藏房型按鈕，只顯示有紅心的房型
function onFavoriteClick() {
    showFavoriteOnly.value = !showFavoriteOnly.value
}

// 子元件通知收藏狀態更新
function updateFavorite(ids) {
    favoriteIds.value = ids
}
</script>
