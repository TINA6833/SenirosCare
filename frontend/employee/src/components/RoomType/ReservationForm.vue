<template>
  <div class="card border">
    <div class="card-body">
      <form @submit.prevent="handleSubmit">
        <!-- 申請人/會員 -->
        <div class="mb-3">
          <label class="form-label">會員ID</label>
          <input v-model="localForm.memberId" type="number" class="form-control" placeholder="會員ID（可留空）" />
        </div>
        <div class="mb-3">
          <label class="form-label">會員姓名</label>
          <input v-model.trim="localForm.memberName" type="text" class="form-control" placeholder="會員姓名" />
        </div>
        <div class="mb-3">
          <label class="form-label">申請人姓名</label>
          <input v-model.trim="localForm.applicantName" type="text" class="form-control" placeholder="申請人姓名（非會員可填）" />
        </div>
        <div class="mb-3">
          <label class="form-label">聯絡電話</label>
          <input v-model.trim="localForm.mainPhone" type="text" class="form-control" placeholder="聯絡電話" />
        </div>

        <!-- 房型 -->
        <div class="mb-3">
          <label class="form-label">房型ID</label>
          <input v-model="localForm.roomTypeId" type="number" class="form-control" placeholder="房型ID（可留空）" />
        </div>
        <div class="mb-3">
          <label class="form-label">房型名稱</label>
          <input v-model.trim="localForm.roomTypeName" type="text" class="form-control" placeholder="房型名稱" />
        </div>
        <div class="mb-3">
          <label class="form-label">房型特徵</label>
          <div>
            <span
              v-for="feature in featureList"
              :key="feature.id"
              class="badge rounded-pill bg-light text-dark me-2 mb-2"
              :class="{ 'bg-primary text-white': selectedFeatures.includes(feature.id) }"
              @click="toggleFeature(feature.id)"
              style="cursor:pointer;"
            >
              {{ feature.name }}
            </span>
          </div>
        </div>

        <!-- 預約需求 -->
        <div class="mb-3">
          <label class="form-label">人數</label>
          <input v-model.number="localForm.partySize" type="number" class="form-control" placeholder="人數" />
        </div>
        <div class="mb-3">
          <label class="form-label">預約日期</label>
          <input v-model="localForm.preferredDate" type="date" class="form-control" />
        </div>
        <div class="mb-3">
          <label class="form-label">開始時間</label>
          <input v-model="localForm.timeFrom" type="time" class="form-control" />
        </div>
        <div class="mb-3">
          <label class="form-label">結束時間</label>
          <input v-model="localForm.timeTo" type="time" class="form-control" />
        </div>
        <div class="mb-3">
          <label class="form-label">狀態</label>
          <select v-model="localForm.status" class="form-select">
            <option :value="0">待審核</option>
            <option :value="1">已預約</option>
            <option :value="2">已取消</option>
            <option :value="3">已完成</option>
          </select>
        </div>
        <div class="mb-3">
          <label class="form-label">備註</label>
          <textarea v-model.trim="localForm.note" class="form-control" rows="2" placeholder="備註"></textarea>
        </div>

        <!-- 系統欄（只顯示，不可編輯） -->
        <div class="mb-3" v-if="localForm.createdAt">
          <label class="form-label">建立時間</label>
          <input :value="localForm.createdAt" type="text" class="form-control" readonly />
        </div>
        <div class="mb-3" v-if="localForm.updatedAt">
          <label class="form-label">更新時間</label>
          <input :value="localForm.updatedAt" type="text" class="form-control" readonly />
        </div>

        <!-- 按鈕區 -->
        <div class="d-flex gap-2 justify-content-end">
          <button type="button" class="btn btn-secondary" @click="handleCancel">取消</button>
          <button type="submit" class="btn btn-primary" :disabled="!isFormValid">儲存</button>
          <button type="button" class="btn btn-outline-secondary" @click="fillTestData">匯入測試資料</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { featureService } from '@/services/featureService';

export default {
  name: 'ReservationForm',
  props: {
    form: {
      type: Object,
      default: () => ({
        reservationId: '',
        memberId: '',
        memberName: '',
        applicantName: '',
        mainPhone: '',
        roomTypeId: '',
        roomTypeName: '',
        partySize: 1,
        preferredDate: '',
        timeFrom: '',
        timeTo: '',
        status: 0,
        note: '',
        createdAt: '',
        updatedAt: ''
      })
    }
  },
  data() {
    return {
      localForm: { ...this.form },
      featureList: [],
      selectedFeatures: []
    };
  },
  computed: {
    isFormValid() {
      return (
        (this.localForm.memberName?.trim() || this.localForm.applicantName?.trim()) &&
        this.localForm.partySize > 0 &&
        this.localForm.preferredDate &&
        this.localForm.timeFrom &&
        this.localForm.timeTo
      );
    }
  },
  watch: {
    form: {
      handler(newForm) {
        this.localForm = { ...newForm };
      },
      deep: true,
      immediate: true
    }
  },
  async mounted() {
    this.featureList = await featureService.getAllFeatures();
    // 若 form.features 有值，初始化已選
    if (this.form.features) {
      this.selectedFeatures = [...this.form.features];
    }
  },
  methods: {
    handleSubmit() {
      const payload = {
        ...this.localForm,
        features: this.selectedFeatures
      };
      this.$emit('submit', payload);
    },
    handleCancel() {
      this.$emit('cancel');
    },
    toggleFeature(id) {
      const idx = this.selectedFeatures.indexOf(id);
      if (idx === -1) {
        this.selectedFeatures.push(id);
      } else {
        this.selectedFeatures.splice(idx, 1);
      }
    },
    fillTestData() {
      this.localForm = {
        reservationId: '',
        memberId: 1001,
        memberName: '王小明',
        applicantName: '',
        mainPhone: '0912345678',
        roomTypeId: 5,
        roomTypeName: '標準雙人房',
        partySize: 2,
        preferredDate: '2025-08-15',
        timeFrom: '14:00',
        timeTo: '16:00',
        status: 1,
        note: '測試預約資料',
        createdAt: '',
        updatedAt: ''
      };
    }
  }
};
</script>