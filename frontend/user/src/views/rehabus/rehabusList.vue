<template>
  <div>
    <MainNavbar />
                <div style="height: 140px;"></div>

    <!-- 頁首 -->
    <div class="flex items-center gap-4 flex-wrap bg-overlay p-16 md:p-20 before:bg-title before:bg-opacity-70"
      :style="{ backgroundImage: 'url(' + bg + ')' }">
      <div class="text-center w-full">
        <h2 class="text-white text-4xl md:text-5xl font-semibold leading-tight">復康巴士</h2>
        <ul class="flex items-center justify-center gap-3 text-lg md:text-xl leading-none font-normal text-white mt-4">
          <li><router-link to="/">首頁</router-link></li>
          <li>/</li>
          <li class="text-primary">巴士列表</li>
        </ul>
      </div>
    </div>

    <!-- 內容 -->
    <div class="s-py-100">
      <div class="container">
        <div class="max-w-[1477px] mx-auto flex items-start justify-between gap-10 lg:gap-12 flex-col lg:flex-row">
          <!-- 左側：搜尋條件 -->
          <aside class="grid gap-5 lg:max-w-[320px] w-full sm:grid-cols-2 lg:grid-cols-1" data-aos="fade-up">
            <!-- 座位數量 -->
            <div class="bg-[#F8F8F9] dark:bg-dark-secondary p-6 md:p-8 rounded-2xl">
              <h4 class="font-semibold leading-none text-2xl md:text-3xl mb-6">座位數量</h4>

              <div class="grid gap-5">
                <!-- 一般座位（最少） -->
                <div class="relative">
                  <svg class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500" width="20" height="20"
                    viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path
                      d="M21 21L16.65 16.65M11 18C14.866 18 18 14.866 18 11C18 7.13401 14.866 4 11 4C7.13401 4 4 7.13401 4 11C4 14.866 7.13401 18 11 18Z"
                      stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                  </svg>
                  <input type="number" min="0" step="1" v-model.number="q.seat" @keyup.enter="doSearch"
                    placeholder="一般座位（最少）"
                    class="w-full pl-12 pr-4 py-3.5 rounded-xl border border-title/30 dark:border-white/20 bg-white dark:bg-dark text-base md:text-lg text-title dark:text-white placeholder-[#9CA3AF] focus:outline-none focus:ring-2 focus:ring-[#BB976D]" />
                </div>

                <!-- 輪椅座位（最少） -->
                <div class="relative">
                  <svg class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500" width="20" height="20"
                    viewBox="0 0 24 24" fill="none" aria-hidden="true">
                    <path
                      d="M21 21L16.65 16.65M11 18C14.866 18 18 14.866 18 11C18 7.13401 14.866 4 11 4C7.13401 4 4 7.13401 4 11C4 14.866 7.13401 18 11 18Z"
                      stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
                  </svg>
                  <input type="number" min="0" step="1" v-model.number="q.wheel" @keyup.enter="doSearch"
                    placeholder="輪椅座位（最少）"
                    class="w-full pl-12 pr-4 py-3.5 rounded-xl border border-title/30 dark:border-white/20 bg-white dark:bg-dark text-base md:text-lg text-title dark:text-white placeholder-[#9CA3AF] focus:outline-none focus:ring-2 focus:ring-[#BB976D]" />
                </div>

                <!-- 動作 -->
                <div class="flex gap-3 pt-2">
                  <button class="px-5 py-3 rounded-xl bg-[#BB976D] text-white text-base md:text-lg"
                    @click="doSearch">搜尋</button>
                  <button class="px-5 py-3 rounded-xl border text-base md:text-lg" @click="clearSearch">清除</button>
                </div>
              </div>
            </div>

            <!-- 訂單查詢 -->
            <div class="bg-[#F8F8F9] dark:bg-dark-secondary p-6 md:p-8 rounded-2xl">
              <h4 class="font-semibold leading-none text-2xl md:text-3xl mb-6">訂單查詢</h4>

              <div class="grid gap-5">
                <!-- 隱藏：只看我的（功能保留、UI 隱藏） -->
                <input type="checkbox" v-model="rq.onlyMine" class="hidden" aria-hidden="true" />

                <!-- 日期（查 scheduled_at），單一天 -->
                <div>
                  <label class="block mb-2 text-gray-600 dark:text-gray-300 text-lg">日期</label>
                  <input type="date" v-model="rq.fromDate" class="w-full input text-lg" />
                </div>

                <!-- 動作 -->
                <div class="flex gap-3 pt-2">
                  <button class="px-5 py-3 rounded-xl bg-[#BB976D] text-white text-base md:text-lg disabled:opacity-60"
                    :disabled="rq.loading" @click="queryReservations">
                    <i class="mdi mdi-magnify"></i> 查詢
                  </button>
                  <button class="px-5 py-3 rounded-xl border text-base md:text-lg" @click="clearReservationQuery">
                    清除
                  </button>
                </div>

                <p v-if="rq.err" class="text-red-500 text-sm">{{ rq.err }}</p>
              </div>
            </div>
          </aside>

          <!-- 右側網格 -->
          <section class="lg:max-w-[1100px] w-full" data-aos="fade-up" data-aos-delay="200">
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-2 xl:grid-cols-3 gap-6 lg:gap-8">
              <article v-for="bus in pagedBuses" :key="bus.id" class="group">
                <div class="relative overflow-hidden rounded-2xl bg-white shadow-sm">
                  <!-- [重點] 維修中的巴士顯示不同樣式，移除連結 -->
                  <div v-if="isBusMaintenance(bus)" class="bus-frame block bg-gray-100 flex items-center justify-center relative">
                    <img class="bus-img opacity-30" :src="bus.image" :alt="bus.title" loading="lazy" />
                    <!-- [重點] 維修中標籤覆蓋層 -->
                    <div class="absolute inset-0 bg-black/20 flex items-center justify-center">
                      <div class="bg-red-500 text-white px-4 py-2 rounded-lg font-bold text-lg shadow-lg">
                        <i class="mdi mdi-wrench mr-2"></i>維修中
                      </div>
                    </div>
                  </div>
                  
                  <!-- [重點] 正常巴士保持原有功能 -->
                  <router-link v-else class="bus-frame block"
                    :to="{ name: 'bus-detail', params: { id: bus.id }, state: { bus } }">
                    <img class="bus-img" :src="bus.image" :alt="bus.title" loading="lazy" />
                  </router-link>

                  <!-- [重點] 只有非維修中的巴士才顯示快捷按鈕 -->
                  <div v-if="!isBusMaintenance(bus)"
                    class="absolute z-10 top-1/2 right-3 -translate-y-1/2 opacity-0 transition-all duration-300 group-hover:opacity-100 hidden md:flex flex-col items-end gap-3">
                    <button class="icon-pill" @click="viewDetail(bus)">
                      <i class="mdi mdi-eye-outline text-[22px]"></i>
                      <span class="mt-[2px]">查看</span>
                    </button>
                    <button class="icon-pill" @click="openReserve(bus)">
                      <i class="mdi mdi-calendar-plus text-[22px]"></i>
                      <span class="mt-[2px]">預約</span>
                    </button>
                  </div>
                </div>

                <div class="px-5 pt-4 pb-3 flex flex-col gap-2">
                  <h4 class="font-semibold leading-snug dark:text-white text-2xl">
                    {{ bus.title }}
                    <!-- [重點] 標題旁也加上維修中標記 -->
                    <span v-if="isBusMaintenance(bus)" class="ml-2 text-red-500 text-base">
                      (維修中)
                    </span>
                  </h4>
                  <p class="font-normal dark:text-white text-xl md:text-2xl leading-[1.6]"
                     :class="{ 'text-gray-400': isBusMaintenance(bus) }">
                    一般座位 {{ bus.meta.seat }} <span class="mx-2">|</span> 輪椅座位 {{ bus.meta.wheel }}
                  </p>
                </div>
              </article>
            </div>

            <!-- 分頁 -->
            <div class="mt-12 md:mt-14 flex items-center justify-center gap-3 text-lg md:text-xl">
              <button class="px-5 py-3 border rounded-xl disabled:opacity-40" :disabled="currentPage === 1"
                @click="prevPage">上一頁</button>
              <span class="px-4">第 {{ currentPage }} / {{ totalPages }} 頁</span>
              <button class="px-5 py-3 border rounded-xl disabled:opacity-40" :disabled="currentPage === totalPages"
                @click="nextPage">下一頁</button>
            </div>
          </section>
        </div>
      </div>
    </div>

    <!-- 查看 Modal -->
    <transition name="fade" v-if="showModal">
      <div class="fixed inset-0 z-[200] flex items-center justify-center" @keydown.esc="closeModal">
        <div class="absolute inset-0 bg-black/50 backdrop-blur-[2px]" @click="closeModal"></div>

        <div class="relative mx-4 w-full max-w-[80vw] bg-white dark:bg-dark rounded-2xl shadow-2xl overflow-hidden"
          role="dialog" aria-modal="true" tabindex="-1">
          <!-- 圖 -->
          <div class="px-8 pt-6">
            <div class="w-[90%] md:w-3/5 mx-auto">
              <div class="w-full bg-white rounded-xl border border-black/5 dark:border-white/10">
                <img :src="selectedBus ? selectedBus.image : ''" :alt="selectedBus ? selectedBus.title : ''"
                  class="w-full max-h-[52vh] object-contain" />
              </div>
            </div>
          </div>

          <!-- 文字 -->
          <div class="px-8 mt-6 text-center">
            <h3 class="text-3xl md:text-4xl font-bold dark:text-white leading-tight">
              {{ selectedBus ? selectedBus.meta.brand : '' }} <span class="mx-2"></span> {{ selectedBus ?
                selectedBus.meta.model : '' }}
            </h3>

            <p class="mt-3 text-2xl md:text-3xl font-semibold text-gray-800 dark:text-gray-100">
              一般座位 {{ selectedBus ? selectedBus.meta.seat : '' }} <span class="mx-2">|</span> 輪椅座位 {{ selectedBus ?
                selectedBus.meta.wheel : '' }}
            </p>

            <p class="mt-2 text-xl md:text-2xl font-semibold text-[#374151] dark:text-white">
              巴士編號 {{ selectedBus ? selectedBus.id : '' }} <span class="mx-2">|</span> 車牌：{{ selectedBus &&
                selectedBus.meta.licensePlate ? selectedBus.meta.licensePlate : '—' }}
            </p>
          </div>

          <!-- 規格 -->
          <div class="px-8 mt-6">
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 max-w-3xl mx-auto">
              <SpecRow label="車行" :value="selectedBus ? selectedBus.meta.dealership : ''" />
              <SpecRow label="廠牌" :value="selectedBus ? selectedBus.meta.brand : ''" />
              <SpecRow label="型號" :value="selectedBus ? selectedBus.meta.model : ''" />
              <SpecRow label="一般座位" :value="selectedBus ? selectedBus.meta.seat : ''" />
              <SpecRow label="輪椅座位" :value="selectedBus ? selectedBus.meta.wheel : ''" />
              <SpecRow label="車牌號碼"
                :value="selectedBus && selectedBus.meta.licensePlate ? selectedBus.meta.licensePlate : ''" />
              <SpecRow label="車輛編號" :value="selectedBus ? selectedBus.id : ''" />
            </div>
          </div>

          <!-- 底部 -->
          <div class="px-8 pb-6 mt-8">
            <div class="flex flex-col sm:flex-row gap-4 justify-center">
              <button
                class="inline-flex items-center justify-center gap-2 px-6 py-3 rounded-xl border hover:bg-black/5 dark:hover:bg-white/10 transition text-lg"
                @click="closeModal">
                <i class="mdi mdi-arrow-left"></i> 返回
              </button>
              <button
                class="inline-flex items-center justify-center gap-2 px-8 py-4 rounded-xl bg-[#BB976D] text-white hover:opacity-95 transition text-lg"
                @click="reserveFromDetail">
                <i class="mdi mdi-calendar-month"></i> 預約
              </button>
            </div>
          </div>

          <!-- 關閉 -->
          <button
            class="absolute top-3 right-3 h-10 w-10 inline-flex items-center justify-center rounded-full hover:bg-black/5 dark:hover:bg-white/10 transition"
            @click="closeModal" aria-label="關閉">
            <i class="mdi mdi-close text-xl"></i>
          </button>
        </div>
      </div>
    </transition>

    <!-- 中央大型：訂單查詢結果 Modal -->
    <transition name="fade" v-if="showResModal">
      <div class="fixed inset-0 z-[205] flex items-center justify-center"
        @keydown.esc="closeResModal">
        <div class="absolute inset-0 bg黑/50 backdrop-blur-[2px]" @click="closeResModal"></div>

        <div
          class="relative mx-4 w-full max-w-[1000px] md:max-w-[1100px] bg-white dark:bg-dark rounded-2xl shadow-2xl overflow-hidden">
          <div class="px-8 py-7">
            <h3 class="text-3xl md:text-4xl font-bold dark:text-white">訂單查詢結果</h3>
            <p class="mt-2 text-xl md:text-2xl text-gray-600 dark:text-gray-300">
              共 <span class="font-semibold">{{ rq.list.length }}</span> 筆
            </p>

            <div class="mt-5 max-h-[62vh] overflow-auto pr-1">
              <ul v-if="rq.list.length" class="space-y-4">
                <li v-for="r in rq.list" :key="r.id"
                  class="p-5 rounded-xl bg-black/5 dark:bg白/5 border border黑/10 dark:border白/10">
                  <div class="text-2xl md:text-3xl font-semibold dark:text-white">
                    {{ formatDateTime(r.scheduled_at || r.SCHEDULED_AT) }}
                    <span class="mx-3">·</span>
                    車輛 {{ r.bus_id || r.BUS_ID }}
                  </div>
                  <div class="mt-1 text-xl md:text-2xl text-gray-800 dark:text-gray-100">
                    {{ (r.start_address || r.START_ADDRESS) || '—' }} → {{ (r.end_address || r.END_ADDRESS) || '—' }}
                  </div>

                  <div class="mt-1 text-lg md:text-xl text-gray-500">
                    狀態：{{ getStatusText(r.reservation_status || r.RESERVATION_STATUS) }}，金額：{{ (r.price || r.PRICE) ??
                      '—' }}
                  </div>
                  <div v-if="normalizeStatus(r.reservation_status || r.RESERVATION_STATUS) === 'completed'"
                    class="mt-1 text-lg md:text-xl text-gray-500">
                    完成時間：{{ formatDateTime(r.completed_at || r.COMPLETED_AT) }}
                  </div>
                    <!-- 新增：去評論按鈕，呼叫 goComment 方法 -->
                    <button class="btn-secondary mt-2" @click="goComment(r.bus_id || r.BUS_ID)">去評論</button>
                </li>
              </ul>
              <p v-else class="text-center text-xl md:text-2xl py-10 text-gray-500">沒有符合的預約。</p>
            </div>

            <div class="mt-6 text-right">
              <button class="btn-outline text-lg px-6 py-3" @click="closeResModal">
                <i class="mdi mdi-close"></i> 關閉
              </button>
            </div>
          </div>

          <button
            class="absolute top-3 right-3 h-10 w-10 inline-flex items-center justify-center rounded-full hover:bg黑/5 dark:hover:bg白/10 transition"
            @click="closeResModal" aria-label="關閉">
            <i class="mdi mdi-close text-xl"></i>
          </button>
        </div>
      </div>
    </transition>

    <!-- 預約表單 Modal -->
    <transition name="fade" v-if="showReserve">
      <div class="fixed inset-0 z-[210] flex items-center justify-center"
        @keydown.esc="closeReserve">
        <div class="absolute inset-0 bg-black/50 backdrop-blur-[2px]" @click="closeReserve"></div>

        <div
          class="relative mx-4 w-full max-w-[80vw] bg-white dark:bg-dark rounded-2xl shadow-2xl overflow-hidden reserve-modal"
          role="dialog" aria-modal="true" tabindex="-1">

          <div class="px-8 py-8">
            <div class="grid grid-cols-1 gap-6">
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
                <div>
                  <label class="block mb-2 text-gray-600 dark:text-gray-300">預約時間</label>
                  <input v-model="reserveForm.scheduledAt" type="datetime-local"
                    class="w-full input field-xl date-time-picker" />
                </div>
              </div>

              <div>
                <label class="block mb-2 text-gray-600 dark:text-gray-300">起點地址</label>
                <input v-model.trim="reserveForm.startAddress" type="text" class="w-full input text-lg"
                  placeholder="桃園市..." />
              </div>

              <div>
                <label class="block mb-2 text-gray-600 dark:text-gray-300">目的地地址</label>
                <input v-model.trim="reserveForm.endAddress" type="text" class="w-full input text-lg"
                  placeholder="新北市..." />
              </div>

              <div class="grid grid-cols-1 sm:grid-cols-3 gap-6 items-end">
                <div>
                  <label class="block mb-2 text-gray-600 dark:text-gray-300">估算距離</label>
                  <input :value="distanceKmText" type="text" class="w-full input text-lg" disabled />
                </div>
                <div>
                  <label class="block mb-2 text-gray-600 dark:text-gray-300">估算車資</label>
                  <input v-model="reserveForm.price" type="number" class="w-full input text-lg" />
                </div>
                <button class="btn-secondary h-[56px] text-lg" @click="doQuote" :disabled="pending">
                  <i class="mdi mdi-ruler"></i> 估算
                </button>
              </div>

              <div>
                <label class="block mb-2 text-gray-600 dark:text-gray-300">備註</label>
                <textarea v-model.trim="reserveForm.note" class="w-full input min-h-[100px] text-lg"
                  placeholder="（選填）"></textarea>
              </div>

              <p v-if="errorMsg" class="text-red-500 text-base">{{ errorMsg }}</p>
              <p v-if="okMsg" class="text-green-600 text-base">{{ okMsg }}</p>
            </div>
          </div>

          <div class="px-8 pb-8">
            <div class="flex flex-col-reverse sm:flex-row sm:items-center sm:justify-between gap-4">
              <button class="btn-secondary px-4 py-2 text-base sm:text-lg" @click="fillDemo">
                <i class="mdi mdi-flash"></i> 測試（DEMO）
              </button>

              <div class="flex items-center justify-end gap-4">
                <button class="btn-outline text-lg" @click="closeReserve"><i class="mdi mdi-close"></i> 取消</button>
                <button class="btn-primary text-lg px-7 py-4" :disabled="pending" @click="submitReserve">
                  <i class="mdi mdi-content-save"></i> 送出預約
                </button>
              </div>
            </div>
          </div>

          <button
            class="absolute top-3 right-3 h-10 w-10 inline-flex items-center justify-center rounded-full hover:bg-black/5 dark:hover:bg-white/10 transition"
            @click="closeReserve" aria-label="關閉">
            <i class="mdi mdi-close text-xl"></i>
          </button>
        </div>
      </div>
    </transition>

     <FooterThree />
    <ScrollToTop />
  </div>
