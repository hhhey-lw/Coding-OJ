<template>
  <a-card class="submit-stats-container" :bordered="false">
    <!-- 顶部标题 -->
    <!-- 顶部标题（新增月份显示） -->
    <h2 class="stats-title">
      <span style="font-size: 28px;margin-left: 0.5REM;margin-right: 0.5REM">{{ currentYear }}</span>年<span style="font-size: 28px;margin-left: 0.5REM;margin-right: 0.5REM">{{ currentMonth }}</span>月提交统计
      <div class="month-switcher">
        <button @click="prevMonth" class="btn-class">
          <IconDoubleLeft/>
        </button>
        <button @click="nextMonth" :disabled="btnStatus" class="btn-class">
          <IconDoubleRight/>
        </button>
      </div>
    </h2>

    <!-- 统计卡片区域 -->
    <a-row :gutter="16" class="stats-cards">
      <a-col :span="8">
        <a-card class="stat-card" hoverable>
          <a-space direction="vertical" align="center">
            <div class="stat-icon success">
              <icon-check-circle-fill />
            </div>
            <a-typography-title :heading="6">{{ totalSubmissions }}</a-typography-title>
            <a-typography-text type="secondary">总提交</a-typography-text>
          </a-space>
        </a-card>
      </a-col>

      <a-col :span="8">
        <a-card class="stat-card" hoverable>
          <a-space direction="vertical" align="center">
            <div class="stat-icon primary">
              <icon-trophy />
            </div>
            <a-typography-title :heading="6">{{ acceptedSubmissions }}</a-typography-title>
            <a-typography-text type="secondary">通过数</a-typography-text>
          </a-space>
        </a-card>
      </a-col>

      <a-col :span="8">
        <a-card class="stat-card" hoverable>
          <a-space direction="vertical" align="center">
            <div class="stat-icon warning">
            </div>
            <a-typography-title :heading="6">{{ acceptanceRate }}%</a-typography-title>
            <a-typography-text type="secondary">通过率</a-typography-text>
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <!-- 提交统计区域 -->
    <a-card class="submission-chart" :bordered="false">
      <template #title>
        <a-space>
          <icon-history />
          <span>每日提交摘要：</span>
        </a-space>
      </template>

      <div class="calendar-grid">
        <a-popover v-for="(day, index) in submitSummaryList" :key="index">
          <div
              class="calendar-day"
              :style="{ backgroundColor: isSubmitted(Number(day.yearMonthDay.substring(8, 10))) ? '#40c463' : '#dae2ef' }"
          ></div>
          <template #content>
            <p>提交：{{day.submitCount}}次</p>
            <p>通过：{{day.acceptCount}}次</p>
            <p>{{day.yearMonthDay}}</p>
          </template>
        </a-popover>
      </div>
    </a-card>
  </a-card>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import {
  IconCheckCircleFill,
  IconTrophy,
  IconHistory,
  IconDoubleLeft,
  IconDoubleRight
} from '@arco-design/web-vue/es/icon'
import {getUserCheckIn} from "@/api/user";
// import { useStore } from 'vuex';
import store from "@/store";

// 统计数据
const totalSubmissions = ref(0)
const acceptedSubmissions = ref(0)
const acceptanceRate = computed(() => {
  return totalSubmissions.value > 0
      ? ((acceptedSubmissions.value / totalSubmissions.value) * 100).toFixed(1)
      : '0.0'
})

interface SubmitSummary {
  userId: string;
  yearMonthDay: string;
  submitCount: number;
  acceptCount: number;
  updateTime: string;
}
const bitmap = ref(0);


// 生成最近30天数据
interface SubmitSummary {
  userId: string;
  yearMonthDay: string;
  submitCount: number;
  acceptCount: number;
  updateTime: string;
}

const submitSummaryList = ref<SubmitSummary[]>([]);

// 新增月份状态管理
const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1) // 月份从1开始


// ===========》 函数定义 《==============
// 新增月份切换方法
const prevMonth = () => {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
  fetchData() // 重新加载数据
}

const nextMonth = () => {
  // 新增：禁止切换到未来月份
  const today = new Date()
  const currentYearValue = today.getFullYear()
  const currentMonthValue = today.getMonth()

  if (currentYear.value > currentYearValue ||
      (currentYear.value === currentYearValue && currentMonth.value > currentMonthValue)) {
    // 如果当前显示的月份已经是未来月份，则不允许切换
    return
  }

  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
  fetchData() // 重新加载数据
}

