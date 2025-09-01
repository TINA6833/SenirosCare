<template>
  <div class="col-md-12">
    <div class="card h-100 p-0">
      <div class="card-header border-bottom bg-base py-16 px-24">
        <h6 class="text-lg fw-semibold mb-0">本年預約次數</h6>
      </div>
      <div class="card-body p-24" style="padding-left:40px;">
        <div id="monthlyLineChart"></div>
      </div>
    </div>
  </div>
</template>

<script>
import ApexCharts from 'apexcharts';
import reservationApi from '@/api/reservationApi';

export default {
  name: 'AnimationChart',
  props: {
    chartType: {
      type: String,
      default: 'monthly'
    }
  },
  async mounted() {
    const allMonths = Array.from({ length: 12 }, (_, i) => `2025-${String(i + 1).padStart(2, '0')}`);
    let monthMap = {};
    try {
      const res = await reservationApi.getMonthlyReservationStatsByDate();
      const arr = Array.isArray(res.data) ? res.data : res.data?.data || [];
      arr.forEach(item => {
        const ym = item.date.slice(0, 7);
        monthMap[ym] = item.count;
      });
    } catch (e) {
      allMonths.forEach(m => { monthMap[m] = 0; });
    }
    const months = allMonths;
    const counts = months.map(m => monthMap[m] || 0);

    this.createLineChart('monthlyLineChart', '#487fff', months, counts);
  },
  methods: {
    createLineChart(chartId, chartColor, months, counts) {
      const options = {
        series: [
          {
            name: '', // 移除預約次數
            data: counts,
          }
        ],
        chart: {
          type: 'line',
          height: 320,
          toolbar: { show: false }
        },
        colors: [chartColor],
        dataLabels: { enabled: false },
        stroke: {
          curve: 'smooth',
          width: 4,
          colors: [chartColor],
          lineCap: 'round',
        },
        grid: {
          show: true,
          borderColor: '#D1D5DB',
          strokeDashArray: 3,
          position: 'back'
        },
        xaxis: {
          categories: months,
          title: {
            text: '' // 移除x軸標題
          },
          labels: {
            style: { fontSize: "14px" }
          }
        },
        yaxis: {
          title: {
            text: '' // 移除y軸標題
          },
          labels: {
            style: { fontSize: "14px" }
          },
          min: 0
        },
        tooltip: {
          x: { format: 'yyyy-MM' }
        },
        legend: { show: false }
      };

      const chart = new ApexCharts(document.querySelector(`#${chartId}`), options);
      chart.render();
    }
  }
}
</script>

// reservationApi.js
import axios from 'axios';

const instance = axios.create({
baseURL: 'http://localhost:8080'
});

export default {
getMonthlyReservationStatsByDate() {
return instance.get('/api/reservations/analytics/monthly');
}
// ...其他方法...
};