</template>

<script setup>
import { onMounted, ref, computed, reactive, defineComponent } from "vue";
import MainNavbar from "@/components/navbar/main-navbar.vue";
import FooterThree from "@/components/footer/footer-three.vue";
import ScrollToTop from "@/components/scroll-to-top.vue";

import bg from "@/assets/img/shortcode/breadcumb.jpg";

import Aos from "aos";
import { busApi } from "@/api/rehabusApi";
import { reservationApi } from "@/api/busReservationApi";
import fallbackImg from "@/assets/img/rehabus/bus.jpg";

/* 後備：直呼後端 */
import axiosInstance from "@/api/axiosInstance.js";

/* 提示框 */
import { useConfirmDialog } from "@/composables/useConfirmDialog";
import { useRouter } from "vue-router";

// 獲取 router 實例
const router = useRouter();

const { showConfirmDialog, showSuccessDialog, showErrorDialog } = useConfirmDialog();

/* 初始 */
onMounted(async () => {
  Aos.init();
  await loadAll();
  // [重點] 移除會員資料獲取邏輯
  // 預設日期設為今天
  rq.fromDate = todayYMD();
});

/* 查詢狀態（座位） */
const q = reactive({ seat: null, wheel: null });

/* 圖片對應 */
function normalize(s) { return (s == null ? "" : String(s)).trim().replace(/\s+/g, " "); }
function keyOf(s) { return normalize(s).toLowerCase(); }
const ctx = require.context("@/assets/img/rehabus", false, /\.(jpe?g|png|webp)$/i);
const IMAGE_MAP = ctx.keys().reduce((acc, key) => {
  const filenameNoExt = key.replace(/^.\//, "").replace(/\.(jpe?g|png|webp)$/i, "");
  acc[keyOf(filenameNoExt)] = ctx(key);
  return acc;
}, {});
function resolveImage(brand, model, photoUrl) {
  if (photoUrl) return photoUrl;
  const k = keyOf(brand + " " + model);
  return IMAGE_MAP[k] || fallbackImg;
}

/* 列表 / 分頁 */
const rawBuses = ref([]);
const pageSize = ref(6);
const currentPage = ref(1);
const totalPages = computed(() => Math.max(1, Math.ceil((rawBuses.value.length || 0) / pageSize.value)));
const pagedBuses = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return rawBuses.value.slice(start, start + pageSize.value);
});

