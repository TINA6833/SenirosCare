<template>
  <div class="col-12 h-100">
    <div class="card h-100 p-0">
      <div class="card-header border-bottom bg-base py-16 px-24">
        <h6 class="text-lg fw-semibold mb-0" style="font-size:1.25rem;">各房型預約比例</h6>
      </div>
      <div class="card-body p-24 h-100 text-center d-flex flex-wrap align-items-start gap-5 justify-content-center" style="background:transparent;">
        <div class="position-relative w-100 h-100">
          <div v-if="chartData.series && chartData.series.length"
               id="basicDonutChart"
               class="w-100 h-100 d-inline-block apexcharts-tooltip-z-none"></div>
          <div v-else class="text-muted py-5">暫無資料</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ApexCharts from "apexcharts";

export default {
  name: "DonutChart",
  props: {
    chartData: {
      type: Object,
      required: true
    },
    isDark: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    totalValue() {
      return (this.chartData.series || []).reduce((a, b) => a + b, 0);
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
    },
    isDark() {
      this.renderChart();
    }
  },
  methods: {
    renderChart() {
      if (!this.chartData.series || !this.chartData.series.length) return;

      const options = {
        series: this.chartData.series || [],
        labels: this.chartData.labels || [],
        chart: {
          height: 340,
          width: '100%',
          type: 'donut',
          background: 'transparent'
        },
        colors: ['#16a34a', '#487fff', '#2563eb', '#dc2626', '#f86624', '#ffc107'],
        dataLabels: {
          enabled: true,
          formatter: function (val, opts) {
            return `${val.toFixed(1)}%`;
          },
          style: {
            fontSize: '14px',
            fontWeight: 'bold'
          }
        },
        legend: {
          position: 'bottom',
          offsetY: 16,
          show: true,
          labels: {
            colors: '#222',
            useSeriesColors: false,
            fontSize: '18px',
            fontWeight: 700
          },
          formatter: function(seriesName, opts) {
            const value = opts.w.globals.series[opts.seriesIndex];
            const total = opts.w.globals.series.reduce((a, b) => a + b, 0);
            const percent = total ? ((value / total) * 100).toFixed(1) : 0;
            return `${seriesName} (${percent}%)`;
          }
        },
        plotOptions: {
          pie: {
            donut: {
              labels: {
                show: true,
                total: {
                  show: true,
                  label: '總計',
                  fontSize: '22px',
                  fontWeight: 700,
                  color: this.isDark ? '#fff' : '#222', // 夜間模式顯示白色
                  formatter: () => this.totalValue
                }
              }
            }
          }
        }
      };

      if (this.chart) {
        this.chart.destroy();
      }
      this.chart = new ApexCharts(this.$el.querySelector("#basicDonutChart"), options);
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

<DonutChart :chartData="chartData" :isDark="isDarkMode" />