<template>
  <form @submit.prevent="handleSubmit">
    <!-- 房型名稱 -->
    <div class="mb-3">
      <label class="form-label">房型名稱 *</label>
      <input v-model="localForm.name" type="text" class="form-control" required />
    </div>

    <!-- 價格 -->
    <div class="mb-3">
      <label class="form-label">價格 *</label>
      <input v-model.number="localForm.price" type="number" class="form-control" required min="1" />
    </div>

    <!-- 容納人數 -->
    <div class="mb-3">
      <label class="form-label">容納人數 *</label>
      <input v-model.number="localForm.capacity" type="number" class="form-control" required min="1" />
    </div>

    <!-- 房型描述 -->
    <div class="mb-3">
      <label class="form-label">房型描述</label>
      <textarea v-model="localForm.description" class="form-control"></textarea>
    </div>

    <!-- 管理員備註 -->
    <div class="mb-3">
      <label class="form-label">管理員備註</label>
      <input v-model="localForm.adminNote" type="text" class="form-control" />
    </div>

    <!-- 是否啟用 -->
    <div class="mb-3">
      <label class="form-label">是否啟用</label>
      <input type="checkbox" v-model="localForm.isAvailable" />
    </div>

    <!-- 圖片上傳 -->
    <div class="mb-3">
      <label class="form-label">房型圖片</label>
      <input type="file" @change="onFileChange" />
      <img v-if="localForm.imagePath" :src="localForm.imagePath" style="max-width:200px;margin-top:8px" />
    </div>

    <div class="mt-3 d-flex gap-2">
      <!-- 取消鍵：通知父元件取消操作 -->
      <button type="button" class="btn btn-outline-secondary" @click="handleCancel">取消</button>
      <!-- 儲存鍵 -->
      <button type="submit" class="btn btn-primary">儲存</button>
    </div>
  </form>
</template>

<script>
export default {
  name: 'RoomTypeForm',
  props: {
    // 父元件傳入的 reactive 物件
    form: { type: Object, required: true }
  },
  data() {
    return {
      // localForm 為表單操作用的本地複本，透過 watch 與父層同步
      localForm: Object.assign({}, this.form)
    };
  },
  watch: {
    // 父層 form 更新時同步到 localForm（避免更換 reference）
    form: {
      handler(newVal) {
        if (!newVal) return;
        Object.assign(this.localForm, newVal);
      },
      deep: true,
      immediate: true
    },
    // localForm 更新時發出 snapshot 給父層（父層負責合併到真正的 form）
    localForm: {
      handler(newVal) {
        this.$emit('update:form', Object.assign({}, newVal));
      },
      deep: true
    }
  },
  methods: {
    // 送出時把 snapshot 傳回父元件
    handleSubmit() {
      this.$emit('submit', Object.assign({}, this.localForm));
    },
    // 取消按鈕：通知父元件（父可處理導頁或清空）
    handleCancel() {
      this.$emit('cancel');
    },
    // 圖片變更：更新 localForm 並通知父層
    onFileChange(e) {
      const file = e.target.files && e.target.files[0];
      if (!file) return;
      this.localForm.imageFile = file;
      this.localForm.imagePath = URL.createObjectURL(file);
      this.$emit('image-change', { file, url: this.localForm.imagePath });
    }
  }
};
</script>