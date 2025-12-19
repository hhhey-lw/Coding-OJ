<template>
  <div id="manageQuestionView">
    <a-table :ref="tableRef" :columns="columns" :data="dataList" :pagination="{
      showTotal: true,
      pageSize: searchParams.pageSize,
      current: searchParams.current,
      total,
    }" @page-change="onPageChange"
      :scroll="{ x: 1000 }"
      :scrollbar="true">
      <!-- 自定义内容列 -->
<!--      <template #content="{ record }">-->
<!--        <div class="ellipsed-column">-->
<!--          {{ record.content }}-->
<!--        </div>-->
<!--      </template>-->
      <!-- 自定义答案列 -->
<!--      <template #answer="{ record }">-->
<!--        <div class="ellipsed-column">-->
<!--          {{ record.answer }}-->
<!--        </div>-->
<!--      </template>-->
      <!-- 自定义标签列 -->
      <template #tags="{ record }">
        <div style="width: 110px;">
          <a-tag v-for="tag in JSON.parse(record.tags)" :key="tag" :checkable="true" :default-checked="true"
            style="margin-left: 10px;" color="green">
            {{ tag }}
          </a-tag>
        </div>
      </template>
      <template #createTime="{ record }">
        <div style="width: 100px;">
          {{ moment(record.createTime).format("YYYY-MM-DD") }}
        </div>
      </template>
      <!-- 自定义判题配置列 -->
      <template #judgeConfig="{ record }">
        <div class="judge-config" style="width: 130px;">
          <div v-for="(value, key) in formatJudgeConfig(record.judgeConfig)" :key="key">
            <strong>{{ key }}:</strong> {{ value }}
          </div>
        </div>
      </template>
      <!-- 自定义判题用例列 -->
<!--      <template #judgeCase="{ record }">-->
<!--        <div class="test-case">-->
<!--          <div v-for="(testCase, index) in parseTestCases(record.judgeCase)" :key="index" class="test-case-item">-->
<!--            <span>输入:</span>-->
<!--            <pre class="inline">{{ formatTestCase(testCase.input) }}</pre>-->
<!--            <br />-->
<!--            <span>输出:</span>-->
<!--            <pre class="inline">{{ testCase.output }}</pre>-->
<!--          </div>-->
<!--        </div>-->
<!--      </template>-->
      <template #optional="{ record }">
        <a-space>
          <a-button type="primary" @click="doUpdate(record)"> 修改</a-button>
          <a-button status="danger" @click="doDelete(record)">删除</a-button>
        </a-space>
      </template>

    </a-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  Page_Question_,
  Question,
  QuestionControllerService,
} from "../../../generated";
import { Modal } from '@arco-design/web-vue';
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";
import moment from "moment";

const tableRef = ref();

const dataList = ref([]);
const total = ref(0);
const searchParams = ref({
  pageSize: 10,
  current: 1,
});

const loadData = async () => {
  const res = await QuestionControllerService.listQuestionByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    console.log('res--manage', res.data);

    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败，" + res.message);
  }
};

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


const columns = [
  {
    title: "标题",
    dataIndex: "title",
  },
  {
    title: "标签",
    slotName: "tags",
    // width: 250,
  },
  {
    title: "提交数",
    dataIndex: "submitNum",
    // width: 150,
  },
  {
    title: "通过数",
    dataIndex: "acceptedNum",
    // width: 150,
  },
  {
    title: "判题配置",
    slotName: "judgeConfig",
  },
  {
    title: "创建时间",
    slotName: "createTime",
  },
  {
    title: "操作",
    slotName: "optional",
  },
];

const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

const doDelete = async (question: Question) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除题目 "${question.title}" 吗？此操作不可撤销。`,
    onOk: async () => {
      const res = await QuestionControllerService.deleteQuestionUsingPost({
        id: question.id,
      });
      if (res.code === 0) {
        message.success("删除成功");
        loadData();
      } else {
        message.error("删除失败");
      }
    }
  });
};

const router = useRouter();

const doUpdate = (question: Question) => {
  router.push({
    path: "/update/question",
    query: {
      id: question.id,
    },
  });
};

// 将 judgeConfig 字符串解析为对象并格式化为特定的显示格式
const formatJudgeConfig = (judgeConfig: string) => {
  try {
    const config = JSON.parse(judgeConfig);
    return {
      "时间限制": `${config.timeLimit} ms`,
      "空间限制": `${config.memoryLimit} KB`,
      "堆限制": `${config.stackLimit == null ? "NULL" : config.stackLimit} KB`,
    };
  } catch (e) {
    return {};
  }
};

</script>

<style scoped>
#manageQuestionView {
  max-width: 1280px;
  margin: 0 auto;
}
</style>
