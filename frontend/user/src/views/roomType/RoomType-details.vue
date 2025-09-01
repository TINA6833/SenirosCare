<template>
    <div>
        <!-- 主導航欄：統一使用 MainNavbar，風格一致 -->
        <MainNavbar />

        <!-- 頂部留白，避免導覽列壓住內容 -->
        <div style="height: 140px;"></div>

        <!-- 標題與導覽皆改為房型介紹，房型名稱動態顯示 -->
        <div class="flex items-center gap-4 flex-wrap bg-overlay p-14 sm:p-16 before:bg-title before:bg-opacity-70"
            :style="{ backgroundImage: 'url(' + bg + ' )' }">
            <div class="text-center w-full">
                <h2 class="text-white text-8 md:text-[40px] font-normal leading-none text-center">房型介紹</h2>
                <ul
                    class="flex items-center justify-center gap-[10px] text-base md:text-lg leading-none font-normal text-white mt-3 md:mt-4">
                    <li><router-link to="/">首頁</router-link></li>
                    <li>/</li>
                    <li>房型介紹</li>
                    <li>/</li>
                    <li class="text-primary">{{ data?.name || '未命名房型' }}</li>
                </ul>
            </div>
        </div>

        <!-- 圖片在上，說明欄與按鈕左右排列 -->
        <div class="portfolio-single s-py-100">
            <div class="container-fluid">
                <div class="max-w-[700px] mx-auto w-full flex flex-col items-center">
                    <!-- 圖片 -->
                    <div class="w-full mb-8">
                        <img class="w-full h-auto rounded-lg object-cover" :src="data?.image || image1" alt="房型圖片" />
                    </div>
                    <!-- 說明欄與按鈕左右排列 -->
                    <div class="w-full flex flex-col md:flex-row md:items-start md:justify-between gap-6">
                        <!-- 左側：標籤、房型名稱、描述 -->
                        <div class="flex-1">
                            <div class="mb-4 text-left">
                                <span v-for="tag in tags" :key="tag.id"
                                    class="inline-block text-title font-medium text-[15px] leading-none p-[10px] rounded-md bg-primary-midum mr-2 mb-2">
                                    {{ tag.name }}
                                </span>
                            </div>
                            <h2 class="font-semibold leading-none mt-4 md:mt-6 text-2xl md:text-3xl text-left">
                                {{ data?.name || '未命名房型' }}
                            </h2>
                            <p class="mt-3 text-base sm:text-lg text-left">
                                {{ data?.description || '尚無房型描述' }}
                            </p>
                        </div>
                        <!-- 右側：預約表單按鈕與返回上一頁按鈕並排 -->
                        <div
                            class="flex-shrink-0 flex items-center md:items-start justify-center md:justify-end w-full md:w-auto gap-4">
                            <button
                                class="px-8 py-4 bg-primary text-white rounded hover:bg-primary-dark transition text-lg"
                                @click="showForm = true">
                                填寫預約表單
                            </button>
                            <!-- 重點註解：返回上一頁按鈕放在預約表單按鈕旁邊 -->
                            <button
                                class="px-8 py-4 bg-gray-400 text-white rounded hover:bg-gray-600 transition text-lg"
                                @click="goBack">
                                返回上一頁
                            </button>
                        </div>
                    </div>
                    <!-- 房型詳細表格 -->
                    <div class="bg-[#FAFAFA] dark:bg-dark-secondary p-8 mt-[30px] block rounded-lg w-full mx-auto">
                        <table class="meta-table w-full">
                            <tbody>
                                <tr class="border-b border-black/10 dark:border-bdr-clr-drk py-4">
                                    <th class="text-[18px] sm:text-[20px] font-medium">價格</th>
                                    <td class='text-base sm:text-lg'>{{ data?.price ? '$' + data.price : '未定價' }}</td>
                                </tr>
                                <tr class="border-b border-black/10 dark:border-bdr-clr-drk py-4">
                                    <th>人數</th>
                                    <td>{{ data?.capacity ? data.capacity + '人' : '未設定' }}</td>
                                </tr>
                                <tr class="border-b border-black/10 dark:border-bdr-clr-drk py-4">
                                    <th>狀態</th>
                                    <td>{{ data?.isAvailable ? '可預約' : '暫停開放' }}</td>
                                </tr>
                                <tr class="py-4">
                                    <th>建立日期</th>
                                    <td>
                                        {{ data?.createdAt ? formatDate(data.createdAt) : '-' }}
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <!-- 重點註解：留言區塊與詳細頁主內容完全等寬，max-w-[894px] w-full mx-auto -->
                    <div class="s-py-50 w-full">
                        <h4 class="font-semibold leading-none mb-5 md:mb-6">留言 ( {{ approvedComments.length }} )</h4>
                        <div class="p-5 sm:p-[30px] bg-[#F8F5F0] dark:bg-dark-secondary rounded-lg">
                            <div v-for="item in approvedComments" :key="item.id"
                                class="flex items-center gap-5 py-5 border-b last:border-b-0 border-bdr-clr dark:border-[#3c434a]">
                                <div class="w-[72px] h-[72px] rounded-full overflow-hidden bg-gray-200 flex-shrink-0">
                                    <img class="w-full h-full object-cover"
                                        :src="`https://i.pravatar.cc/72?u=${item.memberId}`" alt="comment">
                                </div>
                                <div class="flex-1">
                                    <div class="font-medium leading-none mb-1">{{ item.memberName || '匿名會員' }}</div>
                                    <div class="mb-2">{{ item.content }}</div>
                                    <div class="text-xs text-gray-500">{{ formatDate(item.createdAt) }}</div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- RoomType-details.vue 主要內容區塊 -->
                    <div class="pt-3 w-full" data-aos="fade-up">
                        <h4 class="leading-none text-xl sm:text-2xl mb-5 sm:mb-6">留下您的留言</h4>
                        <form @submit.prevent="handleSubmit">
                            <div class="grid gap-[15px]">
                                <div>
                                    <input v-model="form.memberName"
                                        class="w-full h-12 md:h-14 border border-[#3C474E] text-title bg-transparent placeholder:text-paragraph dark:text-white focus:border-primary dark:focus:border-primary dark:border-white-light dark:placeholder:text-white-light p-4 outline-none duration-300"
                                        type="text" placeholder="姓名">
                                </div>
                                <div>
                                    <textarea v-model="form.content"
                                        class="w-full h-28 md:h-[170px] border border-[#3C474E] text-title bg-transparent placeholder:text-paragraph dark:text-white focus:border-primary dark:focus:border-primary dark:border-white-light dark:placeholder:text-white-light p-4 outline-none duration-300"
                                        name="Message" placeholder="請輸入留言內容..."></textarea>
                                </div>
                            </div>
                            <div class="mt-4 md:mt-6 flex gap-2">
                                <!-- 更新按鈕樣式 -->
                                <button
                                    class="px-8 py-4 bg-primary text-white rounded hover:bg-primary-dark transition text-lg flex-1"
                                    type="submit">
                                    <span v-if="showSent">已送出</span>
                                    <span v-else>送出留言</span>
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <!-- 浮動預約表單視窗 -->
        <div v-if="showForm" class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
            <!-- 重點註解：將 max-w-[600px] 改為 max-w-[900px]，讓浮動視窗更寬敞 -->
            <div class="bg-white rounded-lg shadow-lg max-w-[900px] w-full relative">
                <button class="absolute top-4 right-4 text-gray-500 hover:text-gray-700 text-2xl"
                    @click="showForm = false" title="關閉">×</button>
                <!-- 重點註解：嵌入 ReservationForm 元件，帶入房型資訊 -->
                <ReservationForm :roomTypeId="data?.id" :roomTypeName="data?.name" :onSuccess="closeReservation" />
            </div>
        </div>

        <FooterThree />
        <ScrollToTop />
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MainNavbar from '@/components/navbar/main-navbar.vue';
import FooterThree from '@/components/footer/footer-three.vue';
import ScrollToTop from '@/components/scroll-to-top.vue';
import ReservationForm from './RoomReservationForm.vue'