/**
 * [重點] 檢查巴士是否為維修中狀態
 * @param {Object} bus - 巴士物件
 * @returns {boolean} 是否維修中
 */
function isBusMaintenance(bus) {
  const status = (bus.status || bus.meta?.status || '').toLowerCase();
  return status === 'maintenance';
}

function busToCard(bus) {
  const id = bus.bus_id || bus.BUS_ID || bus.id;
  const brand = normalize(bus.bus_brand || bus.BUS_BRAND);
  const model = normalize(bus.bus_model || bus.BUS_MODEL);
  const seat = bus.seat_capacity || bus.SEAT_CAPACITY || 0;
  const wheel = bus.wheelchair_capacity || bus.WHEELCHAIR_CAPACITY || 0;
  const plate = bus.license_plate || bus.LICENSE_PLATE || "";
  const status = (bus.status || bus.STATUS || "").toLowerCase();
  const photo = bus.photo_url || bus.PHOTO_URL || "";
  const dealership = normalize(bus.car_dealership || bus.CAR_DEALERSHIP);
  
  return {
    id,
    title: "巴士編號 " + id,
    image: resolveImage(brand, model, photo),
    badges: status ? [status] : [],
    // [重點] 將 status 保存在最外層，方便檢查
    status: status,
    meta: { 
      licensePlate: plate, 
      brand, 
      model, 
      seat, 
      wheel, 
      dealership,
      status: status
    }
  };
}

