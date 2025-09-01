<template>
    <div class="max-w-1366 mx-auto">
        <div class="flex items-center justify-between gap-5 flex-wrap mb-6 pb-4 md:pb-6 border-b border-bdr-clr dark:border-bdr-clr-drk" data-aos="fade-up">
            <h2 class="font-semibold leading-none text-2xl sm:text-3xl lg:text-4xl">Latest Blog</h2>
        </div>
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 xl:grid-cols-4 gap-5 md:gap-[30px]" data-aos="fade-up" data-aos-delay="100">
            <div v-for="(item, index) in blogData.slice(0,4)" :key="index" class="group">
                <router-link to="/blog-details-v1" class="overflow-hidden block">
                    <img class="duration-300 transform scale-100 group-hover:scale-110 w-full" :src="item.image" alt="blog-card"/>
                </router-link>
                <div class="p-5 relative z-10 before:absolute before:-z-10 before:top-0 before:left-0 before:w-full before:h-full before:bg-primary dark:before:bg-dark-secondary before:bg-opacity-10 before:transition-all before:duration-300 overflow-hidden before:opacity-0 group-hover:before:opacity-100">
                    <ul class="flex items-center gap-[10px] flex-wrap">
                        <li class="text-[15px] leading-none dark:text-white">{{item.date}}</li>
                        <li><router-link to="/blog-tag" class="inline-block text-title font-medium text-[15px] leading-none py-[10px] px-5 rounded-md bg-primary-midum">{{item.tag}}</router-link></li>
                    </ul>
                    <h5 class="mt-3 font-medium text-xl dark:text-white leading-[1.5]"><router-link to="/blog-details-v1" class="text-underline">{{item.title}}</router-link></h5>
                    <p class="text-base md:text-lg mt-3 dark:text-white-light">{{item.desc}}</p>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>

import { blogData } from '@/data/data';

</script>
