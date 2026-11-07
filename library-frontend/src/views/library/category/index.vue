<template>
  <div class="app-container">
    <el-button
      class="mb8" type="primary" plain icon="Plus"
      v-hasPermi="['library:category:add']"
      @click="handleAdd()"
    >新增顶层分类</el-button>
    <el-button
      v-hasPermi="['library:category:remove']"
      class="mb8" type="danger" plain icon="Delete"
      :disabled="!selectedCategories.length || deleting"
      @click="handleDelete(selectedCategories)"
    >批量删除</el-button>
    <el-table
      ref="categoryTable"
      v-loading="loading"
      :data="categoryList"
      row-key="categoryId"
      :tree-props="{ children: 'children', checkStrictly: true }"
      @selection-change="selectedCategories = $event"
    >
      <el-table-column v-if="$auth.hasPermi('library:category:remove')" type="selection" width="50" />
      <el-table-column label="分类名称" prop="categoryName" />
      <el-table-column label="显示顺序" prop="orderNum" width="100" />
      <el-table-column label="状态" align="center" width="100">
        <template #default="scope">
          <span v-if="scope.row.status === '0'">正常</span>
          <span v-else-if="scope.row.status === '1'">停用</span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="265">
        <template #default="scope">
          <el-button
            link type="primary"
            v-hasPermi="['library:category:add']"
            @click="handleAdd(scope.row)"
          >新增子分类</el-button>
          <el-button
            v-if="$auth.hasPermiAnd(['library:category:query', 'library:category:edit'])"
            link type="primary"
            @click="handleEdit(scope.row)"
          >改名</el-button>
          <el-button
            v-hasPermi="['library:category:move']"
            link type="primary" @click="handleMove(scope.row)"
          >移动</el-button>
          <el-button
            v-hasPermi="['library:category:remove']"
            link type="danger" :disabled="deleting" @click="handleDelete([scope.row])"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="open" :title="isEditing ? '修改分类名称' : '新增分类'" width="420px" append-to-body>
      <el-form label-width="90px" @submit.prevent="submitForm">
        <el-form-item v-if="!isEditing" label="上级分类">
          <span>{{ parentLabel }}</span>
        </el-form-item>
        <el-form-item label="分类名称">
          <el-input v-model="form.categoryName" maxlength="50" placeholder="请输入分类名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="open = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
    <el-dialog v-model="moveOpen" title="移动分类" width="420px" append-to-body>
      <el-form label-width="90px" @submit.prevent="submitMove">
        <el-form-item label="分类名称">{{ movingCategory?.categoryName }}</el-form-item>
        <el-form-item label="新上级">
          <el-tree-select
            v-model="moveParentId" :data="moveOptions"
            :props="{ label: 'categoryName', children: 'children' }"
            node-key="categoryId" check-strictly default-expand-all
            placeholder="请选择新上级" style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="moveOpen = false">取消</el-button>
        <el-button type="primary" :loading="moving" :disabled="!canMove" @click="submitMove">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { addCategory, delCategory, getCategory, listCategory, moveCategory, updateCategory } from '@/api/library/category'

const { proxy } = getCurrentInstance()
const categoryList = ref([])
const categoryTable = ref()
const selectedCategories = ref([])
const deleting = ref(false)
const loading = ref(false)
const open = ref(false)
const saving = ref(false)
const parentLabel = ref('')
const form = ref({ parentId: 0, categoryName: '' })
const isEditing = computed(() => form.value.categoryId != null)
const moveOpen = ref(false)
const moving = ref(false)
const movingCategory = ref(null)
const moveParentId = ref(null)
const canMove = computed(() => moveParentId.value != null && moveParentId.value !== movingCategory.value?.parentId)

function excludeMovingBranch(categories, categoryId) {
  return categories.filter(item => item.categoryId !== categoryId).map(item => ({
    ...item,
    children: excludeMovingBranch(item.children || [], categoryId)
  }))
}

const moveOptions = computed(() => [{
  categoryId: 0,
  categoryName: '顶层分类',
  children: excludeMovingBranch(categoryList.value, movingCategory.value?.categoryId)
}])

function handleMove(row) {
  movingCategory.value = row
  moveParentId.value = row.parentId
  moveOpen.value = true
}

async function submitMove() {
  if (moving.value || !canMove.value) return
  moving.value = true
  try {
    await moveCategory({ categoryId: movingCategory.value.categoryId, parentId: moveParentId.value })
    moveOpen.value = false
    proxy.$modal.msgSuccess('移动成功')
    await getList()
  } catch (error) {
    console.error('移动分类失败', error)
  } finally {
    moving.value = false
  }
}

function handleAdd(parent) {
  form.value = { parentId: parent?.categoryId ?? 0, categoryName: '' }
  parentLabel.value = parent?.categoryName ?? '顶层分类'
  open.value = true
}

async function handleEdit(row) {
  try {
    const { data } = await getCategory(row.categoryId)
    form.value = { categoryId: data.categoryId, categoryName: data.categoryName }
    open.value = true
  } catch (error) {
    console.error('查询分类详情失败', error)
  }
}

async function submitForm() {
  if (saving.value) return
  const name = form.value.categoryName.trim()
  if (!name) {
    proxy.$modal.msgWarning('请输入分类名称')
    return
  }

  saving.value = true
  try {
    if (isEditing.value) {
      await updateCategory({ categoryId: form.value.categoryId, categoryName: name })
      proxy.$modal.msgSuccess('改名成功')
    } else {
      await addCategory({ parentId: form.value.parentId, categoryName: name })
      proxy.$modal.msgSuccess('新增成功')
    }
    open.value = false
    getList()
  } catch (error) {
    console.error('保存分类失败', error)
  } finally {
    saving.value = false
  }
}

async function handleDelete(rows) {
  if (deleting.value || !rows.length) return
  const ids = rows.map(row => row.categoryId)
  const names = rows.map(row => row.categoryName).join('、')
  deleting.value = true
  try {
    await proxy.$modal.confirm(`确定删除「${names}」共 ${ids.length} 个分类吗？`)
    await delCategory(ids)
    categoryTable.value.clearSelection()
    proxy.$modal.msgSuccess('删除成功')
    await getList()
  } catch (reason) {
    if (reason !== 'cancel' && reason !== 'close') console.error('删除分类失败', reason)
  } finally {
    deleting.value = false
  }
}

function getList() {
  loading.value = true
  return listCategory()
    .then(res => {
      categoryList.value = proxy.handleTree(res.data, 'categoryId')
    })
    .catch(error => {
      console.error('查询分类失败', error)
    })
    .finally(() => {
      loading.value = false
    })
}

getList()
</script>
