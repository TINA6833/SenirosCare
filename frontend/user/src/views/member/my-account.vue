<template>
    <div>
        <MainNavbar/>
        <div style="height: 140px;"></div>

        <!-- [重點] 使用 AuthPrompt 元件包裝需要登入的內容 -->
        <AuthPrompt title="會員中心" message="請先登入以查看您的會員資訊與訂單記錄" login-button-text="使用 Line 登入" :show-toast="true">
            <!-- [重點] 原本的會員中心內容，只有在登入後才會顯示 -->
            <div class="flex items-center gap-4 flex-wrap bg-overlay p-14 sm:p-16 before:bg-title before:bg-opacity-70"
                :style="{ backgroundImage: 'url(' + bg + ')' }">
                <div class="text-center w-full">
                    <h2 class="text-white text-8 md:text-[40px] font-normal leading-none text-center">會員中心</h2>
                </div>
            </div>

            <div class="s-py-100" data-aos="fade-up">
                <div class="container-fluid">
                    <div
                        class="max-w-[1720px] mx-auto flex items-start gap-8 md:gap-12 2xl:gap-24 flex-col md:flex-row my-profile-navtab">
                        <!-- [重點] 側邊導覽列 -->
                        <div class="w-full md:w-[200px] lg:w-[300px] flex-none">
                            <ProfileTab />
                        </div>

                        <!-- [重點] 主要內容區域 -->
                        <div class="w-full md:w-auto md:flex-1 overflow-auto">
                            <!-- [重點] 整合式會員資料區塊 -->
                            <div
                                class="w-full max-w-[951px] bg-[#F8F8F9] dark:bg-dark-secondary p-5 sm:p-8 lg:p-[50px] mb-6">
                                <!-- [重點] 頭部區域 -->
                                <div class="flex items-center justify-between mb-6">
                                    <div class="flex items-center gap-4">
                                        <!-- [重點] 會員頭像區塊 -->
                                        <div
                                            class="w-16 h-16 rounded-full overflow-hidden bg-primary/10 flex items-center justify-center flex-none relative">
                                            <img v-if="memberData?.imagePath" :src="memberData.imagePath"
                                                :alt="memberData?.memberName || '會員頭像'"
                                                class="w-full h-full object-cover" />
                                            <i v-else class="fa-solid fa-user text-primary text-2xl"></i>
                                            <!-- [重點] Line 同步標示 -->
                                            <div
                                                class="absolute -bottom-1 -right-1 w-5 h-5 bg-green-500 rounded-full border-2 border-white dark:border-title flex items-center justify-center">
                                                <i class="fab fa-line text-white text-[10px]"></i>
                                            </div>
                                        </div>
                                        <div>
                                            <h3
                                                class="font-semibold leading-none text-xl md:text-2xl text-title dark:text-white">
                                                {{ memberData?.memberName || 'Loading...' }}
                                            </h3>
                                        </div>
                                    </div>

                                    <!-- [重點] 編輯模式切換按鈕 -->
                                    <div class="flex items-center gap-2">
                                        <button v-if="!isEditMode" @click="toggleEditMode"
                                            class="flex items-center gap-2 px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90 transition-colors duration-300">
                                            <i class="fa-solid fa-edit"></i>
                                            <span>編輯資料</span>
                                        </button>

                                        <div v-else class="flex items-center gap-2">
                                            <button @click="handleSaveProfile" :disabled="isSaving"
                                                class="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors duration-300 disabled:opacity-50">
                                                <i v-if="isSaving" class="fa-solid fa-spinner fa-spin"></i>
                                                <i v-else class="fa-solid fa-save"></i>
                                                <span v-if="isSaving">儲存中...</span>
                                                <span v-else>儲存變更</span>
                                            </button>

                                            <button @click="cancelEdit" :disabled="isSaving"
                                                class="flex items-center gap-2 px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors duration-300 disabled:opacity-50">
                                                <i class="fa-solid fa-times"></i>
                                                <span>取消</span>
                                            </button>
                                        </div>
                                    </div>
                                </div>

                                <p class="text-base sm:text-lg mb-8 text-justify text-gray-600 dark:text-gray-300">
                                    歡迎來到銀髮照護平台！您可以在這裡管理您的個人資料、查看訂單記錄，以及使用各種照護服務。
                                </p>

                                <!-- [重點] 整合式資料顯示與編輯區塊 -->
                                <div class="grid grid-cols-1 md:grid-cols-2 gap-6 md:gap-8">
                                    <!-- [重點] 基本聯絡資訊 -->
                                    <div class="space-y-4">
                                        <h4
                                            class="text-lg font-semibold text-title dark:text-white border-b border-primary pb-2">
                                            <i class="fa-solid fa-address-book text-primary mr-2"></i>
                                            聯絡資訊
                                        </h4>
                                        <div class="space-y-3">
                                            <!-- [重點] 會員姓名 - 可編輯 -->
                                            <div
                                                class="flex items-center gap-3 p-3 bg-white dark:bg-title rounded-lg group">
                                                <div
                                                    class="w-8 h-8 bg-primary/10 rounded-full flex items-center justify-center flex-none">
                                                    <i class="fa-solid fa-user text-primary text-sm"></i>
                                                </div>
                                                <div class="flex-1">
                                                    <div class="flex items-center gap-2">
                                                        <p class="text-xs text-gray-500 dark:text-gray-400">會員姓名</p>
                                                        <i class="fa-solid fa-edit text-primary text-xs opacity-60"></i>
                                                    </div>
                                                    <!-- [重點] 顯示模式 -->
                                                    <p v-if="!isEditMode"
                                                        class="font-medium text-title dark:text-white">
                                                        {{ memberData?.memberName || '尚未設定' }}
                                                    </p>
                                                    <!-- [重點] 編輯模式 -->
                                                    <input v-else v-model="editForm.memberName"
                                                        class="w-full mt-1 px-2 py-1 bg-gray-50 dark:bg-dark-secondary border border-gray-200 dark:border-gray-600 text-title dark:text-white focus:border-primary outline-none rounded text-sm"
                                                        type="text" placeholder="請輸入您的姓名" required />
                                                </div>
                                            </div>

                                            <!-- [重點] 手機號碼 - 可編輯 -->
                                            <div class="flex items-center gap-3 p-3 bg-white dark:bg-title rounded-lg">
                                                <div
                                                    class="w-8 h-8 bg-primary/10 rounded-full flex items-center justify-center flex-none">
                                                    <i class="fa-solid fa-phone text-primary text-sm"></i>
                                                </div>
                                                <div class="flex-1">
                                                    <div class="flex items-center gap-2">
                                                        <p class="text-xs text-gray-500 dark:text-gray-400">手機號碼</p>
                                                        <i class="fa-solid fa-edit text-primary text-xs opacity-60"></i>
                                                    </div>
                                                    <p v-if="!isEditMode"
                                                        class="font-medium text-title dark:text-white">
                                                        {{ memberData?.mainPhone || '尚未設定' }}
                                                    </p>
                                                    <input v-else v-model="editForm.mainPhone"
                                                        class="w-full mt-1 px-2 py-1 bg-gray-50 dark:bg-dark-secondary border border-gray-200 dark:border-gray-600 text-title dark:text-white focus:border-primary outline-none rounded text-sm"
                                                        type="tel" placeholder="請輸入手機號碼" />
                                                </div>
                                            </div>

                                            <!-- [重點] 電子郵件 - 可編輯 -->
                                            <div class="flex items-center gap-3 p-3 bg-white dark:bg-title rounded-lg">
                                                <div
                                                    class="w-8 h-8 bg-primary/10 rounded-full flex items-center justify-center flex-none">
                                                    <i class="fa-solid fa-envelope text-primary text-sm"></i>
                                                </div>
                                                <div class="flex-1">
                                                    <div class="flex items-center gap-2">
                                                        <p class="text-xs text-gray-500 dark:text-gray-400">電子郵件</p>
                                                        <i class="fa-solid fa-edit text-primary text-xs opacity-60"></i>
                                                    </div>
                                                    <!-- [重點] 修復電子郵件顯示 -->
                                                    <p v-if="!isEditMode"
                                                        class="font-medium text-title dark:text-white break-all">
                                                        {{ memberData?.email || '尚未設定' }}
                                                    </p>
                                                    <input v-else v-model="editForm.email"
                                                        class="w-full mt-1 px-2 py-1 bg-gray-50 dark:bg-dark-secondary border border-gray-200 dark:border-gray-600 text-title dark:text-white focus:border-primary outline-none rounded text-sm"
                                                        type="email" placeholder="請輸入電子郵件" />
                                                </div>
                                            </div>

                                            <!-- [重點] 居住地址 - 可編輯 -->
                                            <div class="flex items-center gap-3 p-3 bg-white dark:bg-title rounded-lg">
                                                <div
                                                    class="w-8 h-8 bg-primary/10 rounded-full flex items-center justify-center flex-none">
                                                    <i class="fa-solid fa-location-dot text-primary text-sm"></i>
                                                </div>
                                                <div class="flex-1">
                                                    <div class="flex items-center gap-2">
                                                        <p class="text-xs text-gray-500 dark:text-gray-400">居住地址</p>
                                                        <i class="fa-solid fa-edit text-primary text-xs opacity-60"></i>
                                                    </div>
                                                    <p v-if="!isEditMode"
                                                        class="font-medium text-title dark:text-white">
                                                        {{ memberData?.address || '尚未設定' }}
                                                    </p>
                                                    <input v-else v-model="editForm.address"
                                                        class="w-full mt-1 px-2 py-1 bg-gray-50 dark:bg-dark-secondary border border-gray-200 dark:border-gray-600 text-title dark:text-white focus:border-primary outline-none rounded text-sm"
                                                        type="text" placeholder="請輸入居住地址" />
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- [重點] 個人基本資料 -->
                                    <div class="space-y-4">
                                        <h4
                                            class="text-lg font-semibold text-title dark:text-white border-b border-primary pb-2">
                                            <i class="fa-solid fa-id-card text-primary mr-2"></i>
                                            個人資料
                                        </h4>
                                        <div class="space-y-3">
                                            <!-- [重點] 性別 - 可編輯 -->
                                            <div class="flex items-center gap-3 p-3 bg-white dark:bg-title rounded-lg">
                                                <div
                                                    class="w-8 h-8 bg-primary/10 rounded-full flex items-center justify-center flex-none">
                                                    <i class="fa-solid fa-venus-mars text-primary text-sm"></i>
                                                </div>
                                                <div class="flex-1">
                                                    <div class="flex items-center gap-2">
                                                        <p class="text-xs text-gray-500 dark:text-gray-400">性別</p>
                                                        <i class="fa-solid fa-edit text-primary text-xs opacity-60"></i>
                                                    </div>
                                                    <!-- [重點] 修復性別顯示 -->
                                                    <p v-if="!isEditMode"
                                                        class="font-medium text-title dark:text-white">
                                                        {{ memberData?.gender || '尚未設定' }}
                                                    </p>
                                                    <select v-else v-model="editForm.gender"
                                                        class="w-full mt-1 px-2 py-1 bg-gray-50 dark:bg-dark-secondary border border-gray-200 dark:border-gray-600 text-title dark:text-white focus:border-primary outline-none rounded text-sm">
                                                        <option value="">請選擇性別</option>
                                                        <option value="true">男性</option>
                                                        <option value="false">女性</option>
                                                    </select>
                                                </div>
                                            </div>

                                            <!-- [重點] 生日 - 可編輯 -->
                                            <div class="flex items-center gap-3 p-3 bg-white dark:bg-title rounded-lg">
                                                <div
                                                    class="w-8 h-8 bg-primary/10 rounded-full flex items-center justify-center flex-none">
                                                    <i class="fa-solid fa-birthday-cake text-primary text-sm"></i>
                                                </div>
                                                <div class="flex-1">
                                                    <div class="flex items-center gap-2">
                                                        <p class="text-xs text-gray-500 dark:text-gray-400">生日</p>
                                                        <i class="fa-solid fa-edit text-primary text-xs opacity-60"></i>
                                                    </div>
                                                    <p v-if="!isEditMode"
                                                        class="font-medium text-title dark:text-white">
                                                        {{ memberData?.birthday || '尚未設定' }}
                                                    </p>
                                                    <input v-else v-model="editForm.birthday"
                                                        class="w-full mt-1 px-2 py-1 bg-gray-50 dark:bg-dark-secondary border border-gray-200 dark:border-gray-600 text-title dark:text-white focus:border-primary outline-none rounded text-sm"
                                                        type="date" />
                                                </div>
                                            </div>

                                            <!-- [重點] 大頭貼 - 不可編輯 -->
                                            <div
                                                class="flex items-center gap-3 p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                                                <div
                                                    class="w-8 h-8 bg-gray-200 dark:bg-gray-600 rounded-full flex items-center justify-center flex-none">
                                                    <i class="fab fa-line text-green-600 text-sm"></i>
                                                </div>
                                                <div class="flex-1">
                                                    <div class="flex items-center gap-2">
                                                        <p class="text-xs text-gray-500 dark:text-gray-400">大頭貼</p>
                                                        <i class="fa-solid fa-lock text-gray-400 text-xs"></i>
                                                    </div>
                                                    <p class="font-medium text-gray-600 dark:text-gray-300 text-sm">
                                                        由 Line 帳號自動同步
                                                    </p>
                                                </div>
                                            </div>
                                        </div>
                                    </div>

                                    <!-- [重點] 帳戶資訊 - 不可編輯 -->
                                    <div class="space-y-4 md:col-span-2">
                                        <h4
                                            class="text-lg font-semibold text-title dark:text-white border-b border-primary pb-2">
                                            <i class="fa-solid fa-clock text-primary mr-2"></i>
                                            帳戶資訊
                                        </h4>
                                        <div class="grid grid-cols-1 sm:grid-cols-3 gap-3">
                                            <!-- [重點] 註冊日期 - 不可編輯 -->
                                            <div
                                                class="flex items-center gap-3 p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                                                <div
                                                    class="w-8 h-8 bg-green-100 dark:bg-green-900/30 rounded-full flex items-center justify-center flex-none">
                                                    <i
                                                        class="fa-solid fa-calendar-plus text-green-600 dark:text-green-400 text-sm"></i>
                                                </div>
                                                <div class="flex-1">
                                                    <div class="flex items-center gap-2">
                                                        <p class="text-xs text-gray-500 dark:text-gray-400">註冊日期</p>
                                                        <i class="fa-solid fa-lock text-gray-400 text-xs"></i>
                                                    </div>
                                                    <p class="font-medium text-gray-600 dark:text-gray-300 text-sm">
                                                        {{ memberData?.createdAt || '資料載入中...' }}
                                                    </p>
                                                </div>
                                            </div>

                                            <!-- [重點] 最後更新 - 不可編輯 -->
                                            <div
                                                class="flex items-center gap-3 p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                                                <div
                                                    class="w-8 h-8 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center flex-none">
                                                    <i
                                                        class="fa-solid fa-calendar-check text-blue-600 dark:text-blue-400 text-sm"></i>
                                                </div>
                                                <div class="flex-1">
                                                    <div class="flex items-center gap-2">
                                                        <p class="text-xs text-gray-500 dark:text-gray-400">最後更新</p>
                                                        <i class="fa-solid fa-lock text-gray-400 text-xs"></i>
                                                    </div>
                                                    <p class="font-medium text-gray-600 dark:text-gray-300 text-sm">
                                                        {{ memberData?.updatedAt || '資料載入中...' }}
                                                    </p>
                                                </div>
                                            </div>

                                            <!-- [重點] 最後登入時間 - 新增的欄位 -->
                                            <div
                                                class="flex items-center gap-3 p-3 bg-gray-50 dark:bg-gray-700/50 rounded-lg">
                                                <div
                                                    class="w-8 h-8 bg-orange-100 dark:bg-orange-900/30 rounded-full flex items-center justify-center flex-none">
                                                    <i
                                                        class="fa-solid fa-sign-in-alt text-orange-600 dark:text-orange-400 text-sm"></i>
                                                </div>
                                                <div class="flex-1">
                                                    <div class="flex items-center gap-2">
                                                        <p class="text-xs text-gray-500 dark:text-gray-400">最後登入</p>
                                                        <i class="fa-solid fa-lock text-gray-400 text-xs"></i>
                                                    </div>
                                                    <p class="font-medium text-gray-600 dark:text-gray-300 text-sm">
                                                        {{ memberData?.loginAt || '資料載入中...' }}
                                                    </p>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </AuthPrompt>

        <FooterThree />
        <ScrollToTop />
    </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import MainNavbar from '@/components/navbar/main-navbar.vue'
