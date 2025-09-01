<template>
  <div class="card h-100 p-0 radius-12">
    <div class="card-body p-24">
      <div class="row justify-content-center">
        <div class="col-xxl-8 col-xl-10 col-lg-12">
          <template v-if="loading">
            <div class="text-center py-5">
              <div class="spinner-border text-primary"></div>
              <div class="mt-3">載入中...</div>
            </div>
          </template>
          <template v-else-if="facilityDetail">
            <h4 class="mb-3">{{ facilityDetail.name }}</h4>
            <div class="mb-3">
              <img v-if="facilityDetail.imagePath" :src="facilityDetail.imagePath" :alt="facilityDetail.name" class="radius-8"
                style="max-width: 320px; max-height: 200px; object-fit:cover;" />
              <div v-else class="bg-light text-center py-4 radius-8 text-muted">
                <iconify-icon icon="material-symbols:image" class="me-1"></iconify-icon>
                無圖片
              </div>
            </div>
            <ul class="list-group mb-3">
              <li class="list-group-item"><strong>編號：</strong>{{ facilityDetail.id }}</li>
              <li class="list-group-item"><strong>描述：</strong>{{ facilityDetail.description || '—' }}</li>
              <li class="list-group-item"><strong>狀態：</strong>
                <span :class="facilityDetail.isAvailable ? 'text-success' : 'text-secondary'">
                  {{ facilityDetail.isAvailable ? '已上架' : '未上架' }}
                </span>
              </li>
              <li class="list-group-item"><strong>建立時間：</strong>{{ facilityDetail.createdAt || '—' }}</li>
              <li class="list-group-item"><strong>更新時間：</strong>{{ facilityDetail.updatedAt || '—' }}</li>
            </ul>
            <div class="text-end">
              <button class="btn btn-secondary" @click="$emit('close')">關閉</button>
            </div>
          </template>
          <template v-else>
            <div class="text-danger">找不到設施資料</div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// 重點註解：引入 useToast，錯誤時用 toast 顯示
import { facilityService } from '@/services/facilityService';
import { useToast } from '@/composables/useToast';

export default {
  name: 'FacilityPreview',
  props: {
    facilityId: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      facilityDetail: null,
      loading: false,
      error: '',
      toastApi: useToast() // 重點註解：toast 實例
    };
  },
  async mounted() {
    // 重點註解：載入設施資料失敗時用 toast 顯示
    this.loading = true;
    try {
      this.facilityDetail = await facilityService.getFacilityById(this.facilityId);
    } catch (e) {
      this.error = e.message;
      this.toastApi.showToast({
        title: '載入失敗',
        message: e.message || '無法取得設施資料',
        type: 'error'
      });
    }
    this.loading = false;
  },
  methods: {
    openPreviewModal(facilityId) {
      this.selectedFacilityId = facilityId;
      // ...modal顯示...
    }
  }
};
</script>