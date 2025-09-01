<template>
  <div class="card h-100 p-0 radius-12">
    <div class="card-body p-24">
      <div class="row justify-content-center">
        <div class="col-xxl-6 col-xl-8 col-lg-10">
          <div class="card border">
            <div class="card-body">
              <!-- 載入中顯示 -->
              <div v-if="loading" class="text-center py-5">
                <div class="spinner-border text-primary" role="status">
                  <span class="visually-hidden">載入中...</span>
                </div>
                <p class="mt-2">正在載入預約資料...</p>
              </div>
              
              <!-- 載入失敗顯示 -->
              <div v-else-if="error" class="alert alert-danger">
                載入預約資料失敗: {{ error }}
                <button class="btn btn-outline-danger btn-sm ms-2" @click="loadReservation">重試</button>
              </div>
              
              <!-- 編輯表單 -->
              <form v-else @submit.prevent="submitForm">
                <!-- 會員編號欄位 -->
                <div class="mb-20">
                  <label for="memberId" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    會員編號 <span class="text-danger-600">*</span>
                  </label>
                  <input
                    v-model.number="form.memberId"
                    type="number"
                    min="1"
                    class="form-control radius-8"
                    id="memberId"
                    placeholder="請輸入會員編號"
                  />
                </div>

                <!-- 巴士編號欄位 -->
                <div class="mb-20">
                  <label for="busId" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    巴士編號 <span class="text-danger-600">*</span>
                  </label>
                  <input
                    v-model.number="form.busId"
                    type="number"
                    min="1"
                    class="form-control radius-8"
                    id="busId"
                    placeholder="請輸入巴士編號"
                  />
                </div>

        <!-- 起點地址欄位 (移除地圖互動部分) -->
        <div class="mb-20">
          <label for="startAddress" class="form-label fw-semibold text-primary-light text-sm mb-8">
            起點地址 <span class="text-danger-600">*</span>
          </label>
          <input
            v-model="form.startAddress"
            type="text"
            class="form-control radius-8"
            id="startAddress"
            placeholder="請輸入起點地址"
            @blur="calculateDistanceAndPrice"
          />
        </div>

        <!-- 終點地址欄位 (移除地圖互動部分) -->
        <div class="mb-20">
          <label for="endAddress" class="form-label fw-semibold text-primary-light text-sm mb-8">
            終點地址 <span class="text-danger-600">*</span>
          </label>
          <input
            v-model="form.endAddress"
            type="text"
            class="form-control radius-8"
            id="endAddress"
            placeholder="請輸入終點地址"
            @blur="calculateDistanceAndPrice"
          />
        </div>
                  
        
<!-- 顯示距離和價格 (添加預估標記和計算中狀態) -->
<div class="mb-20" v-if="estimatedInfo.isCalculating || estimatedInfo.distance">
  <div class="card bg-light border">
    <div class="card-body py-2">
      <div v-if="estimatedInfo.isCalculating" class="text-center">
        <div class="spinner-border spinner-border-sm text-primary" role="status">
          <span class="visually-hidden">計算中...</span>
        </div>
        <span class="ms-2">正在計算距離與價格...</span>
      </div>
      <div v-else class="d-flex justify-content-between align-items-center">
        <div>
          <span class="fw-semibold">預估距離:</span> 
          <span class="ms-2">{{ estimatedInfo.formattedDistance }}</span>
          <span v-if="estimatedInfo.isEstimated || estimatedInfo.isDefault" class="badge bg-warning text-dark ms-2 small">預估值</span>
        </div>
        <div>
          <span class="fw-semibold">預估費用:</span> 
          <span class="ms-2 text-primary fw-bold">{{ estimatedInfo.formattedPrice }}</span>
        </div>
      </div>
      <div v-if="estimatedInfo.isDefault" class="mt-1 text-center">
        <small class="text-muted">
          <i class="bi bi-info-circle me-1"></i>
          系統無法獲取實際路線距離，使用預設估計值
        </small>
      </div>
    </div>
  </div>
</div>

                <!-- 預約時間欄位 -->
<div class="mb-20">
  <label for="scheduledAt" class="form-label fw-semibold text-primary-light text-sm mb-8">
    預約時間 <span class="text-danger-600">*</span>
  </label>
  <input
    v-model="form.scheduledAt"
    type="datetime-local"
    class="form-control radius-8"
    id="scheduledAt"
    placeholder="請選擇預約時間"
    step="60" 
  />
