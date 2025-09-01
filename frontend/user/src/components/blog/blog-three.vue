<template>
    <!-- 
        區塊標題：商業活動
        1. 風格與原本一致，字型與顏色可依設計稿調整
        2. 建議放在部落格列表上方
    -->
    <div class="text-center my-12">

    </div>
    <div class="w-full flex justify-center">
        <div class="grid md:grid-cols-2 xl:grid-cols-3 gap-7 md:gap-9 w-full max-w-[1600px]" data-aos="fade-up" data-aos-delay="100">
            <div v-for="(item, index) in blogData" :key="index" class="group flex flex-col items-center">
                <router-link to="#" class="overflow-hidden block w-full flex justify-center">
                    <!-- 圖片寬高加大，object-cover 保持比例 -->
                    <img
                        class="duration-400 transform scale-100 group-hover:scale-110 w-[420px] h-[340px] object-cover rounded-lg shadow"
                        :src="item.image"
                        alt="blog-card"
                    />
                </router-link>
                <div class="bg-snow dark:bg-dark-secondary p-5 md:p-6 mt-[-40px] w-[90%] relative z-10 shadow-lg rounded-lg">
                    <ul class="flex items-center gap-[10px] flex-wrap">
                        <li class="text-[15px] leading-none dark:text-white">{{item.date}}</li>
                        <li>
                            <router-link to="/blog-tag" class="inline-block text-title font-medium text-[15px] leading-none py-2 px-[10px] rounded-md bg-primary-midum">
                                {{item.tag}}
                            </router-link>
                        </li>
                    </ul>
                    <h5 class="mt-3 font-medium text-xl dark:text-white leading-[1.5]">
                        <router-link to="/blog-details-v1" class="text-underline">{{item.title}}</router-link>
                    </h5>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
    // 引入部落格資料
    import { blogData } from '@/data/index-three';
</script>
