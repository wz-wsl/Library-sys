<template>
  <div class="app-container home">
    <!-- 欢迎语 -->
    <el-card shadow="never" class="welcome">
      <img :src="logo" class="welcome-logo" />
      <div>
        <h2>{{ greeting }}，{{ userStore.nickName || userStore.name }}</h2>
        <p>欢迎使用{{ title }}，今天是 {{ today }}。</p>
      </div>
    </el-card>

    <!-- 数字卡片：没有对应权限的卡片不显示 -->
    <el-row :gutter="20">
      <el-col v-if="canBook" :xs="24" :sm="8">
        <el-card shadow="hover" class="stat" @click="go('/library/book')">
          <div class="stat-label">馆藏图书</div>
          <div class="stat-value">{{ bookTotal }}<span>种</span></div>
        </el-card>
      </el-col>
      <el-col v-if="canBorrow" :xs="24" :sm="8">
        <el-card shadow="hover" class="stat" @click="go('/library/borrow')">
          <div class="stat-label">我的在借</div>
          <div class="stat-value">{{ borrowing.length }}<span>本</span></div>
        </el-card>
      </el-col>
      <el-col v-if="canBorrow" :xs="24" :sm="8">
        <el-card shadow="hover" class="stat" @click="go('/library/borrow')">
          <div class="stat-label">已逾期</div>
          <div class="stat-value" :class="{ danger: overdueCount > 0 }">{{ overdueCount }}<span>本</span></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <!-- 快捷入口 -->
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" header="快捷入口" class="block">
          <div class="entries">
            <el-button v-if="canBook" icon="Reading" @click="go('/library/book')">图书信息</el-button>
            <el-button v-if="auth.hasPermi('library:category:list')" icon="Files" @click="go('/library/category')">图书分类</el-button>
            <el-button v-if="canBorrow" icon="Tickets" @click="go('/library/borrow')">我的借阅</el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 我还没还的书 -->
      <el-col v-if="canBorrow" :xs="24" :lg="16">
        <el-card shadow="never" header="我的在借图书" class="block">
          <el-table :data="borrowing" v-loading="loading" empty-text="目前没有在借的图书">
            <el-table-column label="书名" prop="bookName" min-width="140" />
            <el-table-column label="借阅时间" align="center" width="120">
              <template #default="{ row }">{{ parseTime(row.borrowTime, '{y}-{m}-{d}') }}</template>
            </el-table-column>
            <el-table-column label="应还时间" align="center" width="120">
              <template #default="{ row }">{{ parseTime(row.dueTime, '{y}-{m}-{d}') }}</template>
            </el-table-column>
            <el-table-column label="状态" align="center" width="90">
              <template #default="{ row }">
                <el-tag v-if="row.status === '2'" type="danger">已逾期</el-tag>
                <el-tag v-else>借阅中</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Index">
import logo from '@/assets/logo/logo.svg'
import auth from '@/plugins/auth'
import { parseTime } from '@/utils/ruoyi'
import useUserStore from '@/store/modules/user'
import { listBook } from '@/api/library/book'
import { listMyBorrows } from '@/api/library/borrow'

const router = useRouter()
const userStore = useUserStore()
const title = import.meta.env.VITE_APP_TITLE

// 有权限才去请求接口，否则后端会返回 403
const canBook = auth.hasPermi('library:book:list')
const canBorrow = auth.hasPermi('library:borrow:list')

const bookTotal = ref(0)
const borrowing = ref([])
const loading = ref(false)
const overdueCount = computed(() => borrowing.value.filter(r => r.status === '2').length)

// 按当前小时选问候语
const hour = new Date().getHours()
const greeting = hour < 9 ? '早上好' : hour < 12 ? '上午好' : hour < 18 ? '下午好' : '晚上好'
const today = parseTime(new Date(), '{y}年{m}月{d}日 星期{a}')

function go(path) {
  router.push(path)
}

function loadData() {
  if (canBook) {
    // 只要总数，所以每页 1 条就够了
    listBook({ pageNum: 1, pageSize: 1 }).then(res => {
      bookTotal.value = res.total
    })
  }
  if (canBorrow) {
    loading.value = true
    // 状态 0=借阅中、2=已逾期，这两种就是还没还的书
    listMyBorrows({ pageNum: 1, pageSize: 100 }).then(res => {
      borrowing.value = res.rows.filter(r => r.status === '0' || r.status === '2')
    }).finally(() => {
      loading.value = false
    })
  }
}

loadData()
</script>

<style scoped lang="scss">
.home {
  .welcome {
    margin-bottom: 20px;
    :deep(.el-card__body) {
      display: flex;
      align-items: center;
      gap: 20px;
    }
    h2 {
      margin: 0 0 8px;
      font-size: 22px;
    }
    p {
      margin: 0;
      color: var(--el-text-color-secondary);
    }
  }
  .welcome-logo {
    width: 64px;
    height: 64px;
  }
  .stat {
    margin-bottom: 20px;
    cursor: pointer;
  }
  .stat-label {
    color: var(--el-text-color-secondary);
    font-size: 14px;
  }
  .stat-value {
    margin-top: 10px;
    font-size: 30px;
    font-weight: 600;
    span {
      margin-left: 4px;
      font-size: 14px;
      font-weight: normal;
      color: var(--el-text-color-secondary);
    }
    &.danger {
      color: var(--el-color-danger);
    }
  }
  .block {
    margin-bottom: 20px;
  }
  .entries {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    .el-button {
      margin-left: 0;
    }
  }
}
</style>
