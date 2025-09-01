<template>
  <div class="row row-cols-xxxl-5 row-cols-lg-3 row-cols-sm-2 row-cols-1 gy-4">
    <!-- Total Users Card (動態) -->
    <div class="col">
      <div class="card shadow-none border bg-gradient-start-1 h-100">
        <div class="card-body p-20">
          <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
            <div>
              <p class="fw-medium text-primary-light mb-1">本月註冊會員人數</p>
              <!-- 重點：顯示格式化後的用戶總數 / 載入中文字 / 錯誤狀態 -->
              <h6 class="mb-0" :title="usersError || ''">
                {{ formattedTotalUsers }}
              </h6>
            </div>
            <div class="w-50-px h-50-px bg-cyan rounded-circle d-flex justify-content-center align-items-center">
              <iconify-icon icon="gridicons:multiple-users" class="text-white text-2xl mb-0"></iconify-icon>
            </div>
          </div>
          <p class="fw-medium text-sm text-primary-light mt-12 mb-0 d-flex align-items-center gap-2">
            <span class="d-inline-flex align-items-center gap-1"
                  :class="usersError ? 'text-danger-main' : 'text-success-main'">
              <iconify-icon :icon="usersError ? 'bxs:error' : 'bxs:up-arrow'" class="text-xs"></iconify-icon>
              <!-- 這裡僅示意，可改成實際增長率 -->
              {{ usersError ? '取得失敗' : '+3' }}
            </span>
            過去 7 天成長
          </p>
        </div>
      </div>
    </div>

    <!-- Total Subscription Card -->
    <div class="col">
      <div class="card shadow-none border bg-gradient-start-2 h-100">
        <div class="card-body p-20">
          <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
            <div>
              <p class="fw-medium text-primary-light mb-1">當前巴士數量</p>
              <h6 class="mb-0">15</h6>
            </div>
            <div class="w-50-px h-50-px bg-purple rounded-circle d-flex justify-content-center align-items-center">
              <iconify-icon icon="fa-solid:award" class="text-white text-2xl mb-0"></iconify-icon>
            </div>
          </div>
          <p class="fw-medium text-sm text-primary-light mt-12 mb-0 d-flex align-items-center gap-2">
            <span class="d-inline-flex align-items-center gap-1 text-danger-main">
              <iconify-icon icon="bxs:down-arrow" class="text-xs"></iconify-icon> -2
            </span>
            本月停駛數量
          </p>
        </div>
      </div><!-- card end -->
    </div>

        <!-- Total Expense Card -->
    <div class="col">
      <div class="card shadow-none border bg-gradient-start-5 h-100">
        <div class="card-body p-20">
          <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
            <div>
              <p class="fw-medium text-primary-light mb-1">照服員人數</p>
              <h6 class="mb-0">32</h6>
            </div>
            <div class="w-50-px h-50-px bg-red rounded-circle d-flex justify-content-center align-items-center">
              <iconify-icon icon="fa6-solid:file-invoice-dollar" class="text-white text-2xl mb-0"></iconify-icon>
            </div>
          </div>
          <p class="fw-medium text-sm text-primary-light mt-12 mb-0 d-flex align-items-center gap-2">
            <span class="d-inline-flex align-items-center gap-1 text-success-main">
              <iconify-icon icon="bxs:up-arrow" class="text-xs"></iconify-icon> +3
            </span>
            過去一個月成長人數
          </p>
        </div>
      </div><!-- card end -->
    </div>
        <div class="col">
      <div class="card shadow-none border bg-gradient-start-5 h-100">
        <div class="card-body p-20">
          <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
            <div>
              <p class="fw-medium text-primary-light mb-1">本月輔具銷售額</p>
              <h6 class="mb-0">$18750</h6>
            </div>
            <div class="w-50-px h-50-px bg-red rounded-circle d-flex justify-content-center align-items-center">
              <iconify-icon icon="fa6-solid:file-invoice-dollar" class="text-white text-2xl mb-0"></iconify-icon>
            </div>
          </div>
          <p class="fw-medium text-sm text-primary-light mt-12 mb-0 d-flex align-items-center gap-2">
            <span class="d-inline-flex align-items-center gap-1 text-success-main">
              <iconify-icon icon="bxs:up-arrow" class="text-xs"></iconify-icon> +$3025
            </span>
            較前一個月成長
          </p>
        </div>
      </div><!-- card end -->
    </div>

    <!-- 房型預約參觀人數 -->
    <div class="col">
      <div class="card shadow-none border bg-gradient-start-3 h-100">
        <div class="card-body p-20">
          <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
            <div>
              <p class="fw-medium text-primary-light mb-1">設施預約參觀人數</p>
              <h6 class="mb-0">18</h6>
            </div>
            <div class="w-50-px h-50-px bg-info rounded-circle d-flex justify-content-center align-items-center">
              <iconify-icon icon="fluent:people-20-filled" class="text-white text-2xl mb-0"></iconify-icon>
            </div>
          </div>
          <p class="fw-medium text-sm text-primary-light mt-12 mb-0 d-flex align-items-center gap-2">
            <span class="d-inline-flex align-items-center gap-1 text-success-main">
              <iconify-icon icon="bxs:up-arrow" class="text-xs"></iconify-icon> +9
            </span>
            過去7天成長
          </p>
        </div>
      </div><!-- card end -->
    </div>

    <!-- Total Income Card -->
    <div class="col">
      <div class="card shadow-none border bg-gradient-start-4 h-100">
        <div class="card-body p-20">
          <div class="d-flex flex-wrap align-items-center justify-content-between gap-3">
            <div>
              <p class="fw-medium text-primary-light mb-1">本月活動報名人數</p>
              <h6 class="mb-0">42</h6>
            </div>
            <div
              class="w-50-px h-50-px bg-success-main rounded-circle d-flex justify-content-center align-items-center">
              <iconify-icon icon="solar:wallet-bold" class="text-white text-2xl mb-0"></iconify-icon>
            </div>
          </div>
          <p class="fw-medium text-sm text-primary-light mt-12 mb-0 d-flex align-items-center gap-2">
            <span class="d-inline-flex align-items-center gap-1 text-success-main">
              <iconify-icon icon="bxs:up-arrow" class="text-xs"></iconify-icon> +15
            </span>
            過去30天成長
          </p>
        </div>
      </div><!-- card end -->
    </div>
  </div>
</template>

<script setup>
// 重點：引入 Vue & memberService
import { ref, computed, onMounted } from 'vue'
import { memberService } from '@/services/memberService'

// ========== 狀態 ==========
const totalUsers = ref(0)            // 總會員數
const loadingUsers = ref(false)      // 載入狀態
const usersError = ref('')           // 錯誤訊息

// ========== 動作：取得會員總數 ==========
const fetchTotalUsers = async () => {
  loadingUsers.value = true
  usersError.value = ''
  try {
    // 呼叫 service (會再呼叫 API) 取得所有會員
    const members = await memberService.getAllMembers()
    totalUsers.value = members.length
  } catch (e) {
    usersError.value = e.message || '取得會員數失敗'
    totalUsers.value = 0
  } finally {
    loadingUsers.value = false
  }
}

// ========== 衍生資料：顯示用 ==========
const formattedTotalUsers = computed(() => {
  if (loadingUsers.value) return '載入中...'
  if (usersError.value) return '--'
  return totalUsers.value.toLocaleString('zh-TW')
})

// ========== 生命週期 ==========
onMounted(() => {
  fetchTotalUsers() // 進入頁面即取得最新會員數
})
</script>