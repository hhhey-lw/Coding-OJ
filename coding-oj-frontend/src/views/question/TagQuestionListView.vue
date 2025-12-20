<template>
  <div class="container-box">
    <div class="container">
      <!-- 顶部导航栏 -->
      <header class="header">
        <a-button type="text" class="back-btn" @click="handleBack">
          <template #icon><icon-left /></template>
          返回
        </a-button>
        <div class="header-right">
          <a-button type="text" class="share-btn" @click="() => Message.info('分享功能尚未实现')">
            <template #icon><icon-share-alt /></template>
          </a-button>
        </div>
      </header>

      <!-- 标题区域 -->
      <div class="title-container">
        <div class="title-left">
          <div class="badge-icon">
            <img :src="image5" alt="badge" style="width: 48px; height: 48px;" />
          </div>
          <div>
            <h1 class="main-title">{{ tagName }}</h1>
            <p class="sub-title">{{ tagDescription }}</p>
          </div>
        </div>
        <div class="header-actions">
          <a-button type="primary" status="success" class="add-btn" @click="() => Message.info('加入题单功能尚未实现')">
            <template #icon><icon-plus /></template>
            加入题单
          </a-button>
        </div>
      </div>

      <!-- 题目列表区域 - 使用栅格系统 -->
      <a-row class="content-wrapper" :gutter="20" type="flex" align="stretch">
        <!-- 左侧题目列表 -->
        <a-col :span="17">
          <div class="question-list">
            <div class="list-header">
              <icon-menu class="list-icon" />
              <span class="list-title">题目列表</span>
            </div>
            <a-spin :loading="loadingQuestions" style="width: 100%; flex-grow: 1; overflow-y: auto;">
              <!-- 题目表格 -->
              <a-table
                  v-if="!loadingQuestions && questions.length > 0"
                  :ref="tableRef"
                  :columns="columns"
                  :data="questions"
                  :header-cell-class="() => 'bold-header'"
                  @cell-click="toQuestionPage"
                  class="question-table"
                  style="cursor: pointer;"
                  stripe
                  :header-cell-style="{
                    fontWeight: 'bold',
                    whiteSpace: 'nowrap',
                    width: 'auto',
                  }"
                  @page-change="onQuestionPageChange"
                  :pagination="{
                    showTotal: true,
                    pageSize: questionPagination.pageSize,
                    current: questionPagination.current,
                    total: questionPagination.total,
                  }"
                  :scroll="{ x: 500 }"
                  :scrollbar="true"
              >
                <template #status="{ record }">
                  <a-checkbox value="1" disabled></a-checkbox>
                </template>
                <template #title="{ record }">
                  <div class="table-cell-title">{{ record.title }}</div>
                </template>
                <template #difficulty="{ record }">
                  <div class="table-cell-difficulty">
                    {{ record.difficulty }} 星
                  </div>
                </template>
                <template #tags="{ record }">
                  <a-space wrap>
                    <a-tag
                        v-for="(tag, index) of record.tags"
                        :key="index"
                        color="green"
                    >
                      {{ tag }}
                    </a-tag>
                  </a-space>
                </template>
                <template #acceptedRate="{ record }">
                  <a-progress
                      status="warning"
                      size="large"
                      :percent="
                    record.submitNum > 0
                      ? parseFloat(
                          (record.acceptedNum / record.submitNum).toFixed(4)
                        )
                      : 0
                  "
                  />
                </template>
              </a-table>
              <a-empty v-else-if="!loadingQuestions && questions.length === 0" description="暂无题目数据" style="padding-top: 50px;"/>
            </a-spin>
            <!-- 分页组件 (已注释掉) -->
            <!--            <div style="display: flex; justify-content: center; padding: 20px 0 0 0;" v-if="!loadingQuestions && questions.length > 0 && questionPagination.total > questionPagination.pageSize">-->
            <!--              <a-pagination-->
            <!--                  :current="questionPagination.current"-->
            <!--                  :total="questionPagination.total"-->
            <!--                  :page-size="questionPagination.pageSize"-->
            <!--                  @change="onQuestionPageChange"-->
            <!--              />-->
            <!--            </div>-->
          </div>
        </a-col>

        <!-- 右侧详情面板 -->
        <a-col :span="7">
          <div class="detail-panel">
            <!-- 概述区域 -->
            <div class="panel-section">
              <h3 class="panel-title">
                <icon-info-circle class="panel-icon" />
                <span>概述</span>
              </h3>
              <ul class="overview-list">
                <li class="overview-item">
                  <icon-edit class="item-icon" />
                  <span class="item-text">键盘输入，为程序注入数据</span>
                </li>
                <li class="overview-item">
                  <icon-file class="item-icon" />
                  <span class="item-text">屏幕输出，精准反馈结果</span>
                </li>
                <li class="overview-item">
                  <icon-code class="item-icon" />
                  <span class="item-text">借简单代码，速学输入输出</span>
                </li>
                <li class="overview-item">
                  <icon-arrow-right class="item-icon" />
                  <span class="item-text">打牢基础，迈向复杂程序</span>
                </li>
              </ul>
            </div>

            <!-- 成就区域 -->
            <div class="panel-section">
              <h3 class="panel-title">
                <icon-trophy class="panel-icon" />
                <span>成就</span>
              </h3>
              <div class="achievement-badge">
                <img style="width: 120px" :src="image2"/>
              </div>
            </div>
          </div>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  Button as AButton,
  Radio as ARadio,
  Avatar as AAvatar,
  Message,
  Spin as ASpin,
  Empty as AEmpty,
  Pagination as APagination,
  Row as ARow, // 导入 Row
  Col as ACol,   // 导入 Col
} from '@arco-design/web-vue';
import {
  IconLeft,
  IconShareAlt,
  IconPlus,
  IconMenu,
  IconInfoCircle,
  IconEdit,
  IconFile,
  IconCode,
  IconArrowRight,
  IconUser,
  IconUserGroup,
  IconTrophy,
} from '@arco-design/web-vue/es/icon';
import { getQuestionByTagId, TagVO } from '@/api/tag';
import { QuestionVO } from "@/api/question";

