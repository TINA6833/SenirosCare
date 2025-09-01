<template>
  <div class="card h-100 p-0 radius-12">
    <div class="card-body p-24">
      <div class="row justify-content-center">
        <div class="col-xxl-6 col-xl-8 col-lg-10">
          <div class="card border">
            <div class="card-body">
             

              <!-- 表單 -->
              <form @submit.prevent="submitForm">
                <!-- 會員編號欄位 -->
                <div class="mb-20">
                  <label for="memberId" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    會員編號 <span class="text-danger-600">*</span>
                  </label>
                  <input v-model.number="form.memberId" type="number" min="1" class="form-control radius-8"
                    id="memberId" placeholder="請輸入會員編號" />
                </div>

                <!-- 巴士編號欄位 -->
                <div class="mb-20">
                  <label for="busId" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    巴士編號 <span class="text-danger-600">*</span>
                  </label>
                  <input v-model.number="form.busId" type="number" min="1" class="form-control radius-8" id="busId"
                    placeholder="請輸入巴士編號" />
                </div>

                <!-- 起點地址欄位 -->
                <div class="mb-20">
                  <label for="startAddress" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    起點地址 <span class="text-danger-600">*</span>
                  </label>
                  <input v-model="form.startAddress" type="text" class="form-control radius-8" id="startAddress"
                    placeholder="請輸入起點地址" />
                </div>

                <!-- 終點地址欄位 -->
                <div class="mb-20">
                  <label for="endAddress" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    終點地址 <span class="text-danger-600">*</span>
                  </label>
                  <input v-model="form.endAddress" type="text" class="form-control radius-8" id="endAddress"
                    placeholder="請輸入終點地址" />
                </div>

                <!-- 預約時間欄位 -->
                <div class="mb-20">
                  <label for="scheduledAt" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    預約時間 <span class="text-danger-600">*</span>
                  </label>
                  <input v-model="form.scheduledAt" type="datetime-local" class="form-control radius-8" id="scheduledAt"
                    placeholder="請選擇預約時間" step="60" />
                </div>

                <!-- 顯示距離和價格 -->
                <div class="mb-20" v-if="calculatedInfo.distance">
                  <div class="card bg-light border">
                    <div class="card-body py-2">
                      <div class="d-flex justify-content-between align-items-center">
                        <div>
                          <span class="fw-semibold">預估距離:</span>
                          <span class="ms-2">{{ calculatedInfo.formattedDistance }}</span>
                        </div>
                        <div>
                          <span class="fw-semibold">預估費用:</span>
                          <span class="ms-2 text-primary fw-bold">{{ calculatedInfo.formattedPrice }}</span>
                        </div>
                      </div>
                      <div class="text-muted small mt-1">
                        <i class="bi bi-info-circle me-1"></i>
                        系統根據路線距離自動計算，實際費用依實際行駛情況而定
                      </div>
                    </div>
                  </div>
                </div>


                <!-- 備註欄位 -->
                <div class="mb-20">
                  <label for="note" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    備註
                  </label>
                  <textarea v-model="form.note" class="form-control radius-8" id="note" rows="3"
                    placeholder="請輸入備註內容"></textarea>
                </div>

                <!-- 預約狀態 -->
                <div class="mb-20">
                  <label for="reservationStatus" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    預約狀態 <span class="text-danger-600">*</span>
                  </label>
                  <div class="position-relative">
                    <select v-model="form.reservationStatus" class="form-control radius-8 form-select"
                      id="reservationStatus">
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
                  <button type="button"
                    class="border border-danger-600 bg-hover-danger-200 text-danger-600 text-md px-56 py-11 radius-8"
                    @click="cancelForm">
                    取消
                  </button>
                  <button type="submit" class="btn btn-primary border border-primary-600 text-md px-56 py-12 radius-8">
                    儲存
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { reservationService, toYMDHM } from '@/services/busReservationService';
import { useToast } from '@/composables/useToast';
import { useConfirmDialog } from '@/composables/useConfirmDialog';


