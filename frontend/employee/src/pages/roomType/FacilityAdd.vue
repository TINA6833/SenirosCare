<template>
  <div class="card h-100 p-0 radius-12">
    <div class="card-body p-24">
      <div class="row justify-content-center">
        <div class="col-xxl-6 col-xl-8 col-lg-10">
          <div class="d-flex gap-2 mb-3 justify-content-end">
            <button type="button" class="btn btn-outline-secondary" @click="importTestData">
              匯入測試資料
            </button>
          </div>
          <!-- 使用 FacilityForm 組件 -->
          <FacilityForm :form="form" @submit="handleSubmit" @cancel="handleCancel" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { facilityService } from '@/services/facilityService';
import FacilityForm from '@/components/RoomType/FacilityForm.vue';
import FacilityEdit from './FacilityEdit.vue';
import FacilityPreview from './FacilityPreview.vue';
import { useToast } from '@/composables/useToast';

export default {
  name: 'FacilityAdd',
  components: {
    FacilityForm
  },
  data() {
    return {
      form: {
        name: '',
        description: '',
        isAvailable: false,
        imagePath: '',
        imageFile: null
      },
      loading: false,
      toastApi: useToast()
    };
  },
  methods: {
    async handleSubmit(newForm) {
      if (this.loading) return;
      this.loading = true;
      try {
        const formData = new FormData();
        formData.append('name', newForm.name);
        formData.append('description', newForm.description || '');
        formData.append('isAvailable', newForm.isAvailable ? 1 : 0);
        if (newForm.imageFile) {
          formData.append('image', newForm.imageFile);
        }
        if (!newForm.imageFile && newForm.imagePath) {
          formData.append('imagePath', newForm.imagePath);
        }
        await facilityService.addFacility(formData);
        // 新增成功後，通知父元件刷新資料並排序
        this.$emit('success');
      } catch (e) {
        this.toastApi.showToast({
          title: '新增失敗',
          message: e.message || '伺服器錯誤',
          type: 'error'
        });
      } finally {
        this.loading = false;
      }
    },
    handleCancel() {
      this.$emit('close');
    },
    importTestData() {
      this.form = {
        name: '測試設施',
        description: '這是測試用設施描述',
        isAvailable: false,
        imagePath: 'http://localhost:8080/images/roomImg/test-facility.png',
      };
    }
  }
};
</script>