import AuthPrompt from '@/components/auth/AuthPrompt.vue';
import ProfileTab from '@/components/profile-tab.vue';
import FooterThree from '@/components/footer/footer-three.vue';
import ScrollToTop from '@/components/scroll-to-top.vue';

// 引入認證相關的 Composables
import { useAuth } from '@/composables/useAuth';
import { useToast } from '@/composables/useToast';

// 引入會員服務
import { memberService } from '@/service/memberService';

import bg from '@/assets/img/shortcode/breadcumb.jpg'
import Aos from 'aos';

// 取得認證相關功能
const { currentUser } = useAuth();
const { showToast } = useToast();

// [重點] 編輯模式狀態管理
const isEditMode = ref(false);
const memberData = ref(null);
const isSaving = ref(false);
const isLoading = ref(false);
const originalData = ref(null); // [重點] 保存原始資料用於取消編輯

// [重點] 編輯表單資料
const editForm = ref({
    memberName: '',
    mainPhone: '',
    gender: '',
    birthday: '',
    address: '',
    email: ''
});

/**
 * [重點] 切換編輯模式
 */
function toggleEditMode() {
    isEditMode.value = true;
    // [重點] 保存原始資料
    originalData.value = { ...memberData.value };
    // [重點] 填充編輯表單
    fillEditForm();
}