async function loadAll() {
  try {
    const res = await busApi.getAllBuses();
    const rows = res && res.status === 200 ? res.data : [];
    rawBuses.value = rows.map(busToCard);
    currentPage.value = 1;
  } catch (e) {
    console.error("[rehabus] 全部載入失敗：", e);
    rawBuses.value = [];
  }
}

async function doSearch() {
  if ((q.seat == null || q.seat === "") && (q.wheel == null || q.wheel === "")) {
    await loadAll();
    return;
  }
  const params = {};
  if (Number.isFinite(q.seat) && q.seat >= 0) params.minSeats = q.seat;
  if (Number.isFinite(q.wheel) && q.wheel >= 0) params.minWheels = q.wheel;
  try {
    const res = await busApi.searchBuses(params);
    const rows = res && res.status === 200 ? res.data : [];
    rawBuses.value = rows.map(busToCard);
    currentPage.value = 1;
  } catch (e) {
    console.error("[rehabus] 搜尋失敗：", e);
    rawBuses.value = [];
  }
}

function clearSearch() {
  q.seat = null;
  q.wheel = null;
  loadAll();
}

/* ==== 訂單查詢（單一天） ==== */
const rq = reactive({
  // [重點] 移除 myMemberId 和 onlyMine 相關邏輯
  fromDate: "",   // yyyy-MM-dd
  loading: false,
  queried: false,
  err: "",
  list: []
});
const showResModal = ref(false);

