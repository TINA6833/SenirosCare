<template>
  <div class="card h-100 p-0 radius-12">
    <div class="card-body p-24">
      <div class="row justify-content-center">
        <div class="col-xxl-6 col-xl-8 col-lg-10">
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
import { useToast } from '@/composables/useToast';
import { useConfirmDialog } from '@/composables/useConfirmDialog';

export default {
  name: 'RoomEdit',
  components: { RoomTypeForm },
  props: {
    roomId: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      form: {
        name: '',
        price: '',
        capacity: '',
        description: '',
        adminNote: '',
        isAvailable: false,
        createdAt: '',
        updatedAt: '',
        imagePath: ''
      },
      loading: false,
      toastApi: useToast(),
      confirmApi: useConfirmDialog()
    };
  },
  async mounted() {
    if (this.roomId) {
      const data = await roomTypeService.getRoomTypeById(this.roomId);
      const baseUrl = 'http://localhost:8080';
      this.form = {
        ...data,
        id: data.id,
        imagePath: data.image_path ? baseUrl + data.image_path : ''
      };
    }
  },
  methods: {
    async handleSubmit(newForm) {
      if (!newForm.id) {
       
        return;
      }
      try {
        const formData = new FormData();
        formData.append('id', newForm.id);
        formData.append('name', newForm.name);
        formData.append('price', newForm.price);
        formData.append('capacity', newForm.capacity);
        formData.append('description', newForm.description || '');
        formData.append('imagePath', newForm.imagePath || '');
        formData.append('isAvailable', newForm.isAvailable ? 1 : 0);
        formData.append('adminNote', newForm.adminNote || '');
        if (newForm.imageFile) {
          formData.append('image', newForm.imageFile);
        }
        await roomTypeService.patchRoomTypeWithImage(newForm.id, formData);
        this.toastApi.showToast({
          title: '儲存成功',
          message: '房型資料已成功更新！',
          type: 'success'
        });
        this.$emit('success');
      } catch (e) {
        this.toastApi.showToast({
          title: '儲存失敗',
          message: e.message || '房型資料更新失敗！',
          type: 'error'
        });
      }
    },
    async submitForm() {
      // 範例：儲存前詢問使用者是否確認儲存
      const confirmed = await this.confirmApi.showConfirmDialog({
        title: '儲存房型',
        message: '確定要儲存房型資料嗎？',
        type: 'info',
        confirmText: '確定',
        cancelText: '取消'
      }).catch(() => false);
      if (!confirmed) return;
      try {
        await roomTypeApi.patchRoomType(this.form.id, this.form);
        await roomTypeApi.replaceRoomTypeFeatures(this.form.id, this.selectedFeatures);
       
      } catch (e) {
        this.toastApi.showToast({
          title: '儲存失敗',
          message: e.message || '房型資料更新失敗！',
          type: 'error'
        });
      }
    },
    handleCancel() {
      this.$emit('close');
    },
    handleImageChange({ file, url }) {
      this.form.imageFile = file;
      this.form.imagePath = url;
    }
  }
};
</script>