import bg from '@/assets/img/shortcode/breadcumb.jpg'
import image1 from '@/assets/img/gallery/portfolio-03/portfolio-02.jpg'

import { getRoomTypeById } from '@/service/roomTypeService'
import { getAllFeatures } from '@/service/featureService'
import { getFeatureIdsByRoomType } from '@/service/roomTypeFeatureService'
import { getRoomCommentsByRoomType, addRoomComment } from '@/service/roomCommentService'
import { useConfirmDialog } from '@/composables/useConfirmDialog' // ★重點：引入確認對話框
import { useToast } from '@/composables/useToast' // ★重點：引入 Toast

const route = useRoute()
const router = useRouter()
const data = ref(null)
const tags = ref([])
const showForm = ref(false)
const roomTypeId = Number(route.params.id) || 1
const comments = ref([]) // 重點註解：留言原始資料
const showSent = ref(false) // 重點註解：送出狀態，控制按鈕文字
const approvedComments = computed(() =>
    comments.value.filter(comment => comment.is_approved)
) // 重點註解：只顯示通過審核的留言

const confirmDialog = useConfirmDialog() // ★重點：取得確認對話框狀態
const toast = useToast() // ★重點：取得 Toast 狀態

async function fetchComments() {
    const result = await getRoomCommentsByRoomType(roomTypeId)
    comments.value = (result || []).map(item => ({
        ...item,
        memberName: item.memberName || '匿名會員'
    }))
}

