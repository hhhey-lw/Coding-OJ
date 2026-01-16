<template>
  <div id="questionSubmitView">
    <!--  用户排名  --><!-- 排行榜表格 -->
    <a-card class="ranking-table">
      <h3>
        排名总榜
      </h3>
      <a-table
          :columns="rankColumns"
          :data="users"
          :pagination="false"
          stripe
          class="mobile-responsive-table"
      >
        <!-- 插槽 -->
        <template #rank="{ rowIndex }">
          <span>{{ rowIndex + 1 }}</span>
        </template>
        <!-- 用户名插槽 -->
        <template #userinfo="{ record }">
          <div class="user-info">
            <a-avatar>
              <img :key="record.id" :src="record.userAvatar" :size="24" />
            </a-avatar>
            <span class="username">{{ record.userName }}</span>
          </div>
        </template>

        <!-- 解题数插槽 -->
        <template #passedQuestionNumber="{ record }">
          <div class="stats">
            <span class="stat-value">{{ record.passedQuestionNumber }}</span>
          </div>
        </template>

        <template #submitQuestionNumber="{ record }">
          <div class="stats">
            <span class="stat-value">{{ record.totalSubmitNumber }}</span>
          </div>
        </template>

        <!-- 通过率插槽 -->
        <template #passRate="{ record }">
          <div class="stats">
            <span class="stat-value" :style="{ color: getPassRateColor(parseFloat(fixedNumber(record.passedQuestionNumber / record.totalSubmitNumber))) }">
              {{ fixedNumber(record.passedQuestionNumber / record.totalSubmitNumber)}}%
            </span>
          </div>
        </template>
      </a-table>
    </a-card>
    <a-divider />
    <!--  提交记录  -->
    <a-table :ref="tableRef" :columns="columns" :data="dataList" :pagination="{
      showTotal: true,
      pageSize: searchParams.pageSize,
      current: searchParams.current,
      total,
    }" @page-change="onPageChange"
             stripe
             class="mobile-responsive-table">
      <template #title="{ record }">
        <span
              @click="toQuestionPage(record.questionVO.id)"
              style="color: rgb(87, 163, 243); cursor: pointer">{{ record.questionVO.title }}</span>
      </template>

      <template #judgeInfoStatus="{ record }">
        <a-tag :color="getMessageColor(record.judgeInfo.message)">
          {{ record.judgeInfo.message || '未知状态' }}
        </a-tag>
      </template>

      <template #judgeInfoTime="{ record }">
        <span>{{ record.judgeInfo.time ? formatTime(record.judgeInfo.time) : '未知' }}</span>
      </template>

      <template #judgeInfoMemory="{ record }">
        <span>{{ record.judgeInfo.memory ? formatMemory(record.judgeInfo.memory) : '未知' }}</span>
      </template>
      <template #language="{ record }">
        <span style="color: rgb(87, 163, 243);">
          {{ formatLanguage(record.language) }}
        </span>
      </template>
      <!-- 判题状态 -->
      <template #status="{ record }">
        <span :style="{ color: getStatusColor(record.status) }">
          {{ formatStatus(record.status) }}
        </span>
      </template>
      <template #createTime="{ record }">
        {{ moment(record.createTime).format("YYYY-MM-DD HH:mm") }}
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect, computed } from "vue";
import {
  listQuestionSubmitByPage,
  getTopPassedQuestionUserList,
  QuestionSubmitQueryRequest,
  UserSubmitInfoVO,
} from "@/api/question";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import moment from "moment";

import {
  Card as ACard,
  Divider as ADivider,
  Avatar as AAvatar,
} from "@arco-design/web-vue";

const router = useRouter();

//  =========> 数据定义 <===========

const tableRef = ref();

const dataList = ref([]);
const total = ref(0);
const searchParams = ref<QuestionSubmitQueryRequest>({
  questionId: undefined,
  language: undefined,
  pageSize: 10,
  current: 1,
});

const columns = [
  {
    title: "题目",
    slotName: "title",
  },
  {
    title: "状态",
    slotName: "judgeInfoStatus",
  },
  {
    title: "运行时间",
    slotName: "judgeInfoTime",
  },
  {
    title: "运行内存",
    slotName: "judgeInfoMemory",
  },
  {
    title: "编程语言",
    slotName: "language",
  },
  // {
  //   title: "判题信息",
  //   slotName: "judgeInfo",
  // },
  {
    title: "执行状态",
    slotName: "status",
  },
  {
    title: "提交者",
    dataIndex: "userVO.userName",
  },
  {
    title: "提交时间",
    slotName: "createTime",
  },
];


//  =========> 函数定义 <===========

