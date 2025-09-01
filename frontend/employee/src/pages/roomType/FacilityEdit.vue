<template>
  <div class="card h-100 p-0 radius-12">
    <div class="card-body p-24">
      <div class="row justify-content-center">
        <div class="col-xxl-6 col-xl-8 col-lg-10">
          <!-- 使用 FacilityForm 組件 -->
          <FacilityForm :form="form" @submit="handleSubmit" @cancel="handleCancel" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// 重點註解：按儲存直接修改，不再跳 ConfirmDialog，只保留 Toast 成功/失敗提示
import { facilityService } from '@/services/facilityService';
import FacilityForm from '@/components/RoomType/FacilityForm.vue';
import { useToast } from '@/composables/useToast';

export default {
  name: 'FacilityEdit',
  components: { FacilityForm },
  props: {
    facilityId: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      form: {
        id: '',
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
  async mounted() {
    if (this.facilityId) {
      const data = await facilityService.getFacilityById(this.facilityId);
      this.form = { ...data, imageFile: null };
    }
  },
  methods: {
    async handleSubmit(newForm) {
      if (!newForm.id) return;
      const formData = new FormData();
      formData.append('id', newForm.id);
      formData.append('name', newForm.name);
      formData.append('description', newForm.description || '');
      formData.append('isAvailable', newForm.isAvailable ? 1 : 0);
      formData.append('imagePath', newForm.imagePath || '');
      if (newForm.imageFile) {
        formData.append('image', newForm.imageFile);
      }
      try {
        const res = await facilityService.patchFacilityWithImage(newForm.id, formData);
        if (res && (res.status === 200 || res.status === 201)) {
          this.toastApi.showToast({
            title: '修改成功',
            message: '設施資料已成功更新！',
            type: 'success'
          });
          // 重新取得設施資料並排序
          if (this.$parent && typeof this.$parent.refreshFacilities === 'function') {
            await this.$parent.refreshFacilities();
          }
          // emit success/close
          this.$emit('success');
          this.$emit('close');
        } else {
          throw new Error('伺服器回應失敗');
        }
      } catch (e) {
        this.toastApi.showToast({
          title: '修改失敗',
          message: e.message || '修改設施失敗，請稍後再試。',
          type: 'error'
        });
      }
    },
    handleCancel() {
      this.$emit('close');
    }
  }
};
</script>

<FacilityEdit :facilityId="selectedFacilityId" />