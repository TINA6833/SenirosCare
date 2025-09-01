<template>
  <div class="card h-100 p-0 radius-12">
    <div class="card-body p-24">
      <div class="row justify-content-center">
        <div class="col-xxl-6 col-xl-8 col-lg-10">
          <div class="d-flex gap-2 mb-3 justify-content-end">
            <!-- 加入測試資料按鈕 -->
            <button type="button" class="btn btn-outline-secondary" @click="importTestData">
              匯入測試資料
            </button>
          </div>
          <!-- 使用 RoomTypeForm 組件 -->
          <RoomTypeForm :form="form" @submit="handleSubmit" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { roomTypeService } from '@/services/roomTypeService';
import RoomTypeForm from '@/components/RoomType/RoomTypeForm.vue';

export default {
  name: 'RoomAdd',
  components: {
    RoomTypeForm
  },
  data() {
    return {
      form: {
        name: '',           // 房型名稱
        price: '',          // 價格
        capacity: '',       // 容納人數
        description: '',    // 房型描述
        features: '',       // 房內設施
        adminNote: '',      // 管理員備註
        isAvailable: false, // 是否上架
        createdAt: '',      // 建立時間
        updatedAt: '',      // 更新時間
        imagePath: '',      // 圖片預覽
        imageFile: null     // 圖片檔案
      },
      loading: false
    };
  },
  methods: {
    async handleSubmit(newForm) {
      if (this.loading) return;
      this.loading = true;
      // 送出表單，不顯示任何提示框或 toast
      const formData = new FormData();
      formData.append('name', newForm.name);
      formData.append('price', newForm.price);
      formData.append('capacity', newForm.capacity);
      formData.append('description', newForm.description || '');
      formData.append('features', newForm.features || '');
      formData.append('adminNote', newForm.adminNote || '');
      formData.append('isAvailable', newForm.isAvailable ? 1 : 0);
      if (newForm.imageFile) {
        formData.append('image', newForm.imageFile);
      }
      if (!newForm.imageFile && newForm.imagePath) {
        formData.append('imagePath', newForm.imagePath);
      }
      await roomTypeService.addRoomType(formData);
      this.$emit('success');
      this.loading = false;
    },
    handleCancel() {
      this.$emit('close');
    },
    handleImageChange({ file, url }) {
      this.form.imageFile = file;
      this.form.imagePath = url;
    },
    importTestData() {
      // 匯入測試資料，不顯示任何提示框或 toast
      this.form = {
        name: '豪華雙人房',
        price: '3200',
        capacity: '2',
        description: '舒適寬敞的豪華雙人房，附有獨立衛浴與景觀窗。',
        features: 'WiFi, 電視, 冷氣, 吹風機',
        adminNote: '此為測試房型，請勿刪除。',
        isAvailable: true,
        createdAt: '',
        updatedAt: '',
        imagePath: 'https://localhost:8000/images/roomImg/room6.png',
        imageFile: null
      };
    }
  }
};
</script>