<template>
    <Swiper 
        :modules="[Autoplay]" 
        :slides-per-view="6" 
        :autoplay="{ delay: 3000}"
        :loop="true"
        :breakpoints="{
            320: { slidesPerView: 1, spaceBetween: 10 },
            576: { slidesPerView: 3, spaceBetween: 15 },
            1024: { slidesPerView: 4, spaceBetween: 20 },
            1140: { slidesPerView: 6, spaceBetween: 20 },
        }"
        class="max-w-[1720px] mx-auto owl-carousel home-v1-partner-slider flex items-center">
        <swiper-slide v-for="(item, index) in partnerData" :key="index" class="flex items-center justify-center w-full" aria-label="partner logo">
            <img :src="item.image" class="block dark:hidden" alt="">
            <img :src="item.image2" class="hidden dark:block" alt="">
        </swiper-slide>
    </Swiper>
</template>

<script setup>

import { partnerData } from '@/data/data';

import { Swiper, SwiperSlide } from 'swiper/vue';
import { Autoplay} from 'swiper/modules';
import 'swiper/swiper-bundle.css';

</script>
