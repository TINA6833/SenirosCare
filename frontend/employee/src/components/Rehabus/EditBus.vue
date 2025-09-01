<template>
  <div class="card h-100 p-0 radius-12">
    <div class="card-body p-24">
      <div class="row justify-content-center">
        <div class="col-xxl-6 col-xl-8 col-lg-10">
          <div class="card border">
            <div class="card-body">
              <h6 class="text-md text-primary-light mb-16">編輯復康巴士資料</h6>

              <!-- 載入中顯示 -->
              <div v-if="loading" class="text-center py-5">
                <div class="spinner-border text-primary" role="status">
                  <span class="visually-hidden">載入中...</span>
                </div>
              </div>

              <!-- 錯誤訊息 -->
              <div v-else-if="error" class="alert alert-danger">
                {{ error }}
              </div>

              <!-- 表單內容 -->
              <template v-else>
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
                    <button type="button"
                      class="border border-danger-600 bg-hover-danger-200 text-danger-600 text-md px-56 py-11 radius-8"
                      @click="cancelForm">
                      取消
                    </button>
                    <button type="submit"
                      class="btn btn-primary border border-primary-600 text-md px-56 py-12 radius-8">
                      更新
                    </button>
                  </div>


                </form>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import busImage from "@/assets/images/bus.jpg";
import { rehabusService } from '@/services/rehabusService';
import { useToast } from '@/composables/useToast';

export default {
  name: 'EditBus',
  props: {
    busId: {
      type: [String, Number],
      required: true
    }
  },


  // 聲明組件發射的事件
  emits: ['edit-success'],
  setup(props, { emit }) {
    const { showToast } = useToast();
    // 表單資料
    const form = ref({
      carDealership: '',
      busBrand: '',
      busModel: '',
      seatCapacity: 0,
      wheelchairCapacity: 0,
      licensePlate: '',
      status: ''
    });

    // 圖片預覽
    const imagePreview = ref(busImage);

    // 載入狀態與錯誤訊息
    const loading = ref(true);
    const error = ref(null);

    const router = useRouter();

    /**
     * 載入巴士資料
     */
    const fetchBusData = async () => {
      try {
        loading.value = true;
        const busData = await rehabusService.getBusById(props.busId);

        // 將獲取的資料填入表單
        form.value = {
          carDealership: busData.carDealership || '',
          busBrand: busData.busBrand || '',
          busModel: busData.busModel || '',
          seatCapacity: busData.seatCapacity || 0,
          wheelchairCapacity: busData.wheelchairCapacity || 0,
          licensePlate: busData.licensePlate || '',
          status: busData.status || 'available'
        };

        // 如果有圖片，更新圖片預覽
        if (busData.image) {
          imagePreview.value = busData.image;
        }
      } catch (err) {
        console.error('載入復康巴士資料失敗:', err);
        error.value = `載入資料失敗: ${err.message}`;
      } finally {
        loading.value = false;
      }
    };

    /**
     * 處理圖片上傳變更事件
     * @param {Event} event - 檔案上傳事件
     */
    const onImageChange = (event) => {
      const file = event.target.files[0];
      if (file) {
        const reader = new FileReader();
        reader.onload = (e) => {
          imagePreview.value = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    };

    /**
     * 取消表單編輯，返回列表頁面
     */
    const cancelForm = () => {
      router.push('/rehabus/list');
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
          !form.value.licensePlate ||
          !form.value.status) {
          showToast({
            title: '缺少欄位',
            message: errorMessage,
            type: 'error'
          });
          return;
        }

        // 呼叫服務層更新巴士資料
        const updatedBus = await rehabusService.updateBus(props.busId, {
          carDealership: form.value.carDealership,
          busBrand: form.value.busBrand,
          busModel: form.value.busModel,
          seatCapacity: form.value.seatCapacity,
          wheelchairCapacity: form.value.wheelchairCapacity,
          licensePlate: form.value.licensePlate,
          status: form.value.status
        });

        console.log('更新成功，返回資料:', updatedBus);

        showToast({
          title: '更新成功',
          message: '復康巴士資料已成功更新！',
          type: 'success'
        });

        // 發射編輯成功事件通知父元件
        emit('edit-success');

        // 發送全域事件，通知列表頁面刷新
        window.dispatchEvent(new CustomEvent('bus-status-updated'));

        // 回到列表頁面
        router.push('/rehabus/list');

      } catch (err) {
        console.error('更新復康巴士失敗:', err);
        showToast({
          title: '更新失敗',
          message: errorMessage,
          type: 'error'
        });
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

    // 1. 定義 getStatusClass
    const getStatusClass = (status) => {
      switch (status) {
        case 'available':
          return 'bg-success'
        case 'dispatched':
          return 'bg-warning'
        case 'maintenance':
          return 'bg-danger'
        default:
          return ''
      }
    }

    // 2. 組件掛載時載入資料
    onMounted(() => {
      fetchBusData()
    })




    // 組件掛載時載入資料
    onMounted(() => {
      fetchBusData();
    });

    return {
      form,
      imagePreview,
      loading,
      error,
      onImageChange,
      cancelForm,
      submitForm,
      getStatusClass,
      getStatusText,
      getStatusClass
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
  border-radius: 8px;
  /* 改為方形或圓角方形更適合巴士圖片 */
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
</style>