<template>
  <div class="col-12 h-100"> <!-- 改為 col-12 讓寬度滿足外部卡片 -->
    <div class="card h-100 p-0">
      <div class="card-header border-bottom bg-base py-16 px-24">
        <!-- 標題字體統一 -->
        <h6 class="text-lg fw-semibold mb-0" style="font-size:1.25rem;">各房型預約次數</h6>
      </div>
      <!-- card-body 背景透明，寬高佔滿 -->
      <div class="card-body p-24 h-100" style="background:transparent;">
        <div class="position-relative w-100 h-100">
          <div id="columnChart" class="w-100 h-100"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ApexCharts from 'apexcharts';

export default {
  name: 'ColumnOne',
  props: {
    chartData: {
      type: Object,
      required: true
    }
  },
  mounted() {
    this.renderChart();
  },
  watch: {
    chartData: {
      handler() {
        this.renderChart();
      },
      deep: true
    }
  },
  methods: {
    renderChart() {
      const categories = this.chartData.labels || [];
      const series = this.chartData.datasets
        ? this.chartData.datasets.map(ds => ({
            name: ds.label,
            data: ds.data
          }))
        : [];

      const options = {
        series,
        colors: ['#487FFF', '#FF9F29'],
        chart: {
          type: 'bar',
          height: 400,
          toolbar: { show: false },
          background: 'transparent'
        },
        grid: {
          show: true,
          borderColor: '#D1D5DB',
          strokeDashArray: 4,
          position: 'back'
        },
        plotOptions: {
          bar: {
            borderRadius: 4,
            columnWidth: '70%'
          }
        },
        dataLabels: { enabled: false },
        stroke: {
          show: true,
          width: 2,
          colors: ['transparent']
        },
        xaxis: {
          categories,
          title: {
            text: '' // 移除x軸標題
          },
          labels: {
            style: { fontSize: "14px", color: "#222" }
          }
        },
        yaxis: {
          title: {
            text: '' // 移除y軸標題
          },
          labels: {
            formatter: value => value,
            style: { fontSize: "14px", color: "#222" }
          }
        },
        tooltip: {
          y: {
            formatter: value => value
          }
        },
        fill: {
          opacity: 1
        }
      };

      if (this.chart) {
        this.chart.destroy();
      }
      this.chart = new ApexCharts(this.$el.querySelector("#columnChart"), options);
      this.chart.render();
    }
  },
  beforeUnmount() {
    if (this.chart) {
      this.chart.destroy();
    }
  }
};
</script>
