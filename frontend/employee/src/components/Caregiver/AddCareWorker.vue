<template>
  <div class="card h-100 p-0 radius-12">
    <div class="card-body p-24">
      <div class="row justify-content-center">
        <div class="col-xxl-6 col-xl-8 col-lg-10">
          <div class="card border">
            <div class="card-body">
              <h6 class="text-md text-primary-light mb-16">照服員照片</h6>

              <!-- 上傳圖片區域 -->
              <div class="mb-24 mt-16">
                <div class="avatar-upload">
                  <div class="avatar-edit position-absolute bottom-0 end-0 me-24 mt-16 z-1 cursor-pointer">
                    <input
                      type="file"
                      id="imageUpload"
                      accept=".png, .jpg, .jpeg"
                      @change="onImageChange"
                      hidden
                    />
                    <label
                      for="imageUpload"
                      class="w-32-px h-32-px d-flex justify-content-center align-items-center bg-primary-50 text-primary-600 border border-primary-600 bg-hover-primary-100 text-lg rounded-circle"
                    >
                      <iconify-icon icon="solar:camera-outline" class="icon"></iconify-icon>
                    </label>
                  </div>
                  <div class="avatar-preview">
                    <div
                      v-bind:style="{ backgroundImage: 'url(' + imagePreview + ')' }"
                      id="imagePreview"
                    ></div>
                  </div>
                </div>
              </div>
              <!-- 上傳圖片區域結束 -->

              <form @submit.prevent="submitForm">
                <!-- 照服員姓名 -->
                <div class="mb-20">
                  <label for="chineseName" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    中文姓名 <span class="text-danger-600">*</span>
                  </label>
                  <input
                    v-model="form.chineseName"
                    type="text"
                    class="form-control radius-8"
                    id="chineseName"
                    placeholder="請輸入中文姓名"
                    required
                  />
                </div>

                <!-- 性別 -->
                <div class="mb-20">
                  <label class="form-label fw-semibold text-primary-light text-sm mb-8">
                    性別 <span class="text-danger-600">*</span>
                  </label>
                  <div class="d-flex gap-4">
                    <div class="form-check">
                      <input
                        class="form-check-input"
                        type="radio"
                        name="gender"
                        id="male"
                        :value="true"
                        v-model="form.gender"
                      />
                      <label class="form-check-label" for="male">男性</label>
                    </div>
                    <div class="form-check">
                      <input
                        class="form-check-input"
                        type="radio"
                        name="gender"
                        id="female"
                        :value="false"
                        v-model="form.gender"
                      />
                      <label class="form-check-label" for="female">女性</label>
                    </div>
                  </div>
                </div>

                <!-- 連絡電話 -->
                <div class="mb-20">
                  <label for="phone" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    連絡電話 <span class="text-danger-600">*</span>
                  </label>
                  <input
                    v-model="form.phone"
                    type="text"
                    class="form-control radius-8"
                    id="phone"
                    placeholder="請輸入連絡電話"
                    required
                    @blur="checkPhoneExists"
                  />
                  <small v-if="phoneExists" class="text-danger">此電話號碼已存在，請確認</small>
                </div>

                <!-- 電子郵件 -->
                <div class="mb-20">
                  <label for="email" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    電子信箱 <span class="text-danger-600">*</span>
                  </label>
                  <input
                    v-model="form.email"
                    type="email"
                    class="form-control radius-8"
                    id="email"
                    placeholder="請輸入電子信箱"
                    required
                  />
                </div>

                <!-- 服務年資 -->
                <div class="mb-20">
                  <label for="experienceYears" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    服務年資 <span class="text-danger-600">*</span>
                  </label>
                  <input
                    v-model="form.experienceYears"
                    type="number"
                    min="0"
                    class="form-control radius-8"
                    id="experienceYears"
                    placeholder="請輸入服務年資"
                    required
                  />
                </div>

                <!-- 居住地址 -->
                <div class="mb-20">
                  <label for="address" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    居住地址 <span class="text-danger-600">*</span>
                  </label>
                  <input
                    v-model="form.address"
                    type="text"
                    class="form-control radius-8"
                    id="address"
                    placeholder="請輸入居住地址"
                    required
                  />
                </div>

                <!-- 服務區域 -->
                <div class="mb-20">
                  <label for="serviceArea" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    服務區域 <span class="text-danger-600">*</span>
                  </label>
                  <select v-model="form.serviceArea" class="form-control radius-8 form-select" id="serviceArea" required>
                    <option value="">請選擇服務區域</option>
                    <option v-for="area in serviceAreas" :key="area" :value="area">{{ area }}</option>
                  </select>
                </div>

                <!-- 自我介紹 -->
                <div class="mb-20">
                  <label for="selfIntroduction" class="form-label fw-semibold text-primary-light text-sm mb-8">
                    自我介紹
                  </label>
                  <textarea
                    v-model="form.selfIntroduction"
                    class="form-control radius-8"
                    id="selfIntroduction"
                    placeholder="請輸入個人工作經驗、專業證照、服務特色等介紹..."
                    rows="5"
                    maxlength="1000"
                  ></textarea>
                  <small class="text-muted">
                    {{ form.selfIntroduction?.length || 0 }} / 1000 字
                  </small>
                </div>

                <!-- 測試資料按鈕 -->
                <div class="d-flex align-items-center justify-content-center mb-20">
                  <button
                    type="button"
                    class="btn btn-warning border border-warning-600 text-md px-32 py-8 radius-8"
                    @click="fillTestData"
                  >
                    <iconify-icon icon="solar:test-tube-outline" class="me-2"></iconify-icon>
                    填入測試資料
                  </button>
                </div>

                <!-- 表單按鈕 -->
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
                    :disabled="isSubmitting || phoneExists"
                  >
                    {{ isSubmitting ? '儲存中...' : '儲存' }}
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
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import defaultUserImage from "@/assets/images/user-grid/user-grid-img13.png";
import { caregiverApi } from '@/api/caregiverApi';
import { useCaregivers } from '@/composables/useCaregivers';
import { useToast } from '@/composables/useToast'; // 引入 toast 元件
export default {
  name: 'AddCareWorker',
  setup() {
    const router = useRouter();
    const { createCaregiver } = useCaregivers();
    
    const { showToast } = useToast(); // 使用 Toast 通知功能
    // 表單資料
    const form = reactive({
      chineseName: '',
      gender: false, // 預設女性
      phone: '',
      email: '',
      experienceYears: null,
      selfIntroduction: '',  // 新增自我介紹欄位
      photo: '',
      address: '',
      serviceArea: ''
    });

    // 預設圖片和上傳的檔案
    const imagePreview = ref(defaultUserImage);
    const uploadFile = ref(null);
    const isSubmitting = ref(false);
    const phoneExists = ref(false);

    // 服務區域選項 - 桃園市行政區
    const serviceAreas = [
      '桃園區', '中壢區', '大溪區', '楊梅區', '蘆竹區', '大園區', 
      '龜山區', '八德區', '龍潭區', '平鎮區', '新屋區', '觀音區', '復興區'
    ];

    // 處理圖片上傳
    const onImageChange = (event) => {
      const file = event.target.files[0];
      if (file) {
        uploadFile.value = file;
        const reader = new FileReader();
        reader.onload = (e) => {
          imagePreview.value = e.target.result;
        };
        reader.readAsDataURL(file);
      }
    };

    // 填入測試資料
    const fillTestData = () => {
      // 產生隨機測試資料
      const testNames = ['王美華', '李淑芳', '張惠雯', '陳靜宜', '林雅慧', '劉秀蘭', '黃淑玲', '吳麗雲'];
      const testEmails = ['test1@example.com', 'test2@example.com', 'test3@example.com', 'test4@example.com'];
      const testPhones = ['0912345678', '0923456789', '0934567890', '0945678901'];
      const testAddresses = [
        '桃園市桃園區中正路100號',
        '桃園市中壢區中山路200號',
        '桃園市大溪區和平路300號',
        '桃園市楊梅區文化路400號'
      ];
      const testIntroductions = [
        '您好，我從事照服工作已有多年經驗，擅長長者日常照護、用餐協助和個人衛生清潔。我具備耐心、細心的特質，能提供溫馨的照護服務，讓長者感受到家的溫暖。',
        '我是專業的照服員，具有豐富的失智症照護經驗，能夠提供專業的認知訓練和日常生活協助。我相信每位長者都值得被尊重和關愛。',
        '擁有照服員證照及多年實務經驗，專精於復健協助、移位技巧和生活自理訓練。我致力於維護長者的尊嚴，提供個人化的照護服務。',
        '我熱愛照護工作，具備專業的醫療照護知識和溝通技巧。我能協助長者進行復健運動、藥物管理，並提供情感支持和陪伴。'
      ];

      // 隨機選擇測試資料
      const randomIndex = Math.floor(Math.random() * testNames.length);
      const randomEmailIndex = Math.floor(Math.random() * testEmails.length);
      const randomPhoneIndex = Math.floor(Math.random() * testPhones.length);
      const randomAddressIndex = Math.floor(Math.random() * testAddresses.length);
      const randomIntroIndex = Math.floor(Math.random() * testIntroductions.length);
      const randomGender = Math.random() > 0.5;
      const randomExperience = Math.floor(Math.random() * 10) + 1; // 1-10年經驗
      const randomServiceAreaIndex = Math.floor(Math.random() * serviceAreas.length);

      // 填入表單資料
      form.chineseName = testNames[randomIndex];
      form.gender = randomGender;
      form.phone = testPhones[randomPhoneIndex];
      form.email = testEmails[randomEmailIndex];
      form.experienceYears = randomExperience;
      form.address = testAddresses[randomAddressIndex];
      form.serviceArea = serviceAreas[randomServiceAreaIndex];
      form.selfIntroduction = testIntroductions[randomIntroIndex];

      // 重置電話檢查狀態
      phoneExists.value = false;

      // 顯示成功訊息
      // showToast({
      //   title: '測試資料已填入',
      //   message: '所有欄位已自動填入測試資料',
      //   type: 'info'
      // });
    };

    // 檢查電話是否已存在
    const checkPhoneExists = async () => {
      if (!form.phone) return;
      
      try {
        const response = await caregiverApi.checkPhoneExists(form.phone);
        phoneExists.value = response.data.exists;
      } catch (error) {
        console.error('檢查電話號碼時發生錯誤:', error);
      }
    };

    // 取消表單
    const cancelForm = () => {
      Object.keys(form).forEach(key => {
        if (key === 'gender') {
          form[key] = false;
        } else if (key === 'experienceYears') {
          form[key] = null;
        } else {
          form[key] = '';
        }
      });
      imagePreview.value = defaultUserImage;
      uploadFile.value = null;
      phoneExists.value = false;
      
      // 返回照服員列表頁面
      router.push('/caregiver/list');
    };

    // 提交表單
    const submitForm = async () => {
      isSubmitting.value = true;
      
      try {
        // 先創建照服員
        const newCaregiver = await createCaregiver(form);
        
        // 如果有上傳照片，執行上傳
        if (uploadFile.value && newCaregiver && newCaregiver.id) {
          await caregiverApi.uploadPhoto(newCaregiver.id, uploadFile.value);
        }
        
        // 提示成功並返回列表頁
        showToast({
          title: '新增成功',
          message: `新增照服員: ${newCaregiver.chineseName}`,
          type: 'success'
        });
        router.push('/caregiver/list');
      } catch (error) {
        showToast({
          title: '新增失敗 !',
          message: error.message,
          type: 'error'
        });
      } finally {
        isSubmitting.value = false;
      }
    };

    return {
      form,
      imagePreview,
      serviceAreas,
      phoneExists,
      isSubmitting,
      onImageChange,
      checkPhoneExists,
      cancelForm,
      submitForm,
      fillTestData
    };
  }
};
</script>

<style scoped>
.avatar-upload {
  position: relative;
  max-width: 205px;
  margin: 0 auto;
}

.avatar-preview {
  width: 192px;
  height: 192px;
  position: relative;
  border-radius: 100%;
  overflow: hidden;
  border: 2px solid #ccc;
  margin: 0 auto;
}

.avatar-preview > div {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-repeat: no-repeat;
  background-position: center;
}
</style>