</div>

                <!-- 備註欄位 -->
                <div class="mb-20">
                  <label for="note" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    備註
                  </label>
                  <textarea
                    v-model="form.note"
                    class="form-control radius-8"
                    id="note"
                    rows="3"
                    placeholder="請輸入備註內容"
                  ></textarea>
                </div>

                <!-- 預約狀態 -->
                <div class="mb-20">
                  <label for="reservationStatus" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    預約狀態 <span class="text-danger-600">*</span>
                  </label>
                  <div class="position-relative">
                    <select v-model="form.reservationStatus" class="form-control radius-8 form-select" id="reservationStatus">
                      <option value="">請選擇預約狀態</option>
                      <option value="pending">待處理</option>
                      <option value="in_progress">進行中</option>
                      <option value="completed">已完成</option>
                      <option value="cancelled">已取消</option>
                    </select>
                  
                  </div>
                </div>

                <!-- 按鈕區域 -->
                <div class="d-flex align-items-center justify-content-center gap-3">
                  <button
                    type="button"
                    class="border border-danger-600 bg-hover-danger-200 text-danger-600 text-md px-56 py-11 radius-8"
                    @click="cancelForm"
                  >
                    取消
                  </button>
                  <button
                    type="submit"
                    class="btn btn-primary border border-primary-600 text-md px-56 py-12 radius-8"
                    :disabled="submitting"
                  >
                    <span v-if="submitting" class="spinner-border spinner-border-sm me-2" role="status"></span>
                    儲存
                  </button>
                </div>
  
        <!-- 移除地圖座標輸入區塊，改為隱藏欄位 -->
        <input type="hidden" v-model="form.coords.startLat">
        <input type="hidden" v-model="form.coords.startLng">
        <input type="hidden" v-model="form.coords.endLat">
        <input type="hidden" v-model="form.coords.endLng">

              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, watch, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { reservationService, toYMDHM } from '@/services/busReservationService';
// showToast
import { useToast } from '@/composables/useToast';
import { useConfirmDialog } from '@/composables/useConfirmDialog';