/**
 * [重點] 取消編輯，還原原始資料
 */
function cancelEdit() {
    if (originalData.value) {
        memberData.value = { ...originalData.value };
        fillEditForm();
    }
    isEditMode.value = false;
}

/**
 * [重點] 填充編輯表單資料
 */
function fillEditForm() {
    if (memberData.value) {
        editForm.value = {
            memberName: memberData.value.memberName || '',
            mainPhone: memberData.value.mainPhone || '',
            // [重點] 修復性別轉換邏輯
            gender: memberData.value.gender === '男' ? 'true' : 
                   memberData.value.gender === '女' ? 'false' : '',
            // [重點] 使用 memberService 的日期轉換函數
            birthday: memberService.convertDateForInput(memberData.value.birthdayRaw || memberData.value.birthday),
            address: memberData.value.address || '',
            // [重點] 修復 email 填充
            email: memberData.value.email || ''
        };

        // [重點] 除錯用：印出轉換後的表單資料
        console.log('填充編輯表單資料:', editForm.value);
        console.log('原始會員資料:', memberData.value);
    }
}

/**
 * [重點] 載入會員資料
 */
async function loadMemberData() {
    try {
        isLoading.value = true;

        // [重點] 從 memberService 取得會員資料
        const data = await memberService.getCurrentMember();
        memberData.value = data;

        // [重點] 填充編輯表單
        fillEditForm();

    } catch (error) {
        showToast('載入會員資料失敗，請重新整理頁面', 'error');
    } finally {
        isLoading.value = false;
    }
}

