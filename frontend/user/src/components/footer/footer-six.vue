<template>
    <div class="s-pt-100 2xl:pb-[100px]">
        <div class="container max-w-1366 mx-auto">
            <div class="flex flex-col 2xl:flex-row items-start justify-between gap-10">
                <div class="max-w-[360px] 2xl:max-w-[198px] w-full text-left sm:text-center 2xl:text-left sm:mx-auto 2xl:mx-0">
                    <router-link class="cursor-pointer block" to="/" aria-label="Furnixar">
                        <img :src="logoDark" alt="" class='dark:hidden w-[120px] sm:w-[200px]'/> 
                        <img :src="logoLight" alt="" class='dark:block hidden w-[120px] sm:w-[200px]'/>
                    </router-link> 
                    <p class="mt-[15px] hidden 2xl:block">© {{year}} Furnixar Develop with <i class="fa-solid fa-heart text-red-600"></i> by <a href="https://shreethemes.in/" target="_blank" className='ms-1'>Shreethemes</a></p>
                </div>
                <div class="grid grid-cols-2 sm:grid-cols-3 2xl:max-w-[1050px] w-full lg:flex items-start justify-between gap-10">
                    <div class="lg:w-[300px] sm:order-7 2xl:order-none col-span-2 md:col-span-1">
                        <h4 class="leading-none mb-5 md:mb-6 text-xl">Subscribe Newsletter</h4>
                        <div class="flex flex-col gap-[10px] sm:items-start">
                            <input class="bg-white dark:bg-dark-secondary w-full h-12 md:h-14 p-4 outline-none dark:text-white placeholder:text-title dark:placeholder:text-white" type="text" placeholder="Enter your email address">
                            <button class="btn btn-solid" data-text="Subscribe"><span>Subscribe</span></button>
                        </div>
                    </div>
                    <div>
                        <h4 class="leading-none mb-5 md:mb-6 text-xl">Sitemap</h4>
                        <ul class="text-title dark:text-white leading-none flex flex-col gap-4">
                            <li v-for="(item, index) in footerLink1" :key="index" class="duration-100 hover:text-primary inline-block group"><a class="text-underline-primary" :to="item.link">{{item.name}}</a></li>
                        </ul>
                    </div>
                    <div>
                        <h4 class="leading-none mb-5 md:mb-6 text-xl">Others</h4>
                        <ul class="text-title dark:text-white leading-none flex flex-col gap-4">
                            <li v-for="(item, index) in footerLink2" :key="index" class="duration-100 hover:text-primary inline-block group"><a class="text-underline-primary" :to="item.link">{{item.name}}</a></li>
                        </ul>
                    </div>
                    <div>
                        <h4 class="leading-none mb-5 md:mb-6 text-xl">Shop</h4>
                        <ul class="text-title dark:text-white leading-none flex flex-col gap-4">
                            <li v-for="(item, index) in footerLink3" :key="index" class="duration-100 hover:text-primary inline-block group"><a class="text-underline-primary" :to="item.link">{{item.name}}</a></li>
                        </ul>
                    </div>
                    <div>
                        <h4 class="leading-none mb-5 md:mb-6 text-xl">Customer Service</h4>
                        <ul class="text-title dark:text-white leading-none flex flex-col gap-4">
                            <li v-for="(item, index) in footerLink4" :key="index" class="duration-100 hover:text-primary inline-block group"><a class="text-underline-primary" :to="item.link">{{item.name}}</a></li>
                        </ul>
                    </div>
                </div>
            </div>
            <div class="block 2xl:hidden py-5 text-center mt-14">
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
