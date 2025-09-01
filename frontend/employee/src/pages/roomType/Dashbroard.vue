<template>
  <div class="container py-4">
    <div class="row">
      <!-- 上半部均分兩張卡片 -->
      <div class="col-md-6 mb-4">
        <div class="card h-100 p-3">
          <h5 class="mb-3" style="font-size:1.25rem;"></h5>
          <!-- 動態資料防呆，資料有才顯示 -->
          <ColumnOne v-if="roomTypeBookingData.labels && roomTypeBookingData.labels.length" :chart-data="roomTypeBookingData" />
          <div v-else class="text-center text-muted py-5">暫無資料</div>
        </div>
      </div>
      <div class="col-md-6 mb-4">
        <div class="card h-100 p-3">
          <h5 class="mb-3" style="font-size:1.25rem;"></h5>
          <DonutChart v-if="roomTypePieData.labels && roomTypePieData.labels.length" :chart-data="roomTypePieData" />
          <div v-else class="text-center text-muted py-5">暫無資料</div>
        </div>
      </div>
    </div>
    <!-- 下半部留給折線圖 -->
    <div class="row">
      <div class="col-12 mb-4">
        <AnimationChart />
      </div>
    </div>
  </div>
</template>

<script>
import reservationApi from '@/api/reservationApi';
import ColumnOne from '@/components/RoomType/ColumnOne.vue';
import DonutChart from '@/components/RoomType/DonutChart.vue';
import AnimationChart from '@/components/RoomType/AnimationChart.vue';

export default {
  name: 'Dashbroard',
  components: { ColumnOne, DonutChart, AnimationChart },
  data() {
    return {
      roomTypeBookingData: {
        labels: [],
        datasets: []
      },
      roomTypePieData: {
        labels: [],
        series: []
      },
      chartType: 'monthly'
    };
  },
  async mounted() {
    try {
      const rankingRes = await reservationApi.getRoomTypeRanking(10);
      let arr = [];
      if (Array.isArray(rankingRes.data)) {
        arr = rankingRes.data;
      } else if (Array.isArray(rankingRes.data?.data)) {
        arr = rankingRes.data.data;
      }
      if (arr.length) {
        this.roomTypeBookingData = {
          labels: arr.map(r => r.roomTypeName),
          datasets: [
            {
              label: '預約次數',
              data: arr.map(r => r.count)
            }
          ]
        };
        this.roomTypePieData = {
          labels: arr.map(r => r.roomTypeName),
          series: arr.map(r => r.count)
        };
      }
    } catch (e) {
      console.error('取得預約排行失敗', e);
      this.roomTypeBookingData = { labels: [], datasets: [] };
      this.roomTypePieData = { labels: [], series: [] };
    }
  }
};
</script>