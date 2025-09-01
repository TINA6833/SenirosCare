<template>
    <div class="container-fluid">
        <div class="max-w-[985px] mx-auto">
            <div class="product-dtls-navtab border-y border-bdr-clr dark:border-bdr-clr-drk">
                <ul id="user-nav-tabs" class="text-title dark:text-white text-base sm:text-lg lg:text-xl flex leading-none gap-3 sm:gap-6 md:gap-12 lg:gap-24 justify-start max-w-md sm:max-w-full">
                    <li class="py-3 sm:py-5 lg:6 relative before:absolute before:w-full before:h-[1px] before:bg-title before:top-full before:left-0 before:duration-300 dark:before:bg-white before:opacity-0 active">
                        <router-link to="#" class="duration-300 hover:text-primary">Description</router-link>
                    </li>
                </ul>
            </div>
            <div id="content" class="mt-5 sm:mt-8 lg:mt-12 mx-0 sm:mr-5 md:mr-8 lg:mr-12">
                <div>
                    <p class="sm:text-lg">{{ description }}</p>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { defineProps } from 'vue'

defineProps({
    description: String
})
</script>