/**
 * [重點] 儲存會員資料
 */
async function handleSaveProfile() {
    try {
        isSaving.value = true;

        // [重點] 驗證必填欄位
        if (!editForm.value.memberName.trim()) {
            showToast('請輸入會員姓名', 'warning');
            return;
        }

        // [重點] 驗證日期格式（如果有填寫的話）
        if (editForm.value.birthday && !isValidDate(editForm.value.birthday)) {
            showToast('請輸入正確的生日格式', 'warning');
            return;
        }

        // [重點] 準備更新資料
        const updateData = {
            memberName: editForm.value.memberName.trim(),
            mainPhone: editForm.value.mainPhone.trim(),
            gender: editForm.value.gender,
            birthday: editForm.value.birthday, // [重點] 直接使用 YYYY-MM-DD 格式
            address: editForm.value.address.trim(),
            email: editForm.value.email.trim()
        };


        // [重點] 呼叫 memberService 更新資料
        const updatedData = await memberService.updateCurrentMember(updateData);

        // [重點] 更新本地狀態
        memberData.value = updatedData;

        // [重點] 關閉編輯模式
        isEditMode.value = false;

        showToast({
            title: '更新成功',
            message: '成功修改個人資料',
            type: 'success'
        });

    } catch (error) {
        showToast({
            title: '更新失敗',
            message: '發生錯誤稍後在試',
            type: 'error'
        });
    } finally {
        isSaving.value = false;
    }
}