import image2 from "@/assets/image-2.svg";
import image5 from "@/assets/image-5.svg";

const route = useRoute();
const router = useRouter();

const tableRef = ref(); // 表格引用
// 表格列配置
const columns = [
  { title: "状态", slotName: "status" },
  { title: "题目名称", slotName: "title" },
  { title: "难度", slotName: "difficulty" },
  { title: "标签", slotName: "tags" },
  { title: "通过率", slotName: "acceptedRate" },
];

const tagIdFromRoute = ref<string | null>(null);
const tagName = ref<string>('题单加载中...');
const tagDescription = ref<string>('');

const questions = ref<QuestionVO[]>([]);
const selectedQuestion = ref<number | null>(null);
const loadingQuestions = ref(false);

const questionPagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
});

const loadQuestionsByTagId = async (currentTagId: string) => {
  if (!currentTagId) return;
  loadingQuestions.value = true;
  console.log(currentTagId, 'currentTagId');
  try {
    const res:any = await getQuestionByTagId(
        currentTagId,
        questionPagination.value.current,
        questionPagination.value.pageSize
    );
    if (res) {
      questions.value = res.records || [];
      questionPagination.value.total = res.total || 0;
    } else {
      Message.error('加载题目列表失败');
      questions.value = [];
      questionPagination.value.total = 0;
    }
  } catch (error) {
    Message.error('加载题目列表时发生网络错误');
    console.error("Error loading questions by tag ID:", error);
    questions.value = [];
    questionPagination.value.total = 0;
  } finally {
    loadingQuestions.value = false;
  }
};

const initialLoad = () => {
  const newTagIdParam = route.params.tagId;
  const newId = Array.isArray(newTagIdParam) ? newTagIdParam[0] : newTagIdParam;
  console.log(newId, 'newId');
  if (newId !== "") {
    tagIdFromRoute.value = newId;
    tagName.value = (route.query.name as string) || `题单 #${newId}`;
    tagDescription.value = (route.query.description as string) || `与此题单相关的题目列表`;
    questionPagination.value.current = 1;
    loadQuestionsByTagId(newId);
  } else if (route.name === 'TagQuestionList') {
    Message.error('未提供题单ID');
    router.replace({ path: '/questions' });
  }
};