export default {
  name: 'EditRes',
  // 可選的 prop；若未傳入就用路由的 :id
  props: {
    reservationId: { type: [String, Number], required: false }
  },
  emits: ['edit-success'],
  setup(props, { emit }) {
    const route = useRoute();
    const router = useRouter();
    const { showToast } = useToast();
    const { showConfirmDialog } = useConfirmDialog();

    // 統一 ID 來源
    const id = computed(() => Number(props.reservationId ?? route.params.id));

    const loading = ref(false);
    const submitting = ref(false);
    const error = ref('');

    const form = ref({
      memberId: null,
      busId: null,
      startAddress: '',
      endAddress: '',
      scheduledAt: '',
      note: '',
      reservationStatus: '',
      coords: {
        startLat: null,
        startLng: null,
        endLat: null,
        endLng: null
      },
      system: {
        distanceMeters: null,
        price: null
      }
    });

    const estimatedInfo = ref({
      distance: null,
      formattedDistance: '',
      price: null,
      formattedPrice: '',
      isCalculating: false
    });

    // 簡單 debounce
    const debounce = (fn, delay) => {
      let t = null;
      return (...args) => {
        clearTimeout(t);
        t = setTimeout(() => fn(...args), delay);
      };
    };

    const calculateDistanceAndPrice = async () => {
      if (!form.value.startAddress || !form.value.endAddress) return;
      estimatedInfo.value.isCalculating = true;
      try {
        const r = await reservationService.calculateDistanceAndPrice(
          form.value.startAddress,
          form.value.endAddress
        );
        const dist = r?.distanceMeters ?? 5000;
        const price = r?.rehabusFare ?? 95;

        estimatedInfo.value = {
          distance: dist,
          formattedDistance: `${(dist / 1000).toFixed(1)} km`,
          price,
          formattedPrice: `$${price}`,
          isCalculating: false,
          isEstimated: r?.estimated === true,
          isDefault: !r
        };
        form.value.system = { distanceMeters: dist, price };
      } catch {
        // 預設值
        estimatedInfo.value = {
          distance: 5000,
          formattedDistance: '5.0 km',
          price: 95,
          formattedPrice: '$95',
          isCalculating: false,
          isEstimated: true,
          isDefault: true
        };
        form.value.system = { distanceMeters: 5000, price: 95 };
      }
    };

    const debouncedCalculate = debounce(calculateDistanceAndPrice, 1000);

    watch(
      [() => form.value.startAddress, () => form.value.endAddress],
      () => {
        if (form.value.startAddress && form.value.endAddress) {
          debouncedCalculate();
        }
      }
    );

    const loadReservation = async () => {
      loading.value = true;
      error.value = '';
      try {
        console.log('正在載入預約 ID:', id.value);
        const reservation = await reservationService.getReservationById(id.value);

        const scheduledAt = reservation.scheduledAt
          ? new Date(reservation.scheduledAt).toISOString().slice(0, 16)
          : '';

        form.value = {
          memberId: reservation.memberId,
          busId: reservation.busId,
          startAddress: reservation.startAddress || '',
          endAddress: reservation.endAddress || '',
          scheduledAt,
          note: reservation.note || '',
          reservationStatus: reservation.reservationStatus || 'pending',
          coords: {
            startLat: reservation.coords?.startLat ?? null,
            startLng: reservation.coords?.startLng ?? null,
            endLat: reservation.coords?.endLat ?? null,
            endLng: reservation.coords?.endLng ?? null
          },
          system: {
            distanceMeters: reservation.system?.distanceMeters ?? null,
            price: reservation.system?.price ?? null
          }
        };

        if (reservation.system?.distanceMeters) {
          estimatedInfo.value = {
            distance: reservation.system.distanceMeters,
            formattedDistance:
              reservation.display?.formattedDistance ??
              `${(reservation.system.distanceMeters / 1000).toFixed(1)} km`,
            price: reservation.system.price ?? 0,
            formattedPrice:
              reservation.display?.formattedPrice ?? `$${reservation.system.price ?? 0}`,
            isCalculating: false
          };
        }
      } catch (e) {
        console.error(e);
        error.value = e.message || '無法載入預約資料';
      } finally {
        loading.value = false;
      }
    };

    // 狀態顯示
    const normalizeStatus = (raw) => {
      const s = String(raw || '').trim().toLowerCase();
      if (['pending', 'in_progress', 'completed', 'cancelled'].includes(s)) return s;
      if (['active', 'processing', 'waiting'].includes(s)) return 'pending';
      if (['done', 'finished', 'complete'].includes(s)) return 'completed';
      if (['canceled', 'cancel', 'void'].includes(s)) return 'cancelled';
      return 'pending';
    };
    const getStatusClass = (status) =>
      ({ pending: 'bg-warning', in_progress: 'bg-primary', completed: 'bg-success', cancelled: 'bg-danger' }[
        normalizeStatus(status)
      ] || 'bg-warning');
    const getStatusText = (status) =>
      ({ pending: '待處理', in_progress: '進行中', completed: '已完成', cancelled: '已取消' }[
        normalizeStatus(status)
      ] || '未知狀態');

       const cancelForm = async () => {
      // 使用 showConfirmDialog 替代原生 confirm
      const confirmed = await showConfirmDialog({
        title: '確認取消編輯',
        message: '確定要取消編輯嗎？所有未儲存的變更將會遺失。',
        type: 'warning',
        confirmText: '確認取消',
        cancelText: '繼續編輯',
        confirmButtonClass: 'btn-danger',
        icon: 'heroicons:x-circle' // 添加警告圖示
      });
      
      // 如果用戶確認取消，則返回列表頁
      if (confirmed) {
        router.push('/reservation/list');
      }
    };

    const submitForm = async () => {
      try {
        submitting.value = true;

        if (
          !form.value.memberId ||
          !form.value.busId ||
          !form.value.startAddress ||
          !form.value.endAddress ||
          !form.value.scheduledAt
        ) {
          showToast({
            title: '表單錯誤',
            message: '請填寫所有必填欄位',
            type: 'error'
          });
          return;
        }

        if (!form.value.reservationStatus) {
          form.value.reservationStatus = 'pending';
        }

        if (!estimatedInfo.value.distance) {
          await calculateDistanceAndPrice();
        }

        const reservationData = {
          id: id.value,
          busId: form.value.busId,
          startAddress: form.value.startAddress.trim(),
          endAddress: form.value.endAddress.trim(),
          scheduledAt: toYMDHM(form.value.scheduledAt),
          note: form.value.note || '',
          reservationStatus: form.value.reservationStatus || 'pending',
          startLat: form.value.coords?.startLat ?? null,
          startLng: form.value.coords?.startLng ?? null,
          endLat: form.value.coords?.endLat ?? null,
          endLng: form.value.coords?.endLng ?? null,
          price: estimatedInfo.value.price || 0
        };

        console.log('提交更新請求，資料:', reservationData);
        await reservationService.updateReservation(id.value, reservationData);

        showToast({
          title: '更新成功',
          message: '預約資料已成功更新！',
          type: 'success'
        });
        emit('edit-success');
        window.dispatchEvent(new CustomEvent('reservation-status-updated'));
        router.push('/reservation/list');
      } catch (e) {
        console.error('更新預約失敗:', e);
        showToast({
          title: '更新失敗',
          message: e.message || '發生未知錯誤',
          type: 'error'
        });
      } finally {
        submitting.value = false;
      }
    };

    onMounted(loadReservation);
    // 若從 A 單跳到 B 單（同頁換路由參數），自動重載
    watch(() => id.value, loadReservation);

    return {
      form,
      loading,
      submitting,
      error,
      estimatedInfo,
      loadReservation,
      calculateDistanceAndPrice,
      cancelForm,
      submitForm,
      getStatusClass,
      getStatusText
    };
  }
};
</script>


<style scoped>
/* 狀態標記樣式 */
.status-badge {
  width: 12px;
  height: 12px;
}

.bg-warning {
  background-color: #ffc107 !important; /* 黃色 - 待處理 */
}

.bg-primary {
  background-color: #0d6efd !important; /* 藍色 - 進行中 */
}

.bg-success {
  background-color: #198754 !important; /* 綠色 - 已完成 */
}

.bg-danger {
  background-color: #dc3545 !important; /* 紅色 - 已取消 */
}


/* 下拉選單樣式調整，適應狀態圖示 */
select.form-select {
  padding-right: 100px; /* 留出足夠空間顯示狀態圖示 */
}


</style>