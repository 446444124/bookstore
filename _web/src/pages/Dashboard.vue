<template>
  <div class="dashboard">
    <el-row :gutter="12">
      <el-col :span="6"><el-card><div class="kpi-title">近{{ days }}天销售额</div><div class="kpi-value">¥ {{ toMoney(overview.salesAmount) }}</div></el-card></el-col>
      <el-col :span="6"><el-card><div class="kpi-title">订单总数</div><div class="kpi-value">{{ overview.totalOrders || 0 }}</div></el-card></el-col>
      <el-col :span="6"><el-card><div class="kpi-title">已支付订单</div><div class="kpi-value">{{ overview.paidOrders || 0 }}</div></el-card></el-col>
      <el-col :span="6"><el-card><div class="kpi-title">支付率</div><div class="kpi-value">{{ overview.paidRate || 0 }}%</div></el-card></el-col>
    </el-row>

    <el-row :gutter="12" class="mt12">
      <el-col :span="16">
        <el-card>
          <template #header><div class="card-head">销售额趋势（折线图）</div></template>
          <div ref="trendRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><div class="card-head">分类销量占比（饼图）</div></template>
          <div ref="pieRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="12" class="mt12">
      <el-col :span="24">
        <el-card>
          <template #header><div class="card-head">图书销量Top10（柱状图）</div></template>
          <div ref="barRef" class="chart-lg"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, ref, nextTick } from 'vue'
import { http } from '../api/http'
import * as echarts from 'echarts'

const days = 7
const overview = ref({})
const trendRef = ref()
const pieRef = ref()
const barRef = ref()
let trendChart = null
let pieChart = null
let barChart = null

const toMoney = (v) => Number(v || 0).toFixed(2)

const loadData = async () => {
  const [ov, tr, cg, tp] = await Promise.all([
    http(`/admin/statistics/overview?days=${days}`, { method: 'GET', json: false }),
    http(`/admin/statistics/salesTrend?days=${days}`, { method: 'GET', json: false }),
    http('/admin/statistics/categoryShare?days=30', { method: 'GET', json: false }),
    http('/admin/statistics/bookTop?days=30&limit=10', { method: 'GET', json: false })
  ])
  overview.value = (ov && ov.code === 1 && ov.data) ? ov.data : {}
  drawTrend((tr && tr.code === 1) ? tr.data : { dates: [], amounts: [] })
  drawPie((cg && cg.code === 1) ? cg.data : { items: [] })
  drawBar((tp && tp.code === 1) ? tp.data : { items: [] })
}

const drawTrend = async (data) => {
  await nextTick()
  if (!trendChart && trendRef.value) trendChart = echarts.init(trendRef.value)
  if (!trendChart) return
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: data?.dates || [] },
    yAxis: { type: 'value' },
    series: [{ type: 'line', smooth: true, data: data?.amounts || [] }]
  })
}

const drawPie = async (data) => {
  await nextTick()
  if (!pieChart && pieRef.value) pieChart = echarts.init(pieRef.value)
  if (!pieChart) return
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: true,
      data: data?.items || []
    }]
  })
}

const drawBar = async (data) => {
  await nextTick()
  if (!barChart && barRef.value) barChart = echarts.init(barRef.value)
  if (!barChart) return
  const items = data?.items || []
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: items.map(i => i.title), axisLabel: { interval: 0, rotate: 25 } },
    yAxis: { type: 'value' },
    series: [{ type: 'bar', data: items.map(i => i.quantity) }]
  })
}

const onResize = () => {
  trendChart && trendChart.resize()
  pieChart && pieChart.resize()
  barChart && barChart.resize()
}

onMounted(async () => {
  await loadData()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', onResize)
  trendChart && trendChart.dispose()
  pieChart && pieChart.dispose()
  barChart && barChart.dispose()
})
</script>

<style>
.dashboard { min-height: 92vh; }
.mt12 { margin-top: 14px; }
.kpi-title { color: var(--admin-sub); font-size: 13px; letter-spacing: .2px; }
.kpi-value { margin-top: 8px; font-size: 28px; font-weight: 700; color: #0f172a; }
.card-head { font-weight: 700; color: #0f172a; }
.chart { height: 320px; }
.chart-lg { height: 380px; }
:deep(.el-card) {
  border-radius: 16px;
}
@media (max-width: 992px) {
  .kpi-value {
    font-size: 22px;
  }
}
</style>
