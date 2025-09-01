<template>
  <div class="card h-100 p-0 radius-12">
    <div class="card-body p-24">
      <div class="row justify-content-center">
        <div class="col-xxl-6 col-xl-8 col-lg-10">
          <!-- 預約新增表單 -->
          <form @submit.prevent="handleSubmit">
            <div class="mb-3">
              <label class="form-label">
                申請人
                <span class="text-danger" title="此欄位必填">*</span>
              </label>
              <input v-model="form.applicantName" type="text" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label">
                電話
                <span class="text-danger" title="此欄位必填">*</span>
              </label>
              <input v-model="form.mainPhone" type="text" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label">
                房型
                <span class="text-danger" title="此欄位必填">*</span>
              </label>
              <select v-model="form.roomTypeId" class="form-select" required>
                <option value="" disabled>請選擇房型</option>
                <option v-for="room in roomTypeList" :key="room.id" :value="room.id">
                  {{ room.name }}
                </option>
              </select>
            </div>
            <div class="mb-3">
              <label class="form-label">
                人數
                <span class="text-danger" title="此欄位必填">*</span>
              </label>
              <input v-model.number="form.partySize" type="number" min="1" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label">
                預約日期
                <span class="text-danger" title="此欄位必填">*</span>
              </label>
              <input
                v-model="form.preferredDate"
                type="date"
                class="form-control"
                required
                placeholder="2025/04/09"
              />
            </div>
            <div class="mb-3">
              <label class="form-label">
                開始時間
                <span class="text-danger" title="此欄位必填">*</span>
              </label>
              <input v-model="form.timeFrom" type="time" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label">
                結束時間
                <span class="text-danger" title="此欄位必填">*</span>
              </label>
              <input v-model="form.timeTo" type="time" class="form-control" required />
            </div>
            <div class="mb-3">
              <label class="form-label">備註</label>
              <textarea v-model="form.note" class="form-control"></textarea>
            </div>
            <div class="mb-3 text-end">
              <button type="button" class="btn btn-info me-2" @click="fillTestData">測試資料</button>
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
// 重點註解：送出新增預約表成功時跳提示框
import { reservationService } from '@/services/reservationService';
import { roomTypeService } from '@/services/roomTypeService';
import { useToast } from '@/composables/useToast';

export default {
  name: 'ReservationAdd',
  data() {
    return {
      form: {
        applicantName: '',
        mainPhone: '',
        roomTypeId: '',
        partySize: 1,
        preferredDate: new Date().toISOString().slice(0, 10),
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
  },
  methods: {
    async handleSubmit() {
      try {
        await reservationService.createReservation(this.form);
        // 新增成功時跳提示框
        this.toastApi.showToast({
          title: '新增成功',
          message: '預約已成功新增！',
          type: 'success'
        });
        this.$emit('success');
      } catch (e) {
        this.toastApi.showToast({
          title: '新增失敗',
          message: e.message || String(e),
          type: 'error'
        });
      }
    },
    handleCancel() {
      this.$emit('close');
    },
    fillTestData() {
      this.form.applicantName = '王小明';
      this.form.mainPhone = '0912345678';
      this.form.roomTypeId = this.roomTypeList.length ? this.roomTypeList[0].id : '';
      this.form.partySize = 2;
      this.form.preferredDate = new Date().toISOString().slice(0, 10);
      this.form.timeFrom = '10:00';
      this.form.timeTo = '11:00';
      this.form.note = '測試預約資料';
    }
  }
};
</script>