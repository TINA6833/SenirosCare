<template>
    <Swiper
        :modules="[Autoplay]" 
        :slides-per-view="1" 
        :space-between="25" 
        :loop="true"  
        :autoplay="{ delay: 3000}" 
        :breakpoints="{
            320: { slidesPerView: 1, spaceBetween: 10 },
            576: { slidesPerView: 2, spaceBetween: 15 },
            1024: { slidesPerView: 3, spaceBetween: 25 },
        }" 
        class="owl-carousel home-v2-blog block">

        <SwiperSlide v-for="(item, index) in blogTwoData" :key="index" class="relative group">
            <router-link to="/blog-details-v1" class="overflow-hidden block">
                <img class="duration-300 transform scale-100 group-hover:scale-110 w-full" :src="item.image" alt="blog-card"/>
            </router-link>
            <div class="sm:bg-white sm:bg-opacity-90 sm:dark:bg-title sm:dark:bg-opacity-90 mt-4 sm:p-5 md:p-6 sm:absolute z-10 bottom-0 left-0 sm:w-11/12 max-w-md px-5 sm:px-0 ">
                <ul class="flex items-center gap-[10px] flex-wrap">
                    <li class="text-[15px] leading-none dark:text-white">{{item.date}}</li>
                    <li><router-link to="/blog-tag" class="inline-block text-title font-medium text-[15px] leading-none py-[10px] px-5 rounded-md bg-primary-midum">{{item.tag}}</router-link></li>
                </ul>
                <h5 class="mt-3 font-medium dark:text-white leading-[1.5] text-xl"><router-link to="/blog-details-v1" class="text-underline">{{item.title}}</router-link></h5>
            </div>
        </SwiperSlide>
    </Swiper>
</template>

<script setup>
    import { blogTwoData } from '@/data/data';

    import { Swiper, SwiperSlide } from 'swiper/vue';
    import { Autoplay,  } from 'swiper/modules';
    import 'swiper/swiper-bundle.css';
</script>
