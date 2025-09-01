<template>
  <div class="card h-100 p-0 radius-12">
    <div class="card-body p-24">
      <div class="row justify-content-center">
        <div class="col-xxl-6 col-xl-8 col-lg-10">
          <!-- 預約表單 -->
          <form @submit.prevent="handleSubmit">
            <div class="mb-3">
              <label class="form-label">申請人</label>
              <input v-model="form.applicantName" type="text" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label">電話</label>
              <input v-model="form.mainPhone" type="text" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label">房型</label>
              <select v-model="form.roomTypeId" class="form-select" required>
                <option v-for="room in roomTypeList" :key="room.id" :value="room.id">
                  {{ room.name }}
                </option>
              </select>
            </div>
            <div class="mb-3">
              <label class="form-label">人數</label>
              <input v-model.number="form.partySize" type="number" min="1" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label">預約日期</label>
              <input v-model="form.preferredDate" type="date" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label">開始時間</label>
              <input v-model="form.timeFrom" type="time" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label">結束時間</label>
              <input v-model="form.timeTo" type="time" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label">備註</label>
              <textarea v-model="form.note" class="form-control"></textarea>
            </div>
            <div class="mb-3 text-end">
              <button type="button" class="btn btn-secondary me-2" @click="handleCancel">取消</button>
              <button type="submit" class="btn btn-primary">儲存</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// 重點註解：修改成功後跳提示框
import { reservationService } from '@/services/reservationService';
import { roomTypeService } from '@/services/roomTypeService';
import { useToast } from '@/composables/useToast';

export default {
  name: 'ReservationEdit',
  props: {
    reservationId: {
      type: [String, Number],
      required: false
    }
  },
  data() {
    return {
      form: {
        applicantName: '',
        mainPhone: '',
        roomTypeId: '',
        partySize: 1,
        preferredDate: '',
        timeFrom: '',
        timeTo: '',
        note: ''
      },
      roomTypeList: [],
      loading: false,
      toastApi: useToast()
    };
  },
  async mounted() {
    this.roomTypeList = await roomTypeService.getAllRoomTypes();
    if (this.reservationId) {
      const data = await reservationService.getReservationById(this.reservationId);
      this.form = {
        applicantName: data.applicantName || '',
        mainPhone: data.mainPhone || '',
        roomTypeId: data.roomTypeId || '',
        partySize: data.partySize || 1,
        preferredDate: data.preferredDate || '',
        timeFrom: data.timeFrom || '',
        timeTo: data.timeTo || '',
        note: data.note || ''
      };
    }
  },
  methods: {
    async handleSubmit() {
      try {
        if (this.reservationId) {
          await reservationService.patchReservation(this.reservationId, this.form);
          // 修改成功跳提示框
          this.toastApi.showToast({
            title: '修改成功',
            message: '預約已成功修改！',
            type: 'success'
          });
        } else {
          await reservationService.createReservation(this.form);
          // 新增成功跳提示框
          this.toastApi.showToast({
            title: '新增成功',
            message: '預約已成功新增！',
            type: 'success'
          });
        }
        this.$emit('success');
      } catch (e) {
        this.toastApi.showToast({
          title: '儲存失敗',
          message: e.message || String(e),
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