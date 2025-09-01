<template>
    <div class="header-area absolute bg-white dark:bg-title dark:bg-dark  left-0 right-0 top-0 lg:top-[10px] xl:top-[15px] z-50 before:absolute before:h-full before:top-0 before:left-0 before:z-[-1] before:bg-white dark:before:bg-title before:w-[190px] sm:before:w-[250px] lg:before:w-[22%] xl:before:w-[19%]"
        :class="scroll ? 'sticky-header bg-white' : 'lg:bg-transparent'">
        <div class="container-fluid">
            <div
                class="flex items-center justify-between gap-x-6 max-w-[1720px] mx-auto relative py-[10px] sm:py-4 lg:py-0">
                <router-link class="cursor-pointer block" to="/" aria-label="Furnixar">
                    <img :src="logoDark" alt="" class='dark:hidden w-[120px] sm:w-[200px]' />
                    <img :src="logoLight" alt="" class='dark:block hidden w-[120px] sm:w-[200px]' />
                </router-link>

                <div class="flex items-center gap-12 2xl:gap-24">
                    <div class="main-menu absolute z-50 w-full lg:w-auto top-full left-0 lg:static bg-white dark:bg-title lg:bg-transparent lg:dark:bg-transparent px-5 sm:px-[30px] py-[10px] sm:py-5 lg:px-0 lg:py-0"
                        :class="toggle ? 'active' : ''">
                        <ul class="text-lg leading-none text-title dark:text-white lg:flex lg:gap-[30px]">
                            <!-- Roomtype -->
                            <li class="relative" :class="current === '/roomType' || current === '' ? 'active' : ''">
                                <router-link to="/roomType">房型一覽</router-link>
                            </li>
                            <li class="relative" :class="current === '/facility' ? 'active' : ''">
                                <router-link to="/facility">設施導覽</router-link>
                            </li>

                            <li class="relative" :class="current === '/contact' ? 'active' : ''">
                                <router-link to="/contact">據點資訊</router-link>
                            </li>
                            <!-- Device -->
                            <li class="relative" :class="current === '/products' ? 'active' : ''">
                                <router-link to="/products">輔具商城</router-link>
                            </li>
                            <!-- Rehabus -->
                            <li class="relative" :class="current === '/rehabus' ? 'active' : ''">
                                <router-link to="/rehabus">復康巴士</router-link>
                            </li>
                            <!-- Caregiver -->
                            <li class="relative" :class="current === '/caregiver' || current.startsWith('/caregiver') ? 'active' : ''">
                                <router-link to="/caregiver">照服員預約</router-link>
                            </li>
                            <!-- Activity -->
                            <li class="relative" :class="current === '/activity' ? 'active' : ''">
                                <router-link to="/activity">活動報名</router-link>
                            </li>
                        </ul>
                    </div>
                    <NavMenuOne :toggle="toggle" @toggle-change="toggle = $event" />
                </div>

            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';

import logoDark from '@/assets/img/svg/logo.png'
import logoLight from '@/assets/img/svg/logo-light.svg'

import NavMenuOne from './nav-menu-one.vue';

import { useRoute } from 'vue-router';

const toggle = ref(false);

const router = useRoute();
const current = ref(router.path);

const scroll = ref(false)

const handleScroll = () => {
    if (window.scrollY >= 50) {
        scroll.value = true
    } else {
        scroll.value = false
    }
}

onMounted(() => {
    window.scrollTo(0, 0)
    window.addEventListener('scroll', handleScroll)
})

</script>