function formatDateTime(x) {
  const val = (typeof x === "string") ? x : (x?.toString?.() ?? "");
  if (!val) return "—";
  return val.replace("T", " ").replaceAll("-", "/").slice(0, 16);
}

async function queryReservations() {
  rq.err = "";
  rq.queried = false;
  rq.loading = true;
  rq.list = [];

  if (!rq.fromDate) {
    rq.loading = false;
    rq.err = "請先選擇日期";
    return;
  }

  // [重點] 移除 memberId 參數，只傳遞日期
  const params = { scheduledDate: rq.fromDate };

  try {
    let res = null;
    if (reservationApi && typeof reservationApi.searchReservation === "function") {
      res = await reservationApi.searchReservation(params);
    } else {
      res = await axiosInstance.get("/reservation/search", { params });
    }

    rq.list = (!res || res.status === 204) ? [] : (Array.isArray(res.data) ? res.data : []);
    rq.queried = true;
    showResModal.value = true;
  } catch (e) {
    console.error("[reservation] 查詢失敗：", e);
    rq.err = "查詢失敗，請稍後再試。";
  } finally {
    rq.loading = false;
  }
}

function clearReservationQuery() {
  rq.fromDate = "";
  rq.err = "";
  rq.list = [];
  rq.queried = false;
}

