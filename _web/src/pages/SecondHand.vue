<template>
  <el-card class="sh-card">
    <div class="toolbar">
      <el-select v-model="qStatus" placeholder="状态" clearable class="filt" @change="onSearch">
        <el-option label="待审核" :value="0" />
        <el-option label="已驳回" :value="1" />
        <el-option label="在售" :value="2" />
        <el-option label="已售" :value="3" />
        <el-option label="用户撤回" :value="4" />
        <el-option label="下单待付款" :value="8" />
      </el-select>
      <el-button type="primary" @click="onSearch">搜索</el-button>
    </div>
    <el-table v-loading="loading" :data="items" border stripe height="720" empty-text="暂无数据">
      <el-table-column prop="id" label="ID" width="72" />
      <el-table-column prop="bookTitle" label="书名" min-width="160" show-overflow-tooltip />
      <el-table-column prop="sellerUserId" label="卖家用户ID" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ row.statusText }}</template>
      </el-table-column>
      <el-table-column label="用户实拍" width="200">
        <template #default="{ row }">
          <div v-if="rowPhotos(row).length" class="thumb-row">
            <el-image
              v-for="(u, i) in rowPhotos(row).slice(0, 3)"
              :key="String(row.id) + '-' + i"
              :src="u"
              class="thumb"
              fit="cover"
              :lazy="false"
              :preview-src-list="rowPhotos(row)"
              :initial-index="i"
            />
            <span v-if="rowPhotos(row).length > 3" class="thumb-more">+{{ rowPhotos(row).length - 3 }}</span>
          </div>
          <span v-else class="cell-dash">-</span>
        </template>
      </el-table-column>
      <el-table-column label="店员评定" width="88">
        <template #default="{ row }">{{ row.conditionGradeText || '-' }}</template>
      </el-table-column>
      <el-table-column label="比例" width="72">
        <template #default="{ row }">{{ row.priceRatio != null ? row.priceRatio + '%' : '-' }}</template>
      </el-table-column>
      <el-table-column label="售价" width="100">
        <template #default="{ row }">{{ row.salePrice != null ? '¥ ' + toMoney(row.salePrice) : '-' }}</template>
      </el-table-column>
      <el-table-column prop="userNote" label="用户备注" min-width="140" show-overflow-tooltip />
      <el-table-column prop="staffRemark" label="店员备注" min-width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="primary" size="small" @click="openEvaluate(row)">评估</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        v-model:current-page="page"
        :page-size="pageSize"
        @current-change="fetch"
      />
    </div>

    <el-dialog v-model="dlg" title="二手书评估" width="520px" @closed="resetDlg">
      <el-form label-width="100px">
        <el-form-item label="书名">
          <span>{{ current?.bookTitle }}</span>
        </el-form-item>
        <el-form-item label="用户实拍">
          <div v-if="evaluatePhotos.length" class="eval-photos">
            <el-image
              v-for="(u, i) in evaluatePhotos"
              :key="u + i"
              :src="u"
              fit="cover"
              :lazy="false"
              :preview-src-list="evaluatePhotos"
              :initial-index="i"
              class="eval-ph"
            />
          </div>
          <span v-else class="eval-empty">暂无（未上传或地址无法解析）</span>
        </el-form-item>
        <el-form-item label="处理">
          <el-radio-group v-model="approve">
            <el-radio :label="true">同意上架</el-radio>
            <el-radio :label="false">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="approve" label="成色">
          <el-select v-model="gradeId" placeholder="选择成色档位（决定回收价）" style="width: 100%">
            <el-option
              v-for="g in gradeOptions"
              :key="String(g.id)"
              :label="`${g.name}（回收 ${toPercent(g.recyclePercent)}）`"
              :value="g.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="staffRemark" type="textarea" rows="2" placeholder="对用户可见的说明（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dlg = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doEvaluate">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { http } from '../api/http'
import { normalizeConditionImageUrls } from '../utils/secondHandImages.js'

const loading = ref(false)
const items = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const qStatus = ref(undefined)

