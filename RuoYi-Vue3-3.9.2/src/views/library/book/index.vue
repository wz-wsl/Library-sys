<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true">
         <el-form-item label="书名" prop="bookName">
            <el-input
               v-model="queryParams.bookName"
               placeholder="请输入书名"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="作者名字" prop="author">
            <el-input
               v-model="queryParams.author"
               placeholder="请输入作者名字"
               clearable
               style="width: 200px"
               @keyup.enter="handleQuery"
            />
         </el-form-item>
         <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="图书状态" clearable style="width: 200px">
              <el-option label="上架" value="0" />
              <el-option label="下架" value="1" />
            </el-select>
         </el-form-item>
         <el-form-item>
            <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetQuery">重置</el-button>
         </el-form-item>
      </el-form>
      <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
              <el-button
                type="primary"
                plain
                icon="Plus"
                @click="handleAdd"
                v-hasPermi="['library:book:add']"
              >新增</el-button>
            </el-col>
            <el-col :span="1.5">
              <el-button
                type="danger"
                plain
                icon="Delete"
                :disabled="multiple"
                @click="handleDelete"
                v-hasPermi="['library:book:remove']"
              >删除</el-button>
            </el-col>
          </el-row>
    <!-- 表格 -->
    <el-table v-loading="loading" :data="bookList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="图书ID" align="center" prop="bookId" />
      <el-table-column label="书名" align="center" prop="bookName" />

      <el-table-column label="作者" align="center" prop="author" />
      <el-table-column label="出版社" align="center" prop="publisher" />
      <el-table-column label="馆藏总数" align="center" prop="totalCount" />
      <el-table-column label="可借数量" align="center" prop="stockCount" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['library:book:edit']"
          >修改</el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['library:book:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
        <!-- 添加图书对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="bookRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="书名" prop="bookName">
          <el-input v-model="form.bookName" placeholder="请输入书名" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="出版社" prop="publisher">
          <el-input v-model="form.publisher" placeholder="请输入出版社" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" placeholder="请输入ISBN" />
        </el-form-item>
        <el-form-item label="馆藏总数" prop="totalCount">
          <el-input-number v-model="form.totalCount" controls-position="right" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="0">上架</el-radio>
            <el-radio value="1">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Book">

import { listBook, addBook, getBook, updateBook, delBook } from "@/api/library/book"

const bookList = ref([])     // 表格数据
const loading = ref(true)    // 是否显示"加载中"
const total = ref(0)         // 总条数
const { proxy } = getCurrentInstance()
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  bookName: undefined,
  author: undefined,
  status: undefined
})

const open = ref(false)     // 弹窗是否显示
const title = ref("")       // 弹窗标题
const form = ref({})        // 弹窗表单的数据
const rules = ref({         // 表单校验规则
  bookName: [{ required: true, message: "书名不能为空", trigger: "blur" }]
})

const ids = ref([])             // 选中的图书ID数组
const multiple = ref(true)   // 删除按钮是否禁用

/** 查询图书列表 */
function getList() {
  loading.value = true
  listBook(queryParams.value).then(res => {
    bookList.value = res.rows
    total.value = res.total
    loading.value = false
  })
}

getList()

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1     // 回到第 1 页
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm("queryRef")       // 清空搜索框
  handleQuery()
}

function reset() {
  form.value = {
    bookId: undefined,
    bookName: undefined,
    author: undefined,
    publisher: undefined,
    isbn: undefined,
    totalCount: 0,
    status: "0",
    remark: undefined
  }          // 默认状态为上架
  proxy.resetForm("bookRef")       // 清空表单
}

function handleAdd() {
  reset()
  title.value = "新增图书"
  open.value = true
}
function cancel() {
  open.value = false
  reset()
}
/** 提交按钮 */
function submitForm() {
  proxy.$refs["bookRef"].validate(valid => {
    if (valid) {
      if (form.value.bookId != undefined) {
        updateBook(form.value).then(() => {
          proxy.$modal.msgSuccess("修改成功")
          open.value = false
          getList()
        })
      } else {
        addBook(form.value).then(() => {
          proxy.$modal.msgSuccess("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  getBook(row.bookId).then(res => {
    form.value = res.data
    open.value = true
    title.value = "修改图书"
  })
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.bookId)
  multiple.value = !selection.length
}

/** 删除按钮操作 */
function handleDelete(row) {
  const bookIds = row.bookId || ids.value
  proxy.$modal.confirm('是否确认删除图书编号为"' + bookIds + '"的数据项？').then(() => {
    return delBook(bookIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess("删除成功")
  }).catch(() => {})
}

</script>