const loadData = async () => {
  const res:any = await listQuestionSubmitByPage(
      {
        ...searchParams.value,
        sortField: "create_time",
        sortOrder: "DESC",
      }
  );
  if (res) {
    // console.log('res.data---question_submit',res.data);
    dataList.value = res.records;
    total.value = res.total;
  } else {
    message.error("加载失败");
  }

};


const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};


/**
 * 跳转到做题页面
 */
const toQuestionPage = (id:string) => {
  // console.log(e)
  router.push({
    path: `/view/question/${id}`,
  });
};


// 根据 status 返回对应的状态字符串
const formatStatus = (status: number) => {
  switch (status) {
    case 0:
      return "待判题";
    case 1:
      return "判题中";
    case 2:
      return "运行完成";
    case 3:
      return "运行失败";
    default:
      return "未知状态";
  }
};

const getStatusColor = (status: number) => {
  switch (status) {
    case 0:
      return "orange";
    case 1:
      return "arcoblue";
    case 2:
      return "#19be6b";
    case 3:
      return "red";
    default:
      return "gray";
  }
};

const formatLanguage = (lang: string) => {
  if (lang && lang.toLowerCase() === "java") {
    return "Java";
  }
  return lang;
};

function formatMemory(bytes:number) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(2)} KB`;
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(2)} MB`;
  return `${(bytes / 1024 / 1024 / 1024).toFixed(2)} GB`;
}

function formatTime(ms:any) {
  // 如果是字符串，先转为数字（支持 "123"、"123.456" 等格式）
  if (typeof ms === 'string') {
    ms = parseFloat(ms);
  }

  // 确保 ms 是有效数字（否则返回默认值）
  if (isNaN(ms) || typeof ms !== 'number') {
    return '未知'; // 或 throw new Error("Invalid time value");
  }
  if (ms < 1000) return `${ms.toFixed(0)} ms`;
  if (ms < 60000) return `${(ms / 1000).toFixed(2)} s`;  // < 60s
  return `${(ms / 60000).toFixed(2)} min`;  // ≥ 60s → 分钟
}

// 根据 message 内容获取标签颜色
const getMessageColor = (message: string) => {
  const colorMap: Record<string, string> = {
    '答案错误': '#ed3f14',
    '成功': '#19be6b',
    '答案正确': '#19be6b',
    '时间超限': 'orange',
    '内存超限': 'purple',
    '编译错误': 'magenta',
    '系统错误': 'gray',
    '等待中': 'blue',
    '执行中': 'arcoblue'
  };
  return colorMap[message] || 'gray';
};


//  =========> 生命周期 <===========

/**
 * 监听 searchParams 变量，改变时触发页面的重新加载
 */
watchEffect(() => {
  loadData();
});

/**
 * 页面加载时，请求数据
 */
onMounted(() => {
  loadData();
  loadRankingData();
});

//  =========> 用户排名相关 <===========
// 模拟数据
const users = ref<UserSubmitInfoVO[]>([]);

// 表格列配置
const rankColumns = [
  { title: '排名',
    slotName: 'rank'},
  {
    title: '用户',
    slotName: 'userinfo'
  },
  {
    title: '通过数',
    slotName: 'passedQuestionNumber'
  },{
    title: '提交数',
    slotName: 'submitQuestionNumber'
  },
  {
    title: '正确率',
    slotName: 'passRate'
  }
];

// 通过率颜色计算
const getPassRateColor = (percent: number) => {
  return percent > 50 ? '#4CAF50' : '#FF5252';
};

const fixedNumber = (num:number) => {
  return (num * 100).toFixed(2);
}

const loadRankingData = async () => {
  // 模拟加载用户排名数据
  // 实际应用中可以从后端接口获取
  const res:any = await getTopPassedQuestionUserList(10);
  if (res) {
    users.value = res;
  } else {
    message.error("加载用户排名失败");
  }
};

</script>

<style scoped>
#questionSubmitView {
  max-width: 1280px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .mobile-responsive-table {
    width: 100%;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }

  .mobile-responsive-table :deep(.arco-table) {
    min-width: 100%;
    width: auto;
  }

  .mobile-responsive-table :deep(.arco-table-th),
  .mobile-responsive-table :deep(.arco-table-td) {
    white-space: nowrap;
    min-width: 120px;
    padding: 8px 12px;
  }
}

:deep(.arco-table-th) {
  font-weight: bold !important;
}

:deep(.arco-table-pagination) {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}


/* 用户排名表格样式  */
.ranking-table {
  background-color: #fff;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  margin-left: 10px;
}

.stats {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.stat-label {
  font-size: 12px;
  color: #888;
}

.stat-value {
  font-weight: bold;
}

.rank-change {
  width: 100%;
  text-align: right;
}


</style>
