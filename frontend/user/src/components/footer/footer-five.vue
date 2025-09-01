<template>
    <div>
        <div class="py-[50px] border-b border-[#3A454F] flex items-start gap-10 justify-between flex-col xl:flex-row">
            <div class="max-w-md sm:mx-auto xl:mx-0 xl:max-w-[382px] w-full sm:text-center xl:text-left">
                <router-link to="#">  
                    <img :src="logo" alt="" class="text-white sm:mx-auto xl:mx-0 w-[150px] sm:w-[200px]"/>                                                        
                </router-link>
                <p class="mt-[15px] text-white">Furnixar is a modern furniture template for an eCommerce website designed to help you create an impressive online store for your furniture. </p>
            </div>

            <div class="xl:max-w-[937px] w-full grid grid-cols-2 sm:grid-cols-3 lg:flex items-start justify-between gap-10">
                <div>
                    <h4 class="leading-none mb-5 md:mb-6 text-xl text-white">Sitemap</h4>
                    <ul class="text-white leading-none flex flex-col gap-4">
                        <li v-for="(item, index) in footerLink1" :key="index" class="duration-100 hover:text-primary inline-block group"><router-link class="text-underline-primary" :to="item.link">{{item.name}}</router-link></li>
                    </ul>
                </div>
            
                <div>
                    <h4 class="leading-none mb-5 md:mb-6 text-xl text-white">Others</h4>
                    <ul class="text-white leading-none flex flex-col gap-4">
                        <li v-for="(item, index) in footerLink2" :key="index" class="duration-100 hover:text-primary inline-block group"><router-link class="text-underline-primary" :to="item.link">{{item.name}}</router-link></li>
                    </ul>
                </div>
            
                <div>
                    <h4 class="leading-none mb-5 md:mb-6 text-xl text-white">Social Media</h4>
                    <ul class="text-white leading-none flex flex-col gap-4">
                        <li class="duration-100 hover:text-primary inline-block group"><router-link class="gap-3 flex items-center" to="#">
                            <i class="fa-brands fa-facebook-f text-white text-opacity-70 group-hover:text-primary duration-300"></i>                                    
                            Facebook
                        </router-link></li>
                        <li class="duration-100 hover:text-primary inline-block group"><router-link class="gap-3 flex items-center" to="#">
                            <i class="fa-brands fa-twitter text-white text-opacity-70 group-hover:text-primary duration-300"></i>                        
                            Twitter
                        </router-link></li>
                        <li class="duration-100 hover:text-primary inline-block group"><router-link class="gap-3 flex items-center" to="#">
                            <i class="fa-brands fa-instagram text-white text-opacity-70 group-hover:text-primary duration-300"></i>                          
                            Instagram
                        </router-link></li>
                        <li class="duration-100 hover:text-primary inline-block group"><router-link class="gap-3 flex items-center" to="#">
                            <i class="fa-brands fa-linkedin-in text-white text-opacity-70 group-hover:text-primary duration-300"></i>                    
                            Linkedin
                        </router-link></li>
                    </ul>
                </div>
            
                <div>
                    <h4 class="leading-none mb-5 md:mb-6 text-xl text-white">Shop</h4>
                    <ul class="text-white leading-none flex flex-col gap-4">
                        <li v-for="(item, index) in footerLink3" :key="index" class="duration-100 hover:text-primary inline-block group"><router-link class="text-underline-primary" :to="item.link">{{item.name}}</router-link></li>
                    </ul>
                </div>

                <div class="col-span-2 sm:col-span-1">
                    <h4 class="leading-none mb-5 md:mb-6 text-xl text-white">Customer Service</h4>
                    <ul class="text-white leading-none flex flex-col gap-4">
                        <li v-for="(item, index) in footerLink4" :key="index" class="duration-100 hover:text-primary inline-block group"><router-link class="text-underline-primary" :to="item.link">{{item.name}}</router-link></li>
                    </ul>
                </div>
            </div>
        </div>
        <div class="py-5 md:py-[30px] text-center">
            <p class="text-white leadint-none">© {{year}} Furnixar Develop with <i class="fa-solid fa-heart text-red-600"></i> by <a href="https://shreethemes.in/" target="_blank" className='ms-1'>Shreethemes</a></p>
        </div>
    </div>
</template>

<script setup>
    import { ref } from 'vue';
    import logo from '@/assets/img/footer-logo.svg'
    import { footerLink1, footerLink2, footerLink3, footerLink4 } from '@/data/nav-data';
    
    const year = ref(new Date().getFullYear());
</script>