const dlg = ref(false)
const current = ref(null)
const approve = ref(true)
const gradeId = ref(undefined)
const gradeOptions = ref([])
const staffRemark = ref('')
const saving = ref(false)

const evaluatePhotos = computed(() =>
  normalizeConditionImageUrls(current.value?.userConditionImages ?? current.value?.user_condition_images)
)

const rowPhotos = (row) =>
  normalizeConditionImageUrls(row?.userConditionImages ?? row?.user_condition_images)

const toMoney = (v) => (Number(v || 0)).toFixed(2)
const toPercent = (v) => {
  const n = Number(v ?? 0)
  if (!Number.isFinite(n)) return '0.00%'
  return `${n.toFixed(2)}%`
}

const loadGrades = async () => {
  try {
    const resp = await http('/admin/secondHand/config', { method: 'GET', json: false })
    const list = (resp && Number(resp.code) === 1 && Array.isArray(resp.data?.grades)) ? resp.data.grades : []
    gradeOptions.value = list.filter(g => Number(g?.enabled ?? 1) === 1)
    if (!gradeId.value) gradeId.value = firstEnabledGradeId()
  } catch (_) {
    gradeOptions.value = []
  }
}

const firstEnabledGradeId = () => {
  const g = gradeOptions.value && gradeOptions.value.length ? gradeOptions.value[0] : null
  return g?.id
}

const fetch = async () => {
  loading.value = true
  try {
    const qs = new URLSearchParams({
      page: String(page.value),
      pageSize: String(pageSize.value)
    })
    if (qStatus.value !== undefined && qStatus.value !== null && qStatus.value !== '') {
      qs.set('status', String(qStatus.value))
    }
    const resp = await http(`/admin/secondHand/page?${qs}`, { method: 'GET', json: false })
    if (resp && Number(resp.code) === 1) {
      const d = resp.data
      items.value = Array.isArray(d?.records) ? d.records : []
      total.value = Number(d?.total ?? 0) || 0
    } else {
      ElMessage.error(resp?.msg || `加载失败（HTTP ${resp.httpStatus ?? '?' }）`)
    }
  } catch (_) {
    ElMessage.error('网络异常，请确认本机可访问 store-api:8090')
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  page.value = 1
  fetch()
}

const openEvaluate = (row) => {
  current.value = row
  approve.value = true
  gradeId.value = firstEnabledGradeId()
  staffRemark.value = ''
  dlg.value = true
}

const resetDlg = () => {
  current.value = null
}

const doEvaluate = async () => {
  if (!current.value?.id) return
  if (approve.value && !gradeId.value) {
    ElMessage.warning('请选择成色档位')
    return
  }
  saving.value = true
  try {
    const resp = await http('/admin/secondHand/evaluate', {
      method: 'POST',
      body: {
        id: current.value.id,
        approve: approve.value,
        gradeId: approve.value ? gradeId.value : null,
        staffRemark: staffRemark.value || null
      }
    })
    if (resp && Number(resp.code) === 1) {
      ElMessage.success('已保存')
      dlg.value = false
      fetch()
      window.dispatchEvent(new CustomEvent('admin-badges-refresh'))
    } else {
      ElMessage.error(resp?.msg || '保存失败')
    }
  } catch (_) {
    ElMessage.error('请求失败')
  } finally {
    saving.value = false
  }
}

onMounted(fetch)
onMounted(loadGrades)
</script>

<style scoped>
.sh-card { min-height: 88vh; }
.toolbar { display: flex; gap: 12px; margin-bottom: 12px; flex-wrap: wrap; }
.filt { width: 200px; }
.pager { margin-top: 12px; display: flex; justify-content: flex-end; }
.eval-photos { display: flex; flex-wrap: wrap; gap: 8px; }
.eval-ph { width: 80px; height: 80px; border-radius: 6px; overflow: hidden; }
.eval-empty { color: #909399; font-size: 13px; }
.thumb-row { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; }
.thumb { width: 48px; height: 48px; border-radius: 6px; overflow: hidden; flex-shrink: 0; }
.thumb-more { font-size: 12px; color: #909399; }
.cell-dash { color: #c0c4cc; }
</style>
