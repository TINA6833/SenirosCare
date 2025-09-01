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
          <template v-else-if="roomDetail">
            <h4 class="mb-3">{{ roomDetail.name }}</h4>
            <div class="mb-3">
              <img v-if="roomDetail.imagePath" :src="roomDetail.imagePath" :alt="roomDetail.name" class="radius-8"
                style="max-width: 320px; max-height: 200px; object-fit:cover;" />
              <div v-else class="bg-light text-center py-4 radius-8 text-muted">
                <iconify-icon icon="material-symbols:image" class="me-1"></iconify-icon>
                無圖片
              </div>
            </div>
            <ul class="list-group mb-3">
              <li class="list-group-item"><strong>編號：</strong>{{ roomDetail.id }}</li>
              <li class="list-group-item"><strong>價格：</strong>NT$ {{ roomDetail.price }}</li>
              <li class="list-group-item"><strong>人數：</strong>{{ roomDetail.capacity }} 人</li>
              <li class="list-group-item"><strong>描述：</strong>{{ roomDetail.description || '—' }}</li>
              <li class="list-group-item"><strong>管理員備註：</strong>{{ roomDetail.adminNote || '—' }}</li>
              <li class="list-group-item"><strong>狀態：</strong>
                <span :class="roomDetail.isAvailable ? 'text-success' : 'text-secondary'">
                  {{ roomDetail.isAvailable ? '已上架' : '未上架' }}
                </span>
              </li>
              <li class="list-group-item">
                <strong>建立時間：</strong>
                {{ roomDetail.createdAt ? roomDetail.createdAt.slice(0, 10) : '' }}
              </li>
              <li class="list-group-item">
                <strong>更新時間：</strong>
                {{ roomDetail.updatedAt ? roomDetail.updatedAt.slice(0, 10) : '' }}
              </li>
            </ul>
            <div class="text-end">
              <button class="btn btn-secondary" @click="$emit('close')">關閉</button>
            </div>
          </template>
          <template v-else>
            <div class="text-danger">找不到房型資料</div>
          </template>
          <!-- <pre>{{ reservationId }}</pre> -->
          <!-- <pre>{{ reservationDetail }}</pre> -->
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { roomTypeService } from '@/services/roomTypeService';

export default {
  name: 'RoomPreview',
  props: {
    roomId: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      roomDetail: null,
      loading: false,
      error: ''
    };
  },
  async mounted() {
    this.loading = true;
    try {
      this.roomDetail = await roomTypeService.getRoomTypeById(this.roomId);
      console.log('房型詳細資料', this.roomDetail);
      console.log('圖片路徑', this.roomDetail.imagePath);
      console.log('建立時間', this.roomDetail.createdAt);
      console.log('更新時間', this.roomDetail.updatedAt);
      console.log('管理員備註', this.roomDetail.adminNote);
    } catch (e) {
      this.error = e.message;
    }
    this.loading = false;
  }
};
</script>