initialLoad();

const handleBack = () => {
  router.back();
};

const toQuestionPage = (question: QuestionVO) => {
  if (question && question.id) {
    router.push({
      path: `/view/question/${question.id}`,
    });
  }
};

const onQuestionPageChange = (page: number) => {
  questionPagination.value.current = page;
  if (tagIdFromRoute.value) {
    loadQuestionsByTagId(tagIdFromRoute.value);
  }
};

const joinCount = ref(Math.floor(Math.random() * 800) + 200);
const achievementUrl = ref<string | undefined>(undefined);

const getDifficultyColor = (difficulty?: string) => { // difficulty is now explicitly string | undefined
  if (!difficulty) return '#86909c';
  const lowerDifficulty = difficulty.toLowerCase();
  if (lowerDifficulty.includes('简单') || lowerDifficulty.includes('easy') || lowerDifficulty.includes('入门')) return '#00b42a';
  if (lowerDifficulty.includes('中等') || lowerDifficulty.includes('medium')) return '#ff7d00';
  if (lowerDifficulty.includes('困难') || lowerDifficulty.includes('hard')) return '#f53f3f';
  return '#86909c';
};

</script>

<style scoped>
/* Ensure container-box takes full height if needed, or adjust as per your layout */
.container-box {
  width: 100%;
  background-color: #f0f2f5;
  /* min-height: 100vh; */ /* Or specific height */
  /* display: flex; */ /* If .container needs to fill it */
  /* flex-direction: column; */
}

.container {
  width: 85%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  /* min-height: 100vh; */ /* Removed from here, manage height on container-box or body/html */
  /* height: calc(100vh - 100px); /Example: Adjust based on global header/footer  */
  background-color: #f0f2f5;
  color: #333;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 25px;
  margin-top: 20px;
  margin-bottom: 20px;
  background-color: #fff;
  border-bottom: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border-radius: 8px;
  flex-shrink: 0; /* Prevent header from shrinking */
}

.back-btn, .share-btn {
  color: #555;
  font-size: 16px;
}
.back-btn:hover, .share-btn:hover {
  background-color: #f0f2f5 !important;
  color: #165dff !important;
}

.title-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  flex-shrink: 0; /* Prevent title from shrinking */
}

.title-left {
  display: flex;
  align-items: center;
}

.badge-icon {
  margin-right: 20px;
  display: flex;
  align-items: center;
}

.main-title {
  font-size: 26px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 4px;
}

.sub-title {
  font-size: 14px;
  color: #86909c;
}

.header-actions {
  display: flex;
}

.add-btn {
  font-size: 14px;
  height: 36px;
}
.add-btn .arco-icon {
  font-size: 16px;
}

.content-wrapper {
  flex-grow: 1; /* Allow content-wrapper to fill remaining space */
  overflow: hidden; /* Prevent content from overflowing container */
  margin: 20px; /* Replaces individual margins for consistent spacing */
  /* gap: 20px; */ /* gutter in a-row handles this */
}

.question-list, .detail-panel {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  height: 100%; /* Make columns take full height of the row */
  display: flex; /* Enable flex for internal layout */
  flex-direction: column; /* Stack children vertically */
}

.question-list {
  /* width: 100%; */ /* Col span handles width */
  /* overflow-y: auto; */ /* Moved to a-spin or internal scrollable element */
}

.list-header {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #e8e8e8;
  background-color: #fafafa;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  flex-shrink: 0; /* Prevent header from shrinking */
}

.list-icon {
  margin-right: 10px;
  font-size: 18px;
  color: #4e5969;
}

.list-title {
  font-size: 16px;
  font-weight: 500;
  color: #1d2129;
}

