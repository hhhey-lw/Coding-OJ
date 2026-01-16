<template>
  <div id="questionSubmitView">
    <a-divider size="0" />
    <a-empty v-if="dataList.length === 0" description="暂无提交记录" style="min-height: 400px; display: flex; align-items: center; justify-content: center;" />
    <a-table v-else :ref="tableRef" :columns="columns" :data="dataList" :pagination="{
      showTotal: true,
      pageSize: searchParams.pageSize,
      current: searchParams.current,
      total,
    }" @page-change="onPageChange"
             style="cursor: pointer"
             stripe
             class="mobile-responsive-table"
             @cell-click="toQuestionPage">
      <!-- 手动处理 judgeInfo 插槽 -->
      <template #judgeInfo="{ record }">
        <a-descriptions layout="inline-horizontal" size="small">

          <!-- 状态（使用 a-tag） -->
          <a-descriptions-item label="状态">
            <a-tag :color="getMessageColor(record.judgeInfo.message)">
              {{ record.judgeInfo.message || '未知状态' }}
            </a-tag>
          </a-descriptions-item>

          <!-- 时间 -->
          <a-descriptions-item label="时间">
            {{ record.judgeInfo.time ? formatTime(record.judgeInfo.time) : '未知' }}
          </a-descriptions-item>

          <!-- 内存 -->
          <a-descriptions-item label="内存">
            {{ record.judgeInfo.memory ? formatMemory(record.judgeInfo.memory) : '未知' }}
          </a-descriptions-item>
        </a-descriptions>
      </template>
      <!-- 判题状态 -->
      <template #status="{ record }">
        <a-tag  :color="getStatusColor(record.status)">
          {{ formatStatus(record.status) }}
        </a-tag>
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
  listUserQuestionSubmitByPage,
  QuestionSubmitQueryRequest,
  QuestionVO
} from "@/api/question";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import moment from "moment";
const router = useRouter();

// ===============》 变量定义 《=============
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
    title: "题目标题",
    dataIndex: "questionVO.title",
  },
  {
    title: "编程语言",
    dataIndex: "language",
  },
  {
    title: "判题信息",
    slotName: "judgeInfo",
  },
  {
    title: "判题状态",
    slotName: "status",
  },
  {
    title: "提交者",
    dataIndex: "userVO.userName",
  },
  {
    title: "创建时间",
    slotName: "createTime",
  },
];

// =========》 函数定义 《===========

const loadData = async () => {
  const res:any = await listUserQuestionSubmitByPage(
    {
      ...searchParams.value,
      sortField: "create_time",
      sortOrder: "DESC",
    }
  );
  if (res) {
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
 * @param question
 */
const toQuestionPage = (e: any) => {
  router.push({
    path: `/view/question/${e.questionVO.id}`,
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
      return "成功";
    case 3:
      return "失败";
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
      return "green";
    case 3:
      return "red";
    default:
      return "gray";
  }
};

function formatMemory(bytes: number) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(2)} KB`;
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / 1024 / 1024).toFixed(2)} MB`;
  return `${(bytes / 1024 / 1024 / 1024).toFixed(2)} GB`;
}

function formatTime(ms: number | string) {
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
    '答案错误': 'red',
    '成功': 'green',
    '答案正确': 'green',
    '时间超限': 'orange',
    '内存超限': 'purple',
    '编译错误': 'magenta',
    '系统错误': 'gray',
    '等待中': 'blue',
    '执行中': 'arcoblue'
  };
  return colorMap[message] || 'gray';
};

// =========> 生命周期 <========
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
});

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
</style>
