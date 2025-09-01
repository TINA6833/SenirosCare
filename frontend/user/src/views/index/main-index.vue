<template>
    <div>
        <NavbarThree />
        <div style="height: 140px;"></div>

        <Swiper :modules="[Autoplay, Pagination]" :loop="true" :pagination="{ clickable: true }"
            :autoplay="{ delay: 3000 }" :speed="2000"
            class="my-swiper home-v3-banner-slider owl-carousel pb-12 lg:pb-0 block">
            <SwiperSlide class="lg:flex items-center gap-8 xl:gap-16 2xl:gap-24">
                <div
                    class="lg:w-[55%] relative before:absolute before:w-1/2 before:h-full before:top-0 before:right-0 slider-gradient-overlay crsl-slider-thumb max-h-[500px] lg:max-h-full before:hidden lg:before:block">
                    <img class="w-full" :src="bg1" alt="banner-slider" />
                </div>
                <div class="lg:w-[45%] lg:pr-5">
                    <div class="lg:max-w-[617px] w-full mt-8 xl:mt-0 crsl-slider-content px-5 lg:px-0">
                        <h4 class="leading-none font-medium dark:text-white">提供多元長照與樂齡服務</h4>
                        <h2
                            class="leading-none text-2xl sm:text-3xl xl:text-4xl 1xl:text-5xl font-bold mt-6 dark:text-white">
                            給長輩最安心的照護環境
                        </h2>
                        <p class="mt-4 md:mt-5 dark:text-white-light">
                            我們致力於打造溫馨、安全的生活空間，提供專業照服員、輔具租賃、房型選擇、交通接送與多元活動，讓每一位長輩都能自在生活、家屬更放心。
                        </p>
                    </div>
                </div>
            </SwiperSlide>
            <SwiperSlide class="lg:flex items-center gap-8 xl:gap-16 2xl:gap-24">
                <div
                    class="lg:w-[55%] relative before:absolute before:w-1/2 before:h-full before:top-0 before:right-0 slider-gradient-overlay crsl-slider-thumb max-h-[500px] lg:max-h-full before:hidden lg:before:block">
                    <img class="w-full" :src="bg2" alt="banner-slider" />
                </div>
                <div class="lg:w-[45%] lg:pr-5">
                    <div class="lg:max-w-[617px] w-full mt-8 xl:mt-0 crsl-slider-content px-5 lg:px-0">
                        <!-- 小標題 -->
                        <h4 class="leading-none font-medium dark:text-white">豐富多元的樂齡活動</h4>
                        <!-- 主標題，字體大小可依需求調整 -->
                        <h2
                            class="leading-none text-2xl sm:text-3xl xl:text-4xl 1xl:text-5xl font-bold mt-6 dark:text-white">
                            讓長輩享受有品質的生活
                        </h2>
                        <!-- 說明文字 -->
                        <p class="mt-4 md:mt-5 dark:text-white-light">
                            我們規劃各式各樣的休閒、健康與社交活動，鼓勵長輩參與，促進身心健康，讓每一天都充滿歡樂與成就感。
                        </p>
                    </div>
                </div>
            </SwiperSlide>
            <SwiperSlide class="lg:flex items-center gap-8 xl:gap-16 2xl:gap-24">
                <div
                    class="lg:w-[55%] relative before:absolute before:w-1/2 before:h-full before:top-0 before:right-0 slider-gradient-overlay crsl-slider-thumb max-h-[500px] lg:max-h-full before:hidden lg:before:block">
                    <img class="w-full" :src="bg3" alt="banner-slider" />
                </div>
                <div class="lg:w-[45%] lg:pr-5">
                    <div class="lg:max-w-[617px] w-full mt-8 xl:mt-0 crsl-slider-content px-5 lg:px-0">
                        <!-- 小標題 -->
                        <h4 class="leading-none font-medium dark:text-white">打造溫馨舒適的生活空間</h4>
                        <!-- 主標題 -->
                        <h2
                            class="leading-none text-2xl sm:text-3xl xl:text-4xl 1xl:text-5xl font-bold mt-6 dark:text-white">
                            專業團隊守護您的每一天
                        </h2>
                        <!-- 說明文字 -->
                        <p class="mt-4 md:mt-5 dark:text-white-light">
                            提供24小時照護、健康管理與多元活動，讓長輩安心、家屬放心。立即了解我們的專業服務，開啟美好樂齡生活！
                        </p>
                    </div>
                </div>
            </SwiperSlide>
        </Swiper>

        <!-- 服務分類區塊，兩個在上面、三個在下面，重點註解已標註 -->
        <div class="s-py-100 bg-[#f5f5f5] dark:bg-dark-secondary relative z-10 overflow-hidden">

            <div class="container-fluid max-w-[1720px] mx-auto">
                <!-- 上面兩個 -->
                <div class="max-w-[900px] mx-auto grid grid-cols-1 md:grid-cols-2 gap-[30px] xl:gap-12 mb-12">
                    <div v-for="item in servicesData.slice(0, 2)" :key="item.id" class="text-center sm:text-left group"
                        data-aos="fade-up">
                        <!-- 卡片內容 -->
                        <div
                            class="w-14 sm:w-[70px] h-14 sm:h-[70px] p-[10px] mx-auto sm:mx-0 flex items-center justify-center border border-primary rounded-[10px]">
                            <img :src="item.image" alt="" />
                        </div>
                        <!-- 讓輔具買賣點擊跳轉到 /products，其餘維持原本 -->
                        <h4 class="mt-6 font-semibold leading-none text-xl lg:text-2xl">
                            <router-link v-if="item.title === '輔具買賣'" to="/products"
                                class="text-underline">{{ item.title }}</router-link>
                            <router-link v-else to="/roomType" class="text-underline">{{ item.title }}</router-link>
                        </h4>
                        <span class="block leading-none mt-[10px]">{{ item.product }}</span>
                        <p class="mt-[15px]">{{ item.desc }}</p>
                    </div>
                </div>
                <!-- 下面三個 -->
                <div class="max-w-[1350px] mx-auto grid grid-cols-1 md:grid-cols-3 gap-[30px] xl:gap-12">
                    <div v-for="item in servicesData.slice(2, 5)" :key="item.id" class="text-center sm:text-left group"
                        data-aos="fade-up">
                        <!-- 卡片內容 -->
                        <div
                            class="w-14 sm:w-[70px] h-14 sm:h-[70px] p-[10px] mx-auto sm:mx-0 flex items-center justify-center border border-primary rounded-[10px]">
                            <img :src="item.image" alt="" />
                        </div>
                        <h4 class="mt-6 font-semibold leading-none text-xl lg:text-2xl">
                            <router-link to="/roomType" class="text-underline">{{ item.title }}</router-link>
                        </h4>
                        <span class="block leading-none mt-[10px]">{{ item.product }}</span>
                        <p class="mt-[15px]">{{ item.desc }}</p>
                    </div>
                </div>
            </div>
        </div>

        <!-- 最新商品區塊，串接後端資料，重點註解已標註 -->
        <div class="s-py-100-50">
            <div class="container-fluid max-w-[1720px] mx-auto">
                <!-- 熱銷商品區塊標題 -->
                <div class="flex flex-col items-center mb-[40px] md:mb-12" data-aos="fade-up">
                    <h2 class="font-cursive text-6xl text-[#b89c7d] mb-2 font-bold" style="letter-spacing:0.15em;">
                        熱銷商品
                    </h2>
                    <div class="tracking-[0.5em] text-lg font-medium text-[#1a2936]">PRODUCTS</div>
                </div>
                <!-- 商品卡片區塊，取前4個已上架商品 -->
                <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-4 gap-5 lg:gap-8"
                    data-aos="fade-up" data-aos-delay="100">
                    <div v-for="item in homeDevices" :key="item.id" class="relative overflow-hidden group">
                        <!-- 點擊圖片可進入商品詳情 -->
                        <router-link :to="`/product-details/${item.id}`">
                            <img class="w-full transform duration-300 group-hover:scale-110"
                                :src="getImageUrl(item.image)" :alt="item.name" />
                        </router-link>
                        <!-- 商品資訊區塊 -->
                        <div
                            class="flex flex-col items-start gap-3 md:gap-4 absolute z-20 w-11/12 bottom-3 xl:bottom-5 left-1/2 transform -translate-x-1/2 p-4 xl:p-5 bg-white dark:bg-title bg-opacity-[85%] dark:bg-opacity-[85%] group-hover:-translate-y-1/2 duration-500 group-hover:opacity-0 group-hover:invisible">
                            <h4 class="font-medium leading-none dark:text-white text-lg">NT${{
                                Number(item.unitPrice).toLocaleString('zh-TW') }}</h4>
                            <div>
                                <h5 class="font-normal dark:text-white text-paragraph leading-[1.5]">
                                    <router-link :to="`/product-details/${item.id}`">{{ item.name }}</router-link>
                                </h5>
                                <!-- 評分與銷量可依需求串接 -->
                                <ul class="flex items-center gap-2 mt-1">
                                    <li><i class="fa-solid fa-star text-[#EE9818] text-[14px]"></i></li>
                                    <li><i class="fa-solid fa-star text-[#EE9818] text-[14px]"></i></li>
                                    <li><i class="fa-solid fa-star text-[#EE9818] text-[14px]"></i></li>
                                    <li><i class="fa-solid fa-star text-[#EE9818] text-[14px]"></i></li>
                                    <li><i class="fa-solid fa-star text-slate-300 text-[14px]"></i></li>
                                    <li class="dark:text-gray-100">( 1,230 )</li>
                                </ul>
                            </div>
                        </div>
                        <!-- 按鈕區塊（可依需求串接購物車/收藏功能） -->
                        <div
                            class="absolute z-10 flex gap-2 justify-center bottom-5 md:bottom-7 w-full transform translate-y-5 opacity-0 duration-500 invisible group-hover:translate-y-0 group-hover:opacity-100 group-hover:visible">
                            <button
                                class="w-9 lg:w-12 h-9 p-2 lg:h-12 bg-white dark:bg-title bg-opacity-80 flex items-center justify-center relative tooltip-icon rounded-full">
                                <i class="mdi mdi-cards-heart-outline dark:text-white text-[24px]"></i>
                                <span
                                    class="p-2 bg-white dark:bg-title text-xs text-title dark:text-white absolute -top-[60px] left-1/2 transform -translate-x-1/2 whitespace-nowrap rounded-[4px] opacity-0 invisible duration-300">Add
                                    to wishlist
                                    <span
                                        class="w-3 h-3 bg-white dark:bg-title absolute -bottom-[6px] left-1/2 transform -translate-x-1/2 rotate-45"></span>
                                </span>
                            </button>
                            <router-link to="/cart"
                                class="w-9 lg:w-12 h-9 p-2 lg:h-12 bg-white dark:bg-title bg-opacity-80 flex items-center justify-center relative tooltip-icon rounded-full">
                                <i class="mdi mdi-shopping-outline dark:text-white text-[24px]"></i>
                                <span
                                    class="p-2 bg-white dark:bg-title text-xs text-title dark:text-white absolute -top-[60px] left-1/2 transform -translate-x-1/2 whitespace-nowrap rounded-[4px] opacity-0 invisible duration-300">Add
                                    to Cart
                                    <span
                                        class="w-3 h-3 bg-white dark:bg-title absolute -bottom-[6px] left-1/2 transform -translate-x-1/2 rotate-45"></span>
                                </span>
                            </router-link>
                            <button
                                class="w-9 lg:w-12 h-9 p-2 lg:h-12 bg-white dark:bg-title bg-opacity-80 flex items-center justify-center relative quick-view tooltip-icon rounded-full">
                                <i class="mdi mdi-eye-outline dark:text-white text-[24px]"></i>
                                <span
                                    class="p-2 bg-white dark:bg-title text-xs text-title dark:text-white absolute -top-[60px] left-1/2 transform -translate-x-1/2 whitespace-nowrap rounded-[4px] opacity-0 invisible duration-300">Quick
                                    Preview
                                    <span
                                        class="w-3 h-3 bg-white dark:bg-title absolute -bottom-[6px] left-1/2 transform -translate-x-1/2 rotate-45"></span>
                                </span>
                            </button>
                        </div>
                    </div>
                </div>
                <!-- See all Product 按鈕，跳轉到 /products 頁面 -->
                <div class="text-center mt-7 md:mt-12" data-aos="fade-up" data-aos-delay="200">
                    <router-link to="/products" class="btn btn-outline" data-text="See all Product">
                        <span>查看全部商品</span>
                    </router-link>
                </div>
            </div>
        </div>

        <!-- 房型區塊，圖片左右對齊且放大，並讓兩張圖片中間有間距 -->
        <div class="s-py-50" data-aos="fade-up">
            <div class="container-fluid max-w-[1720px] mx-auto">
                <!-- 房型區塊標題 -->
                <div class="flex flex-col items-center mb-[40px] md:mb-12">
                    <h2 class="font-cursive text-6xl text-[#b89c7d] mb-2 font-bold text-center"
                        style="letter-spacing:0.15em;">
                        房型
                    </h2>
                    <div class="tracking-[0.5em] text-lg font-medium text-[#1a2936]">ROOMS</div>
                </div>
                <!-- 兩張圖片左右對齊且放大，並加上間距 -->
                <div class="flex justify-center items-center w-full gap-10" style="min-height: 500px;">
                    <!-- 左圖 -->
                    <img class="rounded-xl shadow-lg object-cover" :src="product2" alt="單人房"
                        style="width: 700px; height: 500px;" />
                    <!-- 右圖 -->
                    <img class="rounded-xl shadow-lg object-cover" :src="product1" alt="雙人房"
                        style="width: 700px; height: 500px;" />
                </div>
            </div>
        </div>

        <div class="s-py-50-100">
            <div class="container-fluid max-w-[1720px] mx-auto">
                <!-- 只保留活動標題，移除 Blog/LATEST BLOG -->
                <div class="flex flex-col items-center mb-[40px] md:mb-12" data-aos="fade-up">
                    <h2 class="font-cursive text-6xl text-[#b89c7d] mb-2 font-bold">活動</h2>
                    <div class="tracking-[0.5em] text-lg font-medium text-[#1a2936]">EVENTS</div>
                </div>
                <BlogThree />
            </div>
        </div>

        <div class="bg-title dark:bg-dark-secondary py-9 md:py-12">
            <div class="container-fluid max-w-[1720px] mx-auto">
                <div class="2xl:flex items-center gap-10">
                    <div class="max-w-[300px] w-full hidden 2xl:block">
                        <h2 class="text-white leading-none text-xl md:text-4xl">特別感謝資展國際</h2>
                        <p class="text-white mt-[10px]"> </p>
                    </div>
                    <div class="w-[2px] h-12 bg-primary relative hidden 2xl:block"></div>
                    <div class="2xl:max-w-[1340px] w-full relative">
                        <PartnerTwo />
                        <div
                            class="md:absolute md:top-2/4 md:right-0 transform md:-translate-y-2/4 mt-6 md:mt-0 hidden">
                            <div class="flex gap-4">
                                <button
                                    class="icon ptnrSlider01_prev w-9 h-9 border border-white flex items-center justify-center text-white">
                                    <span class="lnr lnr-arrow-left"></span>
                                </button>
                                <button
                                    class="icon ptnrSlider01_next w-9 h-9 border border-white flex items-center justify-center text-white">
                                    <span class="lnr lnr-arrow-right"></span>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <FooterThree />

        <ScrollToTop />

    </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { getAllDevices } from '@/service/deviceService'