const fetchData = async () => {
  // const store = useStore()
  // 使用当前年月而不是硬编码
  const year = currentYear.value
  const month = String(currentMonth.value).padStart(2, '0')

  // const res = await UserCheckInControllerService.getUserCheckInByUserIdAndYearMonthUsingGet(
  //     store.state.user.loginUser.id,
  //     `${year}-${month}`
  // )
  const res:any = await getUserCheckIn(
      store.state.user.loginUser.id,
      `${year}-${month}`
  );

  if (res && res.bitmap != null){
    bitmap.value = res.bitmap;
    totalSubmissions.value = res.userSubmitSummaryVO.reduce((a:number, b:any) => a + b.submitCount, 0);
    acceptedSubmissions.value = res.userSubmitSummaryVO.reduce((a:number, b:any) => a + b.acceptCount, 0);

    // 生成一个月的提交信息
    generateLastMonthDays(res.userSubmitSummaryVO);
  }
  else {
    bitmap.value = 0;
    totalSubmissions.value = 0;
    acceptedSubmissions.value = 0;
    generateLastMonthDays([])
  }
}

const generateLastMonthDays = (userSubmitSummaryVO: SubmitSummary[]) => {
  // 清空现有数据
  submitSummaryList.value = [];

  // const today = new Date();
  // const currentYear = today.getFullYear();
  // const currentMonth = today.getMonth() + 1; // 月份从0开始

  // 获取当月天数
  const daysInMonth = new Date(currentYear.value, currentMonth.value, 0).getDate();

  // 创建用户提交数据的映射，方便快速查找
  const submitMap = new Map<string, SubmitSummary>();
  userSubmitSummaryVO.forEach(item => {
    submitMap.set(item.yearMonthDay, item);
  });

  // 生成30天的数据（从今天往前推29天）
  for (let i = 1; i <= daysInMonth; i++) {
    const date = new Date(currentYear.value, currentMonth.value-1, i);

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const dateStr = `${year}-${month}-${day}`;

    // 检查是否有提交数据
    const existingData = submitMap.get(dateStr);

    if (existingData) {
      // 如果有提交数据，则使用实际数据
      submitSummaryList.value.push(existingData);
    } else {
      // 如果没有提交数据，则创建空对象
      submitSummaryList.value.push({
        userId: '', // 或者保留原始userId
        yearMonthDay: dateStr,
        submitCount: 0, // -1表示非当月日期
        acceptCount: 0,
        updateTime: ''
      });
    }
  }
}

const isSubmitted = (day: number) => {
  return ((bitmap.value >> (day - 1)) & 1) === 1;
};

const btnStatus = computed(() => {
  const today = new Date()
  const currentYearValue = today.getFullYear()
  const currentMonthValue = today.getMonth()

  // 如果当前显示的年份 > 今天年份，或者年份相同但月份 > 今天月份，则返回 true（禁用按钮）
  return currentYear.value > currentYearValue ||
      (currentYear.value === currentYearValue && currentMonth.value > currentMonthValue)
})

// =========》 生命周期 《=============
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.submit-stats-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  background-color: var(--color-bg-1);
}

.stats-title {
  font-size: 20px;
  margin-bottom: 24px;
  color: var(--color-text-1);
}

.stats-cards {
  margin-bottom: 1REM;
}

.stat-card {
  border-radius: 8px;
  text-align: center;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.stat-content {
  padding: 16px 0;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-bottom: 0.5REM;
}

.stat-icon.success {
  background-color: rgba(82, 196, 26, 0.1);
  color: rgb(82, 196, 26);
}

.stat-icon.primary {
  background-color: rgba(47, 84, 235, 0.1);
  color: rgb(47, 84, 235);
}

.stat-icon.warning {
  background-color: rgba(250, 173, 20, 0.1);
  color: rgb(250, 173, 20);
}

.submission-chart {
  border-radius: 8px;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  margin-top: 16px;
}

@media screen and (min-width: 768px) {
  .calendar-grid {
    max-width: 80%;
    margin-left: auto;
    margin-right: auto;
  }
}


.calendar-day {
  max-width: 2REM;
  height: 24px;
  border-radius: 4px;
  transition: all 0.3s;
}

.calendar-day:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  cursor: pointer;
}
.month-switcher {
  display: inline;
}

@media screen and (max-width: 768px)  {
  .mobile-class {
    width: 100%;
  }
  .month-switcher {
    display: block;
  }
}

.btn-class {
  background-color: #8ebc8e;
  border: 2px solid #2d8a55;
  border-radius: 0.5REM;
  height: 36px;
  color: white;
  width: 2REM;
  font-size: 16px;
  margin-right: 1REM;
  margin-top: 0.5REM;
  margin-bottom: 0.5REM;
  cursor: pointer;
}

/* 置灰（禁用）状态 */
.btn-class:disabled {
  background-color: #cccccc;
  border: 2px solid #595959;
  color: #666666;
  cursor: not-allowed;
}

</style>