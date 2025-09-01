<template>
    <div :class="classList">
        <div v-for="(item, index) in productList" :key="index" class="group">
            <div class="relative overflow-hidden">
                <!-- 圖片網址加上 /images/ 前綴 -->
                <router-link :to="`/product-details/${item.id}`">
                    <img
                      class="w-full transform group-hover:scale-110 duration-300"
                      :src="getImageUrl(item.image)"
                      alt="shop"
                    />
                </router-link>
            </div>
            <div class="md:px-2 lg:px-4 xl:px-6 lg:pt-6 pt-5 flex gap-4 md:gap-5 flex-col">
                <h4 class="font-medium leading-none dark:text-white text-lg">NT$ {{item.unitPrice}}</h4>
                <div>
                    <h5 class="font-normal dark:text-white text-xl leading-[1.5]">
                        <router-link :to="`/product-details/${item.id}`" class="text-underline">
                            {{item.name}}
                        </router-link>
                    </h5>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { defineProps } from 'vue'

// 圖片網址組合
function getImageUrl(image) {
  if (!image) return ''
  // 若已經是完整網址則直接回傳
  if (image.startsWith('http')) return image
  // 否則加上 /images/ 前綴
  return `http://localhost:8080/images/${image}`
}

defineProps({
    productList: Array,
    classList: String
})
</script>
