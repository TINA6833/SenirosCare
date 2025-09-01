<template>
    <div :class="classList">
        <!-- 重點註解：拉大不同房型卡片的上下間距 -->
        <div v-for="(item, index) in productList.filter(room => room.isAvailable)" :key="index" class="group"
            style="margin-bottom: 40px;">
            <div class="relative overflow-hidden" style="height:320px;">
                <router-link :to="`/RoomType-details/${item.id}`">
                    <img class="w-full h-full object-cover transform group-hover:scale-110 duration-300"
                        :src="item.image" alt="shop" style="height:280px; width:100%; border-radius:12px;" />
                </router-link>
                <!-- 愛心收藏功能 -->
                <button class="absolute z-20 top-5 left-5" @click.stop="handleFavoriteClick(item.id)"
                    style="width:40px;height:40px;display:flex;align-items:center;justify-content:center;background:none;border:none;">
                    <i class="mdi"
                        :class="[isFavorite(item.id) ? 'mdi-cards-heart text-red-500 text-3xl' : 'mdi-cards-heart-outline text-gray-400 text-3xl']"
                        style="transition:color 0.2s;"></i>
                </button>
                <!-- 功能按鈕區塊 -->
                <div
                    class="absolute z-10 top-[50%] right-3 transform -translate-y-[40%] opacity-0 duration-300 transition-all group-hover:-translate-y-1/2 group-hover:opacity-100 flex flex-col items-end gap-3">
                    <button
                        class="bg-white dark:bg-title dark:text-white bg-opacity-80 flex items-center justify-center gap-2 px-4 py-2 text-sm leading-[1.6] text-title rounded-[40px] h-10 overflow-hidden new-product-icon"
                        @click.stop="handleFavoriteClick(item.id)">
                        <i class="mdi"
                            :class="[isFavorite(item.id) ? 'mdi-cards-heart text-red-500 text-[20px]' : 'mdi-cards-heart-outline text-gray-400 text-[20px]']"></i>
                        <span class="mt-0.5">加入收藏</span>
                    </button>
                    <button
                        class="bg-white dark:bg-title dark:text-white bg-opacity-80 flex items-center justify-center gap-2 px-4 py-2 text-sm leading-[1.6] text-title rounded-[40px] h-10 overflow-hidden new-product-icon"
                        @click.stop="openReservation(item)">
                        <i class="mdi mdi-shopping-outline dark:text-white text-[20px]"></i>
                        <span class="mt-0.5">預約表單</span>
                    </button>
                    <button
                        class="bg-white dark:bg-title dark:text-white bg-opacity-80 flex items-center justify-center gap-2 px-4 py-2 text-sm leading-[1.6] text-title rounded-[40px] h-10 overflow-hidden new-product-icon quick-view"
                        @click="$router.push(`/RoomType-details/${item.id}`)">
                        <i class="mdi mdi-eye-outline dark:text-white text-[20px]"></i>
                        <span class="mt-0.5">詳細內容</span>
                    </button>
                </div>
            </div>
            <!-- 價格與房名區塊，重點：往上貼近圖片，間距縮小 -->
            <div class="flex flex-col items-start mt-0 mb-0 px-2">
                <h4 class="font-medium leading-none dark:text-white text-lg mb-0.5">${{ item.price }}</h4>
                <h5 class="font-normal dark:text-white text-xl leading-[1.5] mb-0.5">
                    <router-link :to="`/RoomType-details/${item.id}`" class="text-underline">
                        {{ item.name }}
                    </router-link>
                </h5>
            </div>
            <!-- 其他資訊區塊（如 features） -->
            <div class="md:px-2 lg:px-4 xl:px-6 pt-2 flex gap-4 md:gap-5 flex-col">
                <div v-if="item.features && item.features.length" class="flex flex-wrap gap-2 mt-2">
                    <span v-for="feature in item.features" :key="feature.id"
                        class="px-3 py-1 bg-gray-100 rounded-full text-xs text-title">
                        {{ feature.name }}
                    </span>
                </div>
            </div>
        </div>
        <!-- 預約表單浮動視窗 -->
        <div v-if="showReservationForm"
            class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
            <div class="bg-white rounded-lg shadow-lg max-w-[900px] w-full relative"
                style="min-height:320px; height:420px;">
                <button class="absolute top-4 right-4 text-gray-500 hover:text-gray-700 text-2xl"
                    @click="closeReservation" title="關閉">×</button>
                <ReservationForm :roomTypeId="selectedRoomType?.id" :roomTypeName="selectedRoomType?.name"
                    :onSuccess="closeReservation" />
            </div>
        </div>
    </div>
</template>

<script setup>
// 重點註解：愛心狀態與資料庫綁定，父元件傳入 favoriteIds，點擊即時更新
import { defineProps, ref, defineEmits } from 'vue'
import ReservationForm from '@/views/roomType/RoomReservationForm.vue'
import { addRoomTypeFavorite, removeRoomTypeFavorite } from '@/service/favoriteService'

const props = defineProps({
    productList: Array,
    classList: String,
    favoriteIds: Array, // 父元件傳入會員已收藏房型 id 陣列
    memberId: Number // 父元件傳入會員ID
})

const emits = defineEmits(['updateFavorite'])

// 直接使用 props.favoriteIds，確保與父元件資料庫同步
function isFavorite(id) {
    // 父元件已保證為數字陣列
    return props.favoriteIds && props.favoriteIds.includes(Number(id))
}

// 收藏/取消收藏，API 串接資料庫，並即時更新父元件 favoriteIds
async function handleFavoriteClick(roomTypeId) {
    const idNum = Number(roomTypeId)
    if (isFavorite(idNum)) {
        // 取消收藏
        const res = await removeRoomTypeFavorite(props.memberId || 1, idNum)
        if (res.success) {
            emits('updateFavorite', props.favoriteIds.filter(fid => Number(fid) !== idNum))
        }
    } else {
        // 新增收藏
        const res = await addRoomTypeFavorite(props.memberId || 1, idNum)
        if (res.success) {
            emits('updateFavorite', [...props.favoriteIds, idNum])
        }
    }
}

// 預約表單浮動視窗狀態
const showReservationForm = ref(false)
const selectedRoomType = ref(null)

function openReservation(item) {
    selectedRoomType.value = item
    showReservationForm.value = true
}
function closeReservation() {
    showReservationForm.value = false
    selectedRoomType.value = null
}
</script>

<!-- 重點註解：如需全域調整，可在 style 區塊加上 .room-card { margin-bottom: 40px; } 並於 v-for 外層加上 class="room-card" -->
