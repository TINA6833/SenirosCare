<template>
    <div class="relative s-pt-100 bg-white dark:bg-title">
        <div class="container">
            <div class="max-w-1366 mx-auto flex items-start justify-between gap-10 flex-col lg:flex-row">
                <div class="sm:max-w-[450px] sm:mx-auto lg:mx-0 lg:max-w-[345px] xl:max-w-[477px] w-full sm:text-center lg:text-left">
                    <router-link class="cursor-pointer block" to="/" aria-label="Furnixar">
                        <img :src="logoDark" alt="" class='dark:hidden w-[120px] sm:w-[200px]'/> 
                        <img :src="logoLight" alt="" class='dark:block hidden w-[120px] sm:w-[200px]'/>
                    </router-link>                         
                    <p class="mt-4">Furnixar is a modern furniture template for an eCommerce website designed to help you create an impressive online store for your furniture. </p>
                    <div class="flex items-center gap-4 mt-6 sm:justify-center lg:justify-start">
                        <router-link to="#" class="w-10 h-10 rounded-full border border-[#8B9197] dark:border-white-light flex items-center justify-center group hover:border-primary duration-300 hover:border-transparent dark:hover:text-white dark:hover:border-transparent hover:bg-primary text-[#5D656E] dark:text-white-light hover:text-white" aria-label="Facebook">
                            <i class="fa-brands fa-facebook-f text-[#5B646D] dark:text-white group-hover:text-white duration-300"></i>
                        </router-link>
                        <router-link to="#" class="w-10 h-10 rounded-full border border-[#8B9197] dark:border-white-light flex items-center justify-center group hover:border-primary duration-300 hover:border-transparent dark:hover:text-white dark:hover:border-transparent hover:bg-primary text-[#5D656E] dark:text-white-light hover:text-white" aria-label="Twitter">
                            <i class="fa-brands fa-twitter text-[#5B646D] dark:text-white group-hover:text-white duration-300"></i>
                        </router-link>
                        <router-link to="#" class="w-10 h-10 rounded-full border border-[#8B9197] dark:border-white-light flex items-center justify-center group hover:border-primary duration-300 hover:border-transparent dark:hover:text-white dark:hover:border-transparent hover:bg-primary text-[#5D656E] dark:text-white-light hover:text-white" aria-label="Instagram">
                            <i class="fa-brands fa-instagram text-[#5B646D] dark:text-white group-hover:text-white duration-300"></i>
                        </router-link>
                        <router-link to="#" class="w-10 h-10 rounded-full border border-[#8B9197] dark:border-white-light flex items-center justify-center group hover:border-primary duration-300 hover:border-transparent dark:hover:text-white dark:hover:border-transparent hover:bg-primary text-[#5D656E] dark:text-white-light hover:text-white" aria-label="Linkedin">
                            <i class="fa-brands fa-linkedin-in text-[#5B646D] dark:text-white group-hover:text-white duration-300"></i>
                        </router-link>
                    </div>
                </div>

                <div class="grid grid-cols-2 lg:max-w-[695px] w-full sm:flex items-start justify-between gap-10 footer2-inner-wrapper">
                    <div>
                        <h4 class="leading-none mb-5 md:mb-6 text-xl">Sitemap</h4>
                        <ul class="text-title dark:text-white leading-none flex flex-col gap-4">
                            <li v-for="(item, index) in footerLink1" :key="index" class="duration-100 hover:text-primary inline-block group"><router-link class="text-underline-primary" :to="item.link">{{item.name}}</router-link></li>
                        </ul>
                    </div>
                    <div>
                        <h4 class="leading-none mb-5 md:mb-6 text-xl">Others</h4>
                        <ul class="text-title dark:text-white leading-none flex flex-col gap-4">
                            <li v-for="(item, index) in footerLink2" :key="index" class="duration-100 hover:text-primary inline-block group"><router-link class="text-underline-primary" :to="item.link">{{item.name}}</router-link></li>
                        </ul>
                    </div>
                    <div>
                        <h4 class="leading-none mb-5 md:mb-6 text-xl">Shop</h4>
                        <ul class="text-title dark:text-white leading-none flex flex-col gap-4">
                            <li v-for="(item, index) in footerLink3" :key="index" class="duration-100 hover:text-primary inline-block group"><router-link class="text-underline-primary" :to="item.link">{{item.name}}</router-link></li>
                        </ul>
                    </div>
                    <div>
                        <h4 class="leading-none mb-5 md:mb-6 text-xl">Customer Service</h4>
                        <ul class="text-title dark:text-white leading-none flex flex-col gap-4">
                            <li v-for="(item, index) in footerLink4" :key="index" class="duration-100 hover:text-primary inline-block group"><router-link class="text-underline-primary" :to="item.link">{{item.name}}</router-link></li>
                        </ul>
                    </div>
                </div>
            </div>

            <div class="max-w-[1722px] mx-auto border-t border-bdr-clr dark:border-bdr-clr-drk py-5 md:py-7 mt-7 text-center">
                <p>© {{year}} Furnixar Develop with <i class="fa-solid fa-heart text-red-600"></i> by <a href="https://shreethemes.in/" target="_blank" className='ms-1'>Shreethemes</a></p>
            </div>
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