export default {
  name: 'AddRes',
  // 聲明組件發射的事件
  emits: ['add-success'],
  setup(props, { emit }) {

    const { showToast } = useToast();
    const { showConfirmDialog } = useConfirmDialog();
    // 表單資料
    const form = ref({
      memberId: null,
      busId: null,
      startAddress: '',
      endAddress: '',
      scheduledAt: '',
      note: '',
      reservationStatus: 'pending', // 預設為待確認狀態
      coords: {
        startLat: null,
        startLng: null,
        endLat: null,
        endLng: null
      }
    });

    // 計算資訊
    const calculatedInfo = ref({
      distance: null,
      formattedDistance: '',
      price: null,
      formattedPrice: ''
    });

    // 載入狀態
    const loading = ref(false);

    // 計算距離與價格
    const calculateDistanceAndPrice = async () => {
      if (!form.value.startAddress || !form.value.endAddress) return;

      try {
        loading.value = true;

        // 使用 reservationService 呼叫後端 API
        const response = await reservationService.calculateDistanceAndPrice(
          form.value.startAddress,
          form.value.endAddress
        );

        // 根據 FareController 回傳的格式調整
        if (response && response.data) {
          const data = response.data;

          calculatedInfo.value = {
            distance: data.distanceMeters,
            formattedDistance: `${(data.distanceMeters / 1000).toFixed(1)} km`,
            price: data.rehabusFare,
            formattedPrice: `$${data.rehabusFare}`,
            // 增加計程車價格
            taxiFare: data.taxiFare,
            formattedTaxiFare: `$${data.taxiFare}`
          };

          // 更新表單中的系統資料，以便提交時一併送出
          form.value.distanceMeters = data.distanceMeters;
          form.value.price = data.rehabusFare;
        }
      } catch (error) {
        console.error('計算距離與價格失敗:', error);
        calculatedInfo.value = {
          distance: 5000,
          formattedDistance: '5.0 km',
          price: 95,
          formattedPrice: '$95',
          taxiFare: 190,
          formattedTaxiFare: '$190'
        };

        // 更新表單中的系統資料
        form.value.distanceMeters = 5000;
        form.value.price = 95;
      } finally {
        loading.value = false;
      }
    };
    /**
     * 獲取狀態對應的樣式類別
     * @param {string} status - 預約狀態
     * @returns {string} 對應的 CSS 類別名稱
     */

    // 後端→前端狀態正規化
    const normalizeStatus = (raw) => {
      const s = String(raw || '').trim().toLowerCase();
      if (!s) return 'pending';

      // 直接支援的
      if (['pending', 'in_progress', 'completed', 'cancelled'].includes(s)) return s;

      // 常見別名／資料庫值對應
      if (['active', 'processing', 'waiting'].includes(s)) return 'pending';
      if (['done', 'finished', 'complete'].includes(s)) return 'completed';
      if (['canceled', 'cancel', 'void'].includes(s)) return 'cancelled';

      // 其他未知 → 當成 pending
      return 'pending';
    };

    const getStatusClass = (status) => {
      switch (normalizeStatus(status)) {
        case 'pending': return 'bg-warning';
        case 'in_progress': return 'bg-primary';
        case 'completed': return 'bg-success';
        case 'cancelled': return 'bg-danger';
        default: return 'bg-warning';
      }
    };

    /**
     * 獲取狀態對應的顯示文字
     * @param {string} status - 預約狀態
     * @returns {string} 對應的顯示文字
     */
    const getStatusText = (status) => {
      switch (normalizeStatus(status)) {
        case 'pending': return '待處理';
        case 'in_progress': return '進行中';
        case 'completed': return '已完成';
        case 'cancelled': return '已取消';
        default: return '未知狀態';
      }
    };

    /**
     * 重置表單資料
     */
    const resetForm = () => {
      // 重置表單數據為初始值
      form.value = {
        memberId: null,
        busId: null,
        startAddress: '',
        endAddress: '',
        scheduledAt: '',
        note: '',
        reservationStatus: 'pending',
        coords: {
          startLat: null,
          startLng: null,
          endLat: null,
          endLng: null
        }
      };
    };

    /**
     * 取消表單編輯，提示確認
     */
     const cancelForm = async () => {
      const confirmed = await showConfirmDialog({
        title: '確認取消新增',
        message: '確定要取消編輯嗎？所有未儲存的變更將會遺失。',
        type: 'warning',
        confirmText: '確認取消',
        cancelText: '繼續編輯',
        confirmButtonClass: 'btn-danger', 
        icon: 'heroicons:x-circle' // 添加警告圖示
      });
      
      if (confirmed) {
        resetForm();
      }
    };

    /**
     * 提交表單資料至後端
     */
    const submitForm = async () => {
      try {

        loading.value = true;
        // 表單驗證
        if (!form.value.memberId ||
          !form.value.busId ||
          !form.value.startAddress ||
          !form.value.endAddress ||
          !form.value.scheduledAt) {
          showToast({
            title: '缺少欄位',
            message: '請填寫所有必填欄位。',
            type: 'warning'
          });
          loading.value = false;
          return;
        }

        // 確保狀態值有設定
        if (!form.value.reservationStatus) {
          form.value.reservationStatus = 'pending';
        }

        // 如果未計算距離和價格，先計算
        if (!calculatedInfo.value.distance) {
          await calculateDistanceAndPrice();
        }



        // 準備送出的資料
        const reservationData = {
          memberId: form.value.memberId,
          busId: form.value.busId,
          startAddress: form.value.startAddress.trim(),
          endAddress: form.value.endAddress.trim(),
          scheduledAt: toYMDHM(form.value.scheduledAt),
          note: form.value.note || '',
          reservationStatus: form.value.reservationStatus || 'pending',

          // 扁平化座標資料 (與其他地方保持一致)

          startLat: form.value.coords?.startLat || null,
          startLng: form.value.coords?.startLng || null,
          endLat: form.value.coords?.endLat || null,
          endLng: form.value.coords?.endLng || null,

          // 添加系統計算的價格和距離
          price: calculatedInfo.value.price || 0,
          distanceMeters: calculatedInfo.value.distance || 0

        };

        console.log('提交新增預約請求，資料:', reservationData);

        // 呼叫服務層新增預約
        const result = await reservationService.createReservation(reservationData);
        console.log('新增結果:', result);

        showToast({
          title: '預約成功',
          message: '成功預約巴士',
          type: 'success'
        });

        // 發射成功事件通知父元件
        emit('add-success');

        // 發送全域事件，通知列表頁面刷新
        window.dispatchEvent(new CustomEvent('reservation-status-updated'));

        // 重置表單
        resetForm();
      } catch (error) {
        console.error('新增預約失敗:', error);
        showToast({
          title: '新增失敗',
          message: '復康巴士資料新增失敗！',
          type: 'error'
        });
      } finally {
        loading.value = false;
      }
    };

    return {
      form,
      loading,
      calculatedInfo,
      calculateDistanceAndPrice,
      cancelForm,
      resetForm,
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
  background-color: #ffc107 !important;
  /* 黃色 - 待處理 */
}

.bg-primary {
  background-color: #0d6efd !important;
  /* 藍色 - 進行中 */
}

.bg-success {
  background-color: #198754 !important;
  /* 綠色 - 已完成 */
}

.bg-danger {
  background-color: #dc3545 !important;
  /* 紅色 - 已取消 */
}


/* 下拉選單樣式調整，適應狀態圖示 */
select.form-select {
  padding-right: 100px;
  /* 留出足夠空間顯示狀態圖示 */
}

/* 游標樣式 */
.cursor-pointer {
  cursor: pointer;
}
</style>