<template>
  <div class="card h-100 p-0 radius-12">
    <div class="card-body p-24">
      <div class="row justify-content-center">
        <div class="col-xxl-6 col-xl-8 col-lg-10">
          <div class="card border">
            <div class="card-body">


              <!-- 巴士圖片上傳區塊 -->
              <div class="mb-24 mt-16">
                <div class="avatar-upload">
                  <div class="avatar-edit position-absolute bottom-0 end-0 me-24 mt-16 z-1 cursor-pointer">
                    <input type="file" id="imageUpload" accept=".png, .jpg, .jpeg" @change="onImageChange" hidden />
                    <label for="imageUpload"
                      class="w-32-px h-32-px d-flex justify-content-center align-items-center bg-primary-50 text-primary-600 border border-primary-600 bg-hover-primary-100 text-lg rounded-circle">
                      <iconify-icon icon="solar:camera-outline" class="icon"></iconify-icon>
                    </label>
                  </div>
                  <div class="avatar-preview">
                    <div v-bind:style="{ backgroundImage: 'url(' + imagePreview + ')' }" id="imagePreview"></div>
                  </div>
                </div>
              </div>
              <!-- 巴士圖片上傳結束 -->

              <!-- 表單 -->
              <form @submit.prevent="submitForm">
                <!-- 車行欄位 -->
                <div class="mb-20">
                  <label for="carDealership" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    車行名稱 <span class="text-danger-600">*</span>
                  </label>
                  <input v-model="form.carDealership" type="text" class="form-control radius-8" id="carDealership"
                    placeholder="請輸入車行名稱" />
                </div>

                <!-- 汽車廠牌欄位 -->
                <div class="mb-20">
                  <label for="busBrand" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    汽車廠牌 <span class="text-danger-600">*</span>
                  </label>
                  <input v-model="form.busBrand" type="text" class="form-control radius-8" id="busBrand"
                    placeholder="請輸入汽車廠牌" />
                </div>

                <!-- 汽車型號欄位 -->
                <div class="mb-20">
                  <label for="busModel" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    型號 <span class="text-danger-600">*</span>
                  </label>
                  <input v-model="form.busModel" type="text" class="form-control radius-8" id="busModel"
                    placeholder="請輸入汽車型號" />
                </div>

                <!-- 一般座位數量 -->
                <div class="mb-20">
                  <label for="seatCapacity" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    一般座位數量 <span class="text-danger-600">*</span>
                  </label>
                  <input v-model.number="form.seatCapacity" type="number" min="0" class="form-control radius-8"
                    id="seatCapacity" placeholder="請輸入一般座位數量" />
                </div>

                <!-- 輪椅座位數量 -->
                <div class="mb-20">
                  <label for="wheelchairCapacity" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    輪椅座位數量 <span class="text-danger-600">*</span>
                  </label>
                  <input v-model.number="form.wheelchairCapacity" type="number" min="0" class="form-control radius-8"
                    id="wheelchairCapacity" placeholder="請輸入輪椅座位數量" />
                </div>

                <!-- 車牌號碼 -->
                <div class="mb-20">
                  <label for="licensePlate" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    車牌號碼 <span class="text-danger-600">*</span>
                  </label>
                  <input v-model="form.licensePlate" type="text" class="form-control radius-8" id="licensePlate"
                    placeholder="請輸入車牌號碼" />
                </div>

                <!-- 派遣狀態 -->
                <div class="mb-20">
                  <label for="status" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    派遣狀態 <span class="text-danger-600">*</span>
                  </label>
                  <div class="position-relative">
                    <select v-model="form.status" class="form-control radius-8 form-select" id="status">
                      <option value="">請選擇派遣狀態</option>
                      <option value="available">可派遣</option>
                      <option value="dispatched">已派遣</option>
                      <option value="maintenance">維修中</option>
                    </select>

                  </div>
                </div>

                <!-- 按鈕區域 -->
                <div class="d-flex align-items-center justify-content-center gap-3">

                  <!-- 測試按鈕 -->
                  <button type="button" class="btn btn-outline-secondary action-btn" @click="fillTestData">
                    測試
                  </button>

                  <!-- 取消 / 儲存 -->
                  <button type="button"
                    class="border border-danger-600 bg-hover-danger-200 text-danger-600 text-md px-56 py-11 radius-8"
                    @click="cancelForm">
                    清除
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
import busImage from "@/assets/images/bus.jpg";
import { rehabusService } from '@/services/rehabusService';
import { useToast } from '@/composables/useToast'; // 引入 toast 元件
import { useConfirmDialog } from '@/composables/useConfirmDialog'; // 引入自訂確認對話框