onMounted(async () => {
    const id = parseInt(route.params.id)
    data.value = await getRoomTypeById(id)
    const featureIds = await getFeatureIdsByRoomType(id)
    const allFeatures = await getAllFeatures()
    tags.value = allFeatures.filter(f => featureIds.includes(f.id))
    await fetchComments()
})

const form = ref({
    memberId: 2, // ★請依登入會員ID動態取得
    memberName: '', // 可選填
    roomTypeId,
    content: ''
})

function formatDate(dateStr) {
    // 重點註解：將 ISO 字串轉為「YYYY-MM-DD HH:mm」格式
    const d = new Date(dateStr)
    if (isNaN(d)) return '-'
    const yyyy = d.getFullYear()
    const mm = String(d.getMonth() + 1).padStart(2, '0')
    const dd = String(d.getDate()).padStart(2, '0')
    const hh = String(d.getHours()).padStart(2, '0')
    const min = String(d.getMinutes()).padStart(2, '0')
    return `${yyyy}-${mm}-${dd} ${hh}:${min}`
}

function goBack() {
    router.back()
}

async function handleSubmit() {
    if (!form.value.content) return

    // ★重點：送出前先彈出確認視窗
    try {
        const confirmed = await confirmDialog.showConfirmDialog({
            title: '送出留言',
            message: '您確定要送出這則留言嗎？',
            type: 'info',
            confirmText: '送出',
            cancelText: '取消',
            confirmButtonClass: 'btn-primary',
            icon: 'heroicons:information-circle'
        })
        if (!confirmed) return

        // ★重點：確認後才送出留言
        await addRoomComment({
            roomTypeId: form.value.roomTypeId,
            memberId: form.value.memberId,
            memberName: form.value.memberName,
            content: form.value.content
        })
        form.value.content = ''
        form.value.memberName = ''
        await fetchComments()
        showSent.value = true

        // ★重點：留言送出成功後跳出 Toast 通知
        toast.showToast({
            title: '留言已送出',
            message: '感謝您的留言！',
            type: 'success'
        })

        setTimeout(() => showSent.value = false, 2000)
    } catch {
        // ★重點：使用者取消或錯誤不做任何事
    }
}
</script>
