<template>
    <div style="background-color: #1a2530;" class="py-12">
        <div class="max-w-[1720px] mx-auto flex flex-col md:flex-row items-center md:items-start">
            <!-- 左側標題、分隔線、ispan logo -->
            <div class="md:w-1/4 w-full text-white px-6 mb-8 md:mb-0 flex flex-row items-center">
                <!-- 標題 -->
                <h2 class="text-4xl font-bold mb-2 whitespace-nowrap">特別感謝資展國際</h2>
                <!-- 垂直分隔線 -->
                <div class="h-[60px] w-[2px] bg-[#bfa16b] mx-6"></div>
                <!-- ispan logo -->
                <img
                    v-if="ispanLogo"
                    :src="ispanLogo"
                    alt="ispan"
                    class="max-h-[200px] max-w-[180px] object-contain"
                />
            </div>
            <!-- 右側品牌logo（排除 ispan） -->
            <div class="md:w-3/4 w-full flex flex-wrap items-center justify-start gap-10 px-6">
                <div
                    v-for="(item, index) in otherPartnerData"
                    :key="index"
                    class="flex items-center h-[80px]"
                >
                    <img
                        :src="item.image2"
                        alt="brand"
                        class="max-h-[60px] max-w-[180px] object-contain"
                    />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
// 引入品牌資料
import { partnerData } from '@/data/data';

// 取得 ispan logo 與其他品牌資料
const ispanLogo = partnerData.find(item => item.image2.includes('ispan'))?.image2; // 只取 ispan
const otherPartnerData = partnerData.filter(item => !item.image2.includes('ispan')); // 排除 ispan
</script>