function closeResModal() { showResModal.value = false; }

/* 查看 Modal */
const showModal = ref(false);
const selectedBus = ref(null);
function viewDetail(cardBus) {
  // [重點] 檢查巴士是否維修中
  if (isBusMaintenance(cardBus)) {
    showErrorDialog("該車輛目前維修中，暫停查看", "無法查看");
    return;
  }
  
  selectedBus.value = cardBus;
  showModal.value = true;
}
function closeModal() { showModal.value = false; selectedBus.value = null; }
function reserveFromDetail() {
  if (!selectedBus.value) return;
  
  // [重點] 檢查巴士是否維修中
  if (isBusMaintenance(selectedBus.value)) {
    showErrorDialog("該車輛目前維修中，暫停預約", "無法預約");
    return;
  }
  
  openReserve(selectedBus.value);
  closeModal();
}

/* 會員與預約表單 */
const showReserve = ref(false);
const reserveForm = reactive({
  // [重點] 移除 memberId 欄位
  busId: null,
  brand: "",
  model: "",
  scheduledAt: "",
  startAddress: "",
  endAddress: "",
  price: "",
  note: ""
});
const pending = ref(false);
const errorMsg = ref("");
const okMsg = ref("");
const quote = ref({ distanceMeters: 0, rehabusFare: 0 });

const distanceKmText = computed(() => {
  const km = (quote.value.distanceMeters || 0) / 1000;
  return km ? km.toFixed(2) + " km" : "";
});

