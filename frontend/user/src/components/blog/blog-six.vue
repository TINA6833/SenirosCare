<template>
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-[30px]" data-aos="fade-up" data-aos-delay="100">
        <div v-for="(item, index) in blogData2" :key="index" class="group">
            <router-link to="/blog-details-v1" class="p-[10px] sm:p-[15px] bg-[#F3F3F3] transition-all duration-300 group-hover:bg-opacity-0 overflow-hidden block">
                <img class="duration-200 transform scale-100 group-hover:scale-110 w-full" :src="item.image" alt="blog-card">
            </router-link>
            <div class="pt-5 md:pt-6 md:px-4 text-center">
                <ul class="text-[15px] font-medium flex items-center justify-center gap-2 md:gap-4">
                    <li class="dark:text-white leading-none">{{item.date}}</li>
                    <li class="w-1 h-1 rounded-full bg-primary relative"></li>
                    <li class="leading-none"><router-link class="text-primary leading-none" to="/blog-tag">{{item.tag}}</router-link></li>
                </ul>
                <h5 class="mt-3 text-xl font-medium dark:text-white leading-[1.5]"><router-link to="/blog-details-v1" class="text-underline">{{item.title}} </router-link></h5>
                <div class="mt-5 md:mt-6">
                    <router-link class="font-medium text-base text-title md:text-[17px] leading-none py-[15px] px-5 relative z-10 before:absolute before:bottom-0 before:left-0 before:bg-primary-midum before:w-full before:h-2/4 before:-z-10 blog-btn-hover before:transition-all before:duration-300 dark:text-white inline-block" to="/blog-details-v1">Read More</router-link>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>

import { blogData2 } from '@/data/index-five';

</script>