import bg1 from '@/assets/img/shortcode/carousel/carousel-23.png'
import bg2 from '@/assets/img/shortcode/carousel/carousel-24.png'
import bg3 from '@/assets/img/shortcode/carousel/carousel-25.png'
import product1 from '@/assets/img/home-v3/room2.png'
import product2 from '@/assets/img/home-v3/room5.png'

// [修正] 將元件路徑還原至 components 資料夾的原始結構
import NavbarThree from '@/components/navbar/main-navbar.vue';
import BlogThree from '@/components/blog/blog-three.vue';
import PartnerTwo from '@/components/partner/partner-two.vue';
import FooterThree from '@/components/footer/footer-three.vue';
import ScrollToTop from '@/components/scroll-to-top.vue';

import { Swiper, SwiperSlide } from 'swiper/vue';
import { Autoplay, Pagination } from 'swiper/modules';
import 'swiper/swiper-bundle.css';

import Aos from 'aos';

import { servicesData } from '@/data/index-three';

// 首頁商品狀態
const homeDevices = ref([])

// 取得前4個已上架商品
async function fetchHomeDevices() {
    const all = await getAllDevices()
    // 只顯示 isOnline 為 true 的商品，取前4個
    homeDevices.value = all.filter(d => d.isOnline).slice(0, 4)
}

// 圖片網址組合（與其他元件一致）
function getImageUrl(image) {
    if (!image) return ''
    if (image.startsWith('http')) return image
    return `http://localhost:8080/images/${image}`
}

onMounted(() => {
    Aos.init()
    fetchHomeDevices()
})

</script>
