<template>
  <el-dialog
    :model-value="modelValue"
    title="新增收货地址"
    width="640px"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-form ref="addrFormRef" :model="addrForm" :rules="addrRules" label-width="110px">
      <el-form-item label="收货人" prop="consignee">
        <el-input v-model="addrForm.consignee" placeholder="请输入收货人" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="addrForm.phone" placeholder="请输入手机号" maxlength="11" />
      </el-form-item>
      <el-form-item label="性别" prop="sex">
        <el-radio-group v-model="addrForm.sex">
          <el-radio :label="1">男</el-radio>
          <el-radio :label="0">女</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="省份">
        <el-input v-model="addrForm.provinceName" disabled />
      </el-form-item>
      <el-form-item label="城市">
        <el-input v-model="addrForm.cityName" disabled />
      </el-form-item>
      <el-form-item label="区县" prop="districtName">
        <el-select v-model="addrForm.districtName" placeholder="请选择区县" @change="onDistrictChange">
          <el-option label="城厢区" value="城厢区" />
          <el-option label="荔城区" value="荔城区" />
        </el-select>
      </el-form-item>
      <el-form-item label="学校分区">
        <el-input v-model="addrForm.schoolPartition" disabled />
      </el-form-item>
      <el-form-item label="宿舍楼" prop="building">
        <el-input v-model="addrForm.building" placeholder="请输入宿舍楼" />
      </el-form-item>
      <el-form-item label="宿舍号" prop="houseNumber">
        <el-input v-model="addrForm.houseNumber" placeholder="请输入宿舍号" />
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="addrForm.label" placeholder="如 家/公司/学校" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-ops">
        <el-button @click="$emit('update:modelValue', false)">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['update:modelValue', 'saved'])

const addrFormRef = ref()
const saving = ref(false)
const addrForm = ref({
  consignee: '',
  phone: '',
  sex: 1,
  provinceName: '福建省',
  cityName: '莆田市',
  districtName: '',
  label: '',
  isDefault: 0,
  schoolPartition: '',
  building: '',
  houseNumber: ''
})
const addrRules = {
  consignee: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  districtName: [{ required: true, message: '请选择区县', trigger: 'change' }],
  building: [{ required: true, message: '请输入宿舍楼', trigger: 'blur' }],
  houseNumber: [{ required: true, message: '请输入宿舍号', trigger: 'blur' }]
}

const onDistrictChange = () => {
  if (addrForm.value.districtName === '荔城区') addrForm.value.schoolPartition = '紫霄校区'
  else if (addrForm.value.districtName === '城厢区') addrForm.value.schoolPartition = '学园校区'
  else addrForm.value.schoolPartition = ''
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      addrForm.value = {
        consignee: '',
        phone: '',
        sex: 1,
        provinceName: '福建省',
        cityName: '莆田市',
        districtName: '',
        label: '',
        isDefault: 0,
        schoolPartition: '',
        building: '',
        houseNumber: ''
      }
    }
  }
)

const onSave = async () => {
  if (!addrFormRef.value) return
  try {
    await addrFormRef.value.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    const token = localStorage.getItem('token') || ''
    const userId = localStorage.getItem('userId') || ''
    const payload = { ...addrForm.value, userId, isDefault: 0 }
    const resp = await fetch('/user/addressBook', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...(token ? { authentication: token } : {}) },
      body: JSON.stringify(payload)
    })
    if (resp.ok) {
      ElMessage.success('地址已新增')
      emit('update:modelValue', false)
      emit('saved')
    } else {
      ElMessage.error('保存地址失败')
    }
  } catch {
    ElMessage.error('保存地址失败')
  } finally {
    saving.value = false
  }
}
</script>