export default {
  name: 'addBus',
  emits: ['add-success'],
  setup(props, { emit }) {
    const { showToast } = useToast();
    const { showConfirmDialog } = useConfirmDialog(); // 取得自訂確認對話框方法
    // 表單資料
    const form = ref({
      carDealership: '',
      busBrand: '',
      busModel: '',
      seatCapacity: 0,
      wheelchairCapacity: 0,
      licensePlate: '',
      status: 'available' // 預設為可派遣狀態
    });

    /**
     * 獲取狀態對應的樣式類別
     * @param {string} status - 巴士狀態
     * @returns {string} 對應的 CSS 類別名稱
     */
    const getStatusClass = (status) => {
      switch (status) {
        case 'available':
          return 'bg-success';
        case 'dispatched':
          return 'bg-warning';
        case 'maintenance':
          return 'bg-danger';
        default:
          return 'bg-secondary';
      }
    };

    /**
     * 獲取狀態對應的顯示文字
     * @param {string} status - 巴士狀態
     * @returns {string} 對應的顯示文字
     */
    const getStatusText = (status) => {
      switch (status) {
        case 'available':
          return '可派遣';
        case 'dispatched':
          return '已派遣';
        case 'maintenance':
          return '維修中';
        default:
          return '請選擇狀態';
      }
    };

    // 圖片預覽
    const imagePreview = ref(busImage);

    /**
     * 處理圖片上傳變更事件
     * @param {Event} event - 檔案上傳事件
     */
    const onImageChange = (event) => {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          console.log("上傳巴士圖片:", e.target.result);
          imagePreview.value = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    };

    /**
     * 重置表單資料
     */
    const resetForm = () => {
      // 重置表單數據為初始值
      form.value = {
        carDealership: '',
        busBrand: '',
        busModel: '',
        seatCapacity: 0,
        wheelchairCapacity: 0,
        licensePlate: '',
        status: 'available',
      };
      imagePreview.value = busImage;
    };

    /**
     * 取消表單編輯，提示確認（改用自訂確認對話框）
     */

    const cancelForm = () => {
        resetForm();
    };

    /**
     * 提交表單資料至後端
     */
    const submitForm = async () => {
      try {
        // 表單驗證
        if (!form.value.carDealership ||
          !form.value.busBrand ||
          !form.value.busModel ||
          !form.value.licensePlate) {
          showToast({
            title: '請填寫所有欄位',
            message: '車行名稱、汽車廠牌、型號和車牌號碼為必填欄位。',
            type: 'warning'
          });
          return;
        }

        // 確保狀態值有設定
        if (!form.value.status) {
          form.value.status = 'available';
        }

        // 呼叫服務層新增巴士
        await rehabusService.addBus({
          carDealership: form.value.carDealership,
          busBrand: form.value.busBrand,
          busModel: form.value.busModel,
          seatCapacity: form.value.seatCapacity,
          wheelchairCapacity: form.value.wheelchairCapacity,
          licensePlate: form.value.licensePlate,
          status: form.value.status,
          // 如需上傳圖片，可在此加入圖片資料
        });

        showToast({
          title: '新增成功',
          message: '復康巴士資料新增成功！',
          type: 'success'
        });

        // 修正：發射成功事件通知父元件後再重置表單
        emit('add-success');

        // 發送全域事件，通知列表頁面刷新
        window.dispatchEvent(new CustomEvent('bus-status-updated'));

        // 重置表單
        resetForm();
      } catch (error) {
        console.error('新增復康巴士失敗:', error);
        showToast({
          title: '新增失敗',
          message: '復康巴士資料新增失敗！',
          type: 'error'
        });
      }
    };

    /** 填入測試資料 */
    const fillTestData = () => {
      form.value = {
        carDealership: '生通股份有限公司',
        busBrand: 'Hyundai',
        busModel: 'STARIA',
        seatCapacity: 2,
        wheelchairCapacity: 1,
        licensePlate: 'WEZ-3088',
        status: 'available'
      };
    };



    return {
      form,
      imagePreview,
      onImageChange,
      cancelForm,
      resetForm,
      submitForm,
      getStatusClass,
      getStatusText,
      fillTestData
    };
  }
};
</script>

<style scoped>
/* 確保圖片預覽區域樣式與原有一致 */
.avatar-upload {
  position: relative;
  max-width: 120px;
  margin: 0 auto;
}

.avatar-preview {
  width: 120px;
  height: 120px;
  position: relative;
  border-radius: 100%;
  overflow: hidden;
}

.avatar-preview>div {
  width: 100%;
  height: 100%;
  border-radius: 100%;
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
}

/* 巴士圖片預覽調整樣式 - 可能需要根據實際巴士圖片調整 */
.avatar-preview>div {
  border: 8px;
}

/* 狀態標記樣式 */
.status-badge {
  width: 12px;
  height: 12px;
}

.bg-success {
  background-color: #00ab55;
}

.bg-warning {
  background-color: #ffa000;
}

.bg-danger {
  background-color: #ff3030;
}

.bg-secondary {
  background-color: #757575;
}

/* 下拉選單樣式調整，適應狀態圖示 */
select.form-select {
  padding-right: 100px;
  /* 留出足夠空間顯示狀態圖示 */
}


.action-btn {
  min-width: 120px;
  padding: 12px 56px;
  border-radius: 8px;
  font-size: 1rem;
  line-height: 1.2;
}
</style>