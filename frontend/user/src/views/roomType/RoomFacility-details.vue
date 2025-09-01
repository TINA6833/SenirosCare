<template>
    <div>
        <NavbarOne/>

        <div class="flex items-center gap-4 flex-wrap bg-overlay p-14 sm:p-16 before:bg-title before:bg-opacity-70" :style="{backgroundImage:'url(' + bg + ' )'}">
            <div class="text-center w-full">
                <h2 class="text-white text-8 md:text-[40px] font-normal leading-none text-center">Portfolio</h2>
                <ul class="flex items-center justify-center gap-[10px] text-base md:text-lg leading-none font-normal text-white mt-3 md:mt-4">
                    <li><router-link to="/"></router-link></li>
                    <li>/</li>
                    <li>Portfolio</li>
                    <li>/</li>
                    <li class="text-primary">{{data && data.name ? data.name : 'Classic Interior Set'}}</li>
                </ul>
            </div>
        </div>

        <div class="portfolio-single s-py-100">
            <div class="container-fluid">
                <div class="max-w-[1200px] mx-auto w-full">
                    <div class="relative">
                        <Swiper
                            :modules="[Autoplay,Navigation]" 
                            :slides-per-view="3" 
                            :space-between="20" 
                            :loop="true"  
                            :navigation="true" 
                            :autoplay="{ delay: 3000}" 
                            :breakpoints="{
                                320: { slidesPerView: 1, spaceBetween: 0 },
                                576: { slidesPerView: 1, spaceBetween: 0 },
                                1024: { slidesPerView: 1, spaceBetween: 0 },
                            }" class="portfolio-v3-slider owl-carousel block">
                            <SwiperSlide class="relative before:absolute before:w-full before:h-full before:top-0 before:left-0 before:bg-title before:bg-opacity-20">
                                <img class="w-full" :src="image1" alt="Portfolio"/>
                            </SwiperSlide>
                            <SwiperSlide class="relative before:absolute before:w-full before:h-full before:top-0 before:left-0 before:bg-title before:bg-opacity-20">
                                <img class="w-full" :src="image2" alt="Portfolio"/>
                            </SwiperSlide>
                            <SwiperSlide class="relative before:absolute before:w-full before:h-full before:top-0 before:left-0 before:bg-title before:bg-opacity-20">
                                <img class="w-full" :src="image3" alt="Portfolio"/>
                            </SwiperSlide>
                        </Swiper>
                    </div>
                </div>
                
                <div class="max-w-[680px] mx-auto w-full block mt-[40px]">
                    <div class="div">
                        <div>
                            <span class="inline-block text-title font-medium text-[15px] leading-none p-[10px] rounded-md bg-primary-midum">{{data && data.tag ? data.tag : 'Classic Interior'}}</span>
                            <h2 class="font-semibold leading-none mt-4 md:mt-6 text-2xl md:text-3xl">
                                {{data && data.name ? data.name : 'Classic Interior Set'}}
                            </h2>
                            <p class="mt-3 text-base sm:text-lg">
                                All the Lorem Ipsum generators on the Internet tend to repeat predefined on the Internet. Lorem ipsum dolor sit amet, consectetur vulputate posuere habitant vel tempor varius.
                            </p>
                        </div>

                        <div class="bg-[#FAFAFA] dark:bg-dark-secondary p-8 mt-[30px] block">
                            <table class="meta-table block">
                                <tbody class="w-full block">
                                    <tr class="border-b border-black/10 dark:border-bdr-clr-drk py-4">
                                        <th class="text-[18px] sm:text-[20px] font-medium">Client</th>
                                        <td class='text-base sm:text-lg'>Wordpress</td>
                                    </tr>
                                    <tr class="border-b border-black/10 dark:border-bdr-clr-drk py-4">
                                        <th>Designer</th>
                                        <td>John Smith Doe</td>
                                    </tr>
                                    <tr class="border-b border-black/10 dark:border-bdr-clr-drk py-4">
                                        <th>Material</th>
                                        <td>Wood, Steel, Paper, Fiber</td>
                                    </tr>
                                    <tr class="py-4">
                                        <th>Website</th>
                                        <td>demosite.com</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div> 

                        <div class="mt-5 sm:mt-7 lg:mt-10">
                            <div class="flex items-center justify-start gap-6">
                                <h6 class="font-normal">Share : </h6>
                                <div class="flex items-center gap-6">
                                    <router-link to="#" class="text-title duration-300 dark:text-white hover:text-primary dark:hover:text-primary">
                                        <i class="fa-brands fa-facebook-f"></i>
                                    </router-link>
                                    <router-link to="#" class="text-title duration-300 dark:text-white hover:text-primary dark:hover:text-primary">
                                        <i class="fa-brands fa-twitter"></i>
                                    </router-link>
                                    <router-link to="#" class="text-title duration-300 dark:text-white hover:text-primary dark:hover:text-primary">
                                        <i class="fa-brands fa-instagram"></i>
                                    </router-link>
                                </div>
                            </div>
                        </div>   
                    </div>
                </div>
            </div>
        </div>

        <div class="s-py-100 bg-primary-light dark:bg-dark-secondary">
            <RelatedProject/>
        </div>

        <FooterOne/>
        <ScrollToTop/>

    </div>
</template>

<script setup>
    import { useRoute } from 'vue-router';

    import NavbarOne from '@/components/navbar/navbar-one.vue';
    import RelatedProject from '@/components/related-project.vue';
    import FooterOne from '@/components/footer/footer-one.vue';
    import ScrollToTop from '@/components/scroll-to-top.vue';

    import bg from '@/assets/img/shortcode/breadcumb.jpg'
    import image1 from '@/assets/img/gallery/portfolio-03/portfolio-02.jpg'
    import image2 from '@/assets/img/gallery/portfolio-03/portfolio-03.jpg'
    import image3 from '@/assets/img/gallery/portfolio-03/portfolio-04.jpg'

    import { Swiper, SwiperSlide } from 'swiper/vue';
    import { Autoplay,Navigation } from 'swiper/modules';
    import 'swiper/swiper-bundle.css';

    import { portfolioTwo } from '@/data/portfolio-data';

    const route = useRoute()

    const data = portfolioTwo.find((item)=>item.id === parseInt(route.params.id))

</script>