async function openReserve(cardBus) {
  // [重點] 檢查巴士是否維修中
  if (isBusMaintenance(cardBus)) {
    await showErrorDialog("該車輛目前維修中，暫停預約", "無法預約");
    return;
  }
  
  errorMsg.value = "";
  okMsg.value = "";
  reserveForm.busId = cardBus.id;
  reserveForm.brand = cardBus.meta.brand;
  reserveForm.model = cardBus.meta.model;
  reserveForm.scheduledAt = "";
  reserveForm.startAddress = "";
  reserveForm.endAddress = "";
  reserveForm.price = "";
  reserveForm.note = "";
  quote.value = { distanceMeters: 0, rehabusFare: 0 };
  showReserve.value = true;
}

function closeReserve() { showReserve.value = false; }

async function doQuote() {
  try {
    pending.value = true;
    errorMsg.value = "";
    okMsg.value = "";
    const res = await reservationApi.calculateDistanceAndPrice(
      reserveForm.startAddress,
      reserveForm.endAddress
    );
    const data = res ? res.data : {};
    quote.value = {
      distanceMeters: data?.distanceMeters ? data.distanceMeters : 0,
      rehabusFare: data?.rehabusFare ? data.rehabusFare : 0
    };
    if (!reserveForm.price && data?.rehabusFare) reserveForm.price = data.rehabusFare;
  } catch {
    errorMsg.value = "估算失敗，請稍後再試。";
  } finally {
    pending.value = false;
  }
}

/**
 * [重點] 送出預約：移除 memberId 傳遞和認證檢查
 */
async function submitReserve() {
  try {
    const confirmed = await showConfirmDialog({
      title: "送出預約",
      message: "確認要送出這筆預約嗎？",
      type: "warning",
      confirmText: "送出",
      cancelText: "取消"
    });
    if (!confirmed) return;

    pending.value = true;
    errorMsg.value = "";
    okMsg.value = "";

    // [重點] 移除 memberId，由後端自動填入
    const payload = {
      busId: reserveForm.busId,
      scheduledAt: reserveForm.scheduledAt,
      startAddress: reserveForm.startAddress,
      endAddress: reserveForm.endAddress,
      note: reserveForm.note,
      price: reserveForm.price,
      distanceMeters: quote.value.distanceMeters
    };

    try {
      const res = await reservationApi.createReservation(payload);
      if (res && (res.status === 200 || res.status === 201)) {
        okMsg.value = "預約已建立！";
        await showSuccessDialog("預約已建立！", "成功");
        showReserve.value = false;
        return;
      }
      throw new Error("建立失敗");
    } catch (e) {
      // [重點] 車輛維修檢查
      const msg = e?.response?.data?.message || e.message || '';
      const isMaint = /維修中|maintenance/i.test(msg);
      if (e?.response?.status === 409 && isMaint) {
        const err = new Error('該車輛目前維修中，暫停預約');
        err.code = 'BUS_MAINTENANCE';
        throw err;
      }

      // [重點] 時段衝突檢查
      if (e && e.code === "RESERVATION_CONFLICT") {
        errorMsg.value = "該時段無法預約，請重新選擇時間或是車輛";
        await showErrorDialog(errorMsg.value, "錯誤");
        return;
      }
      throw e;
    }
  } catch (e) {
    if (e && e.code === "BUS_MAINTENANCE") {
      await showErrorDialog("該車輛目前維修中，暫停預約", "無法預約");
      return;
    }

    errorMsg.value = e?.message || "建立預約失敗，請確認必填欄位。";
    await showErrorDialog(errorMsg.value, "錯誤");
  } finally {
    pending.value = false;
  }
}

/* DEMO：一鍵填入 */
function toLocalDatetimeInput(d) {
  const pad = (n) => String(n).padStart(2, "0");
  return (
    d.getFullYear() + "-" + pad(d.getMonth() + 1) + "-" + pad(d.getDate()) +
    "T" + pad(d.getHours()) + ":" + pad(d.getMinutes())
  );
}

async function fillDemo() {
  // [重點] 移除 memberId 設定
  const twoHoursLater = new Date(Date.now() + 2 * 60 * 60 * 1000);
  reserveForm.scheduledAt = toLocalDatetimeInput(twoHoursLater);
  reserveForm.startAddress = "南崁國中";
  reserveForm.endAddress = "新北市板橋區中山路一段1號";
  reserveForm.note = "需要協助上下車";
  reserveForm.price = "";
  await doQuote();
}

/* 分頁 */
function prevPage() { if (currentPage.value > 1) currentPage.value--; }
function nextPage() { if (currentPage.value < totalPages.value) currentPage.value++; }

