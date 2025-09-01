<template>
    <div class="grid sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5 gap-5 md:gap-[30px]" data-aos="fade-up" data-aos-delay="100">
        <div v-for="(item, index) in blogData.slice(0,5)" :key="index" class="group">
            <router-link to="/blog-details-v1" class="overflow-hidden block">
                <img class="duration-300 transform scale-100 group-hover:scale-110 w-full" :src="item.image" alt="blog">
            </router-link>
            <div class="text-center mt-4 px-3">
                <ul class="flex items-center justify-center gap-[10px] flex-wrap">
                    <li class="text-[15px] leading-none dark:text-white">{{item.date}}</li>
                    <li><router-link to="/blog-tag" class="inline-block text-title font-medium text-[15px] leading-none py-2 px-[10px] rounded-md bg-primary-midum">{{item.tag}}</router-link></li>
                </ul>
                <h5 class="text-xl mt-3 font-medium dark:text-white leading-[1.5]"><router-link to="/blog-details-v1" class="text-underline">{{item.title}} </router-link></h5>
            </div>
        </div>
    </div>
</template>

<script setup>

    import { blogData } from '@/data/data';

</script>