.question-ul {
  list-style: none;
  padding: 0;
  margin: 0;
  /* flex-grow: 1; */ /* a-spin will handle this area */
  /* overflow-y: auto; */ /* Moved to a-spin */
}
/* Styling for a-spin to make its content scrollable */
.question-list .arco-spin {
  flex-grow: 1;
  overflow-y: auto; /* Make the content within a-spin scrollable */
}


.question-item {
  display: flex;
  align-items: center;
  padding: 10px 20px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
}
.question-item:last-child {
  border-bottom: none;
}
.question-item:hover {
  background-color: #f7f8fa;
}

.radio-btn {
  display: flex;
  align-items: center;
  width: 100%;
  color: #4e5969;
}
:deep(.arco-radio-label) {
  display: flex;
  flex-grow: 1;
  align-items: center;
  justify-content: space-between;
  color: inherit;
}


.question-id {
  margin-right: 15px;
  font-size: 14px;
  color: #86909c;
  font-weight: 500;
}

.question-name {
  flex-grow: 1;
  font-size: 14px;
  color: #1d2129;
  margin-right: 15px;
  overflow: hidden; /* Prevent long names from breaking layout */
  text-overflow: ellipsis; /* Add ellipsis for long names */
  white-space: nowrap; /* Keep name on one line */
}

.question-meta {
  display: flex;
  align-items: center;
  font-size: 12px;
  white-space: nowrap;
  flex-shrink: 0; /* Prevent meta from shrinking too much */
}

.question-star {
  margin-right: 15px;
  font-weight: 500;
}

.question-pass-rate {
  color: #86909c;
}

.detail-panel {
  /* width: 100%; */ /* Col span handles width */
  padding: 0; /* Remove padding, sections will have their own */
  overflow-y: auto; /* Allow detail panel to scroll if content exceeds height */
  gap: 20px; /* Gap between sections in detail panel */
}

.panel-section {
  background-color: #fff;
  border-radius: 6px;
  padding: 20px;
  color: #333;
  border: 1px solid #e8e8e8;
  margin: 20px; /* Add margin to sections inside detail-panel */
}
.detail-panel .panel-section:first-child {
  margin-top: 0;
}
.detail-panel .panel-section:last-child {
  margin-bottom: 0;
}


.panel-title {
  display: flex;
  align-items: center;
  margin-bottom: 18px;
  font-size: 17px;
  font-weight: 500;
  color: #1d2129;
}

.panel-icon {
  margin-right: 10px;
  font-size: 20px;
  color: #165dff;
}

.overview-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.overview-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.overview-item:last-child {
  margin-bottom: 0;
}

.item-icon {
  margin-right: 10px;
  font-size: 16px;
  color: #165dff;
  width: 20px;
  text-align: center;
}

.item-text {
  font-size: 14px;
  color: #4e5969;
}

.join-count {
  display: flex;
  align-items: center;
  padding-top: 10px;
}
.join-count .arco-avatar {
  margin-right: 12px;
}

.count-text {
  font-size: 14px;
  color: #4e5969;
}

.achievement-badge {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}
.achievement-badge .arco-avatar .arco-icon {
  font-size: 48px !important;
  color: #c9cdd4;
}

/* Responsive adjustments */
@media (max-width: 992px) {
  .content-wrapper .arco-col { /* Target columns directly for responsive span */
    flex-basis: 100% !important; /* Make columns full width */
    max-width: 100% !important;
    margin-bottom: 20px; /* Add space between stacked columns */
  }
  .content-wrapper .arco-col:last-child {
    margin-bottom: 0;
  }
  .question-list, .detail-panel {
    height: auto; /* Adjust height for stacked layout */
  }
}

@media (max-width: 768px) {
  .container {
    width: 90%;
  }

  .header, .title-container {
    padding: 15px;
  }
  .content-wrapper {
    margin: 10px;
  }
  .main-title {
    font-size: 22px;
  }
  .sub-title {
    font-size: 13px;
  }
  .panel-section {
    padding: 15px;
    margin: 10px; /* Adjust margin for smaller screens */
  }
  .panel-title {
    font-size: 16px;
  }
  .item-text, .count-text, .question-name, .question-id {
    font-size: 13px;
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

</style>