/* 小元件：規格列（彈窗用） */
const SpecRow = defineComponent({
  name: "SpecRow",
  props: { label: { type: String, required: true }, value: { type: [String, Number], default: "" } },
  template: `
    <div class="flex items-center justify-between gap-4 p-3.5 rounded-xl bg-black/3 dark:bg-white/5 border border-black/5 dark:border-white/10">
      <span class="text-base md:text-lg text-gray-600 dark:text-gray-300">{{ label }}</span>
      <span class="text-lg md:text-xl font-semibold dark:text白 break-all text-right">{{ value || '—' }}</span>
    </div>
  `
});

/* ===== 狀態對應（與後台一致） ===== */
function normalizeStatus(raw) {
  const s = String(raw || "").trim().toLowerCase();
  if (!s) return "pending";
  if (["pending", "in_progress", "completed", "cancelled"].includes(s)) return s;
  if (["active", "waiting"].includes(s)) return "pending";
  if (["processing"].includes(s)) return "in_progress";
  if (["done", "complete"].includes(s)) return "completed";
  if (["canceled", "cancel"].includes(s)) return "cancelled";
  return "pending";
}

function getStatusText(status) {
  switch (normalizeStatus(status)) {
    case "pending": return "待處理";
    case "in_progress": return "進行中";
    case "completed": return "已完成";
    case "cancelled": return "已取消";
    default: return "未知狀態";
  }
}

const goComment = function(busId) {
  router.push({
    path: '/rehabus/' + busId,
    hash: '#reviews'
  }).then(() => {
    // [重點] 給予渲染時間後再嘗試找元素
    setTimeout(() => {
      const el = document.getElementById('reviews');
      if (el) el.scrollIntoView({ behavior: 'smooth' });
    }, 100);
  });
};

function todayYMD() {
  const d = new Date();
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}
</script>

<style scoped>
/* 卡片圖片框 */
.bus-frame {
  width: 100%;
  aspect-ratio: 4/3;
  background: #fff;
  overflow: hidden;
  border-radius: 16px
}

.bus-img {
  width: 100%;
  height: 100%;
  object-fit: scale-down;
  object-position: center;
  display: block
}

.icon-pill {
  background: rgba(255, 255, 255, .95);
  color: #111827;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 12px 18px;
  font-size: 15px;
  line-height: 1;
  border-radius: 9999px;
  height: 56px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, .08);
  transition: transform .15s ease, box-shadow .15s ease
}

.icon-pill:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, .12)
}

.btn-primary {
  @apply inline-flex items-center gap-2 rounded-xl bg-[#BB976D] text-white hover:opacity-95 transition;
}

.btn-outline {
  @apply inline-flex items-center gap-2 px-5 py-2.5 rounded-xl border border-black/15 dark:border-white/15 hover:bg-black/5 dark:hover:bg-white/10 transition;
}

.btn-secondary {
  @apply inline-flex items-center gap-2 px-4 py-2 rounded-xl border border-black/15 dark:border-white/15 hover:bg-black/5 dark:hover:bg-white/10 transition;
}

.input {
  @apply px-3 py-3 rounded-lg border border-black/10 dark:border-white/10 bg-white dark:bg-slate-900 text-slate-900 dark:text-white focus:outline-none focus:ring-2 focus:ring-[#BB976D];
}

.field-xl {
  font-size: 1.45rem;
  padding: 1rem 1rem;
  height: 3.8rem;
  line-height: 1.75
}

.date-time-picker {
  --picker-scale: 1.7
}

.date-time-picker::-webkit-calendar-picker-indicator {
  transform: scale(var(--picker-scale));
  transform-origin: center
}

.date-time-picker::-webkit-datetime-edit,
.date-time-picker::-webkit-datetime-edit-text,
.date-time-picker::-webkit-datetime-edit-year-field,
.date-time-picker::-webkit-datetime-edit-month-field,
.date-time-picker::-webkit-datetime-edit-day-field,
.date-time-picker::-webkit-datetime-edit-hour-field,
.date-time-picker::-webkit-datetime-edit-minute-field,
.date-time-picker::-webkit-datetime-edit-ampm-field {
  font-size: 1.45rem;
  padding: 0 .2rem
}

.date-time-picker:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(187, 151, 109, .25);
  border-color: rgba(187, 151, 109, .6)
}

.reserve-modal :is(input, textarea)::placeholder {
  font-size: 1.125rem
}

.reserve-modal label {
  font-size: 1.1rem
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity .18s ease
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0
}

/* [重點] 維修中巴士的額外樣式 */
.bus-maintenance {
  filter: grayscale(50%);
  opacity: 0.7;
}

.bus-maintenance .bus-img {
  filter: grayscale(100%);
}

/* [重點] 維修標籤樣式 */
.maintenance-badge {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(239, 68, 68, 0.95);
  color: white;
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  z-index: 10;
}
</style>