/**
 * [重點] 驗證日期格式是否正確
 * @param {string} dateString - 日期字串 (YYYY-MM-DD)
 * @returns {boolean} 是否為有效日期
 */
function isValidDate(dateString) {
    if (!dateString) return true; // [重點] 空值視為有效（可選欄位）

    // [重點] 檢查日期格式是否為 YYYY-MM-DD
    const dateRegex = /^\d{4}-\d{2}-\d{2}$/;
    if (!dateRegex.test(dateString)) {
        return false;
    }

    // [重點] 檢查日期是否真實存在
    const date = new Date(dateString);
    return !isNaN(date.getTime()) && dateString === date.toISOString().split('T')[0];
}

// [重點] 監聽 currentUser 的變化，自動載入會員資料
watch(currentUser, (newUser) => {
    if (newUser) {
        loadMemberData();
    }
}, { immediate: true });

// [重點] 頁面載入時的初始化
onMounted(() => {
    // [重點] 初始化 AOS 動畫
    Aos.init();
});
</script>

<style scoped>
/* [重點] 編輯模式的視覺回饋 */
.group:hover .fa-edit {
    opacity: 1 !important;
}

/* [重點] 輸入框樣式優化 */
input:focus,
select:focus {
    box-shadow: 0 0 0 2px rgba(var(--primary-rgb), 0.2);
}
</style>
