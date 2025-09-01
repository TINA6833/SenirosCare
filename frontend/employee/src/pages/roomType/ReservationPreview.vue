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
          <template v-else-if="reservationDetail">
            <h4 class="mb-3">預約詳細資料</h4>
            <ul class="list-group mb-3">
              <li class="list-group-item"><strong>編號：</strong>{{ reservationDetail.reservationId }}</li>
              <li class="list-group-item"><strong>申請人：</strong>{{ reservationDetail.applicantName }}</li>
              <li class="list-group-item"><strong>電話：</strong>{{ reservationDetail.mainPhone }}</li>
              <li class="list-group-item"><strong>房型：</strong>{{ getRoomTypeName(reservationDetail.roomTypeId) }}</li>
              <li class="list-group-item"><strong>人數：</strong>{{ reservationDetail.partySize }}</li>
              <li class="list-group-item"><strong>預約日期：</strong>{{ reservationDetail.preferredDate }}</li>
              <li class="list-group-item"><strong>時間：</strong>{{ reservationDetail.timeFrom }} ~ {{ reservationDetail.timeTo }}</li>
              <li class="list-group-item"><strong>狀態：</strong>
                <span :class="statusClass(reservationDetail.status)">
                  {{ statusText(reservationDetail.status) }}
                </span>
              </li>
              <li class="list-group-item"><strong>備註：</strong>{{ reservationDetail.note || '—' }}</li>
              <li class="list-group-item"><strong>建立時間：</strong>
                {{ formatTime(reservationDetail.createdAt) || '—' }}
              </li>
              <li class="list-group-item"><strong>更新時間：</strong>
                {{ formatTime(reservationDetail.updatedAt) || '—' }}
              </li>
            </ul>
            <div class="text-end">
              <button class="btn btn-secondary" @click="$emit('close')">關閉</button>
            </div>
          </template>
          <template v-else>
            <div class="text-danger">找不到預約資料</div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reservationService } from '@/services/reservationService';
import { roomTypeService } from '@/services/roomTypeService'; // 需有此 service

export default {
  name: 'ReservationPreview',
  props: {
    reservationId: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      reservationDetail: null,
      loading: false,
      error: '',
      roomTypeList: []
    };
  },
  async mounted() {
    this.loading = true;
    try {
      // 取得預約詳細資料
      this.reservationDetail = await reservationService.getReservationById(this.reservationId);
      // 動態取得房型資料
      this.roomTypeList = await roomTypeService.getAllRoomTypes();
    } catch (e) {
      this.error = e.message;
    }
    this.loading = false;
  },
  methods: {
    getRoomTypeName(roomTypeId) {
      const room = this.roomTypeList.find(r => r.id === roomTypeId);
      return room ? room.name : '-';
    },
    statusText(status) {
      switch (Number(status)) {
        case 0: return '待審核';
        case 1: return '已預約';
        case 2: return '已取消';
        case 3: return '已完成';
        default: return '未知';
      }
    },
    statusClass(status) {
      switch (Number(status)) {
        case 1: return 'text-success fw-bold';
        case 2: return 'text-danger fw-bold';
        case 3: return 'text-primary fw-bold';
        default: return 'text-secondary';
      }
    },
    formatTime(timestamp) {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    }
  }
};
</script>