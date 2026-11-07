<template>
  <div class="app-container">
    <el-table v-loading="loading" :data="records">
      <el-table-column label="书名" prop="bookName" />
      <el-table-column label="借出时间" prop="borrowTime" width="175" />
      <el-table-column label="应还时间" prop="dueTime" width="175" />
      <el-table-column label="归还时间" prop="returnTime" width="175" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">{{ statusLabels[row.status] || '—' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button v-if="!row.returnTime" v-hasPermi="['library:borrow:return']"
                     link type="primary" :disabled="returning" @click="handleReturn(row)">还书</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total > 0" :total="total" v-model:page="query.pageNum"
                v-model:limit="query.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="LibBorrow">
import { listMyBorrows, returnBook } from '@/api/library/borrow'

const { proxy } = getCurrentInstance()
const records = ref([])
const total = ref(0)
const loading = ref(false)
const returning = ref(false)
const query = ref({ pageNum: 1, pageSize: 10 })
const statusLabels = { '0': '借阅中', '1': '已归还', '2': '已逾期' }

async function getList() {
  loading.value = true
  try {
    const res = await listMyBorrows(query.value)
    records.value = res.rows
    total.value = res.total
  } catch (error) {
    console.error('读取借阅记录失败', error)
  } finally {
    loading.value = false
  }
}

async function handleReturn(row) {
  if (returning.value) return
  returning.value = true
  try {
    await proxy.$modal.confirm('确认归还《' + row.bookName + '》吗？')
    await returnBook(row.borrowId)
    proxy.$modal.msgSuccess('还书成功')
    await getList()
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') console.error('还书失败', error)
  } finally {
    returning.value = false
  }
}

onMounted(getList)
</script>
