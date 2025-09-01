<template>
    <Swiper
        :modules="[Autoplay,EffectCoverflow]"
        :slides-per-view="1" 
        :space-between="0" 
        :loop="true" 
        :autoplay="{ delay: 3000}"
        :breakpoints="{
            320: { slidesPerView: 1, spaceBetween: 0 },
            576: { slidesPerView: 2, spaceBetween: 0 },
            1024: { slidesPerView: 3, spaceBetween: 0 },
        }"
        :effect="'coverflow'"
        :coverflowEffect="{
            rotate: 20,
            stretch: 1,
            depth: 90,
            modifier: 0.5,
            slideShadows: true,
            }"
     class="home-v5-blog-slider owl-carousel   sm:mb-8 block">
        <SwiperSlide v-for="(item, index) in blogData" :key="index" class="relative group mx-[15px] sm:mx-0">
            <router-link to="/blog-details-v1" class="overflow-hidden block">
                <img class="duration-300 transform scale-100 group-hover:scale-110 w-full" :src="item.image" alt="blog-card"/>
            </router-link>
            <div class="bg-white bg-opacity-90 dark:bg-title dark:bg-opacity-90 p-5 md:p-6 absolute z-10 w-11/12 max-w-md transform left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2">
                <ul class="flex items-center gap-[10px] flex-wrap">
                    <li class="text-[15px] leading-none dark:text-white">{{item.date}}</li>
                    <li><router-link to="/blog-tag" class="inline-block text-title font-medium text-[15px] leading-none py-[10px] px-5 rounded-md bg-primary-midum">{{item.tag}}</router-link></li>
                </ul>
                <h5 class="mt-3 font-medium dark:text-white leading-[1.5]"><router-link to="/blog-details-v1" class="text-underline">{{item.title}}</router-link></h5>
            </div>
        </SwiperSlide>
    </Swiper>
</template>

<script setup>

    import { blogData } from '@/data/index-five';

    import { Swiper, SwiperSlide } from 'swiper/vue';
    import { Autoplay,EffectCoverflow } from 'swiper/modules';
    import 'swiper/swiper-bundle.css';

</script>
