<template>
    <div class="s-py-100-50">
        <div class="container">
            <div class="max-w-1366 mx-auto flex items-start gap-10 justify-between flex-col 2xl:flex-row">
                <div class="max-w-md sm:mx-auto 2xl:mx-0 2xl:max-w-[208px] w-full sm:text-center 2xl:text-left">
                    <router-link class="cursor-pointer block" to="/" aria-label="Furnixar">
                        <img :src="logoDark" alt="" class='dark:hidden w-[120px] sm:w-[200px]'/> 
                        <img :src="logoLight" alt="" class='dark:block hidden w-[120px] sm:w-[200px]'/>
                    </router-link>
                    <p class="mt-[15px]">Furnixar is a modern furniture template for an eCommerce website designed to help you create an impressive online store.</p>
                </div>
                <div class="hidden 2xl:block h-[121px] w-[1px] transform translate-y-12 border border-dashed border-title dark:border-white"></div>
                <div class="2xl:max-w-[1057px] w-full grid grid-cols-2 sm:grid-cols-3 lg:flex items-start justify-between gap-10">
                    <div>
                        <h4 class="leading-none mb-5 md:mb-6 text-xl">Sitemap</h4>
                        <ul class="text-title dark:text-white leading-none flex flex-col gap-4">
                            <li v-for="(item, index) in footerLink1" :key="index" class="duration-100 hover:text-primary inline-block group"><router-link class="text-underline-primary" :to="item.link">{{item.name}}</router-link></li>
                        </ul>
                    </div>
                    <div class="h-[121px] w-[1px] transform translate-y-12 border border-dashed border-title dark:border-white hidden lg:block"></div>
                    <div>
                        <h4 class="leading-none mb-5 md:mb-6 text-xl">Others</h4>
                        <ul class="text-title dark:text-white leading-none flex flex-col gap-4">
                            <li v-for="(item, index) in footerLink2" :key="index" class="duration-100 hover:text-primary inline-block group"><router-link class="text-underline-primary" :to="item.link">{{item.name}}</router-link></li>
                        </ul>
                    </div>
                    <div class="h-[121px] w-[1px] transform translate-y-12 border border-dashed border-title dark:border-white hidden lg:block"></div>
                    <div>
                        <h4 class="leading-none mb-5 md:mb-6 text-xl">Social Media</h4>
                        <ul class="text-title dark:text-white leading-none flex flex-col gap-4">
                            <li class="duration-100 hover:text-primary inline-block group"><router-link class="gap-3 flex items-center" to="#">
                                <i class="fa-brands fa-facebook-f text-[#5D656E] dark:text-white duration-300 group-hover:text-primary"></i>                                     
                                Facebook
                            </router-link></li>
                            <li class="duration-100 hover:text-primary inline-block group"><router-link class="gap-3 flex items-center" to="#">
                                <i class="fa-brands fa-twitter text-[#5D656E] dark:text-white duration-300 group-hover:text-primary"></i>                      
                                Twitter
                            </router-link></li>
                            <li class="duration-100 hover:text-primary inline-block group"><router-link class="gap-3 flex items-center" to="#">
                                <i class="fa-brands fa-instagram text-[#5D656E] dark:text-white duration-300 group-hover:text-primary"></i>                        
                                Instagram
                            </router-link></li>
                            <li class="duration-100 hover:text-primary inline-block group"><router-link class="gap-3 flex items-center" to="#">
                                <i class="fa-brands fa-linkedin-in text-[#5D656E] dark:text-white duration-300 group-hover:text-primary"></i>                     
                                Linkedin
                            </router-link></li>
                        </ul>
                    </div>
                    <div class="h-[121px] w-[1px] transform translate-y-12 border border-dashed border-title dark:border-white hidden lg:block"></div>
                    <div>
                        <h4 class="leading-none mb-5 md:mb-6 text-xl">Shop</h4>
                        <ul class="text-title dark:text-white leading-none flex flex-col gap-4">
                            <li v-for="(item, index) in footerLink3" :key="index" class="duration-100 hover:text-primary inline-block group"><router-link class="text-underline-primary" :to="item.link">{{item.name}}</router-link></li>
                        </ul>
                    </div>
                    <div class="h-[121px] w-[1px] transform translate-y-12 border border-dashed border-title dark:border-white hidden lg:block "></div>
                    <div class="col-span-2 sm:col-span-1">
                        <h4 class="leading-none mb-5 md:mb-6 text-xl">Customer Service</h4>
                        <ul class="text-title dark:text-white leading-none flex flex-col gap-4">
                            <li v-for="(item, index) in footerLink4" :key="index" class="duration-100 hover:text-primary inline-block group"><router-link class="text-underline-primary" :to="item.link">{{item.name}}</router-link></li>
                        </ul>
                    </div>
                </div> 
            </div>
            <p class="max-w-1366 mx-auto mt-12">© {{year}} Furnixar Develop with <i class="fa-solid fa-heart text-red-600"></i> by <a href="https://shreethemes.in/" target="_blank" className='ms-1'>Shreethemes</a></p>
        </div>
    </div>
</template>

<script setup>
    import { ref } from 'vue';

    import logoDark from '@/assets/img/svg/logo.svg'
    import logoLight from '@/assets/img/svg/logo-light.svg'

    import { footerLink1,footerLink2, footerLink3,footerLink4 } from '@/data/nav-data';

    const year = ref(new Date().getFullYear());
</script>
