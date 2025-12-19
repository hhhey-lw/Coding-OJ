<template>
  <!-- 页面主容器 -->
  <div id="questionsView">
    <a-row :gutter="[16, 16]">
      <!-- 左侧主内容区域 -->
      <a-col :xs="24" :sm="24" :md="17" :lg="17" :xl="17">
        <a-row :gutter="[16, 16]">
          <!-- 推荐题单区域 -->
          <a-col :span="24">
            <div class="recommend-section">
              <div class="recommend-section-header">
                <h2 class="section-title">推荐题单</h2>
                <a-button class="view-more" @click="viewMore">查看更多 >
                </a-button>
              </div>
              <div class="card-grid">
                <a-card
                    v-for="(card, index) in recommendCards"
                    :key="index"
                    class="card"
                    :class="card.color"
                    @click="toTagQuestionPage(card.tagId)"
                >
                  <div class="custom-card-layout">
                    <img
                        :src="card.icon"
                        alt="icon"
                        class="custom-card-icon"
                    />
                    <div class="custom-card-text-content">
                      <div class="custom-card-title">{{ card.title }}</div>
                      <div class="custom-card-description">
                        {{ card.description }}
                      </div>
                    </div>
                  </div>
                </a-card>
              </div>
            </div>
          </a-col>

          <!-- 题目列表区域 -->
          <a-col :span="24">
            <!-- 搜索表单 -->
            <a-form
                :model="searchParams"
                layout="inline"
                class="search-form-container"
            >
              <a-form-item
                  field="title"
                  label="搜索："
                  class="search-form-item"
              >
                <a-input
                    v-model="searchParams.title"
                    class="len-input-class"
                    placeholder="搜索题目标题"
                />
              </a-form-item>
              <a-form-item class="search-form-button-item">
                <button class="button-class" @click="doSubmit">搜索</button>
              </a-form-item>
            </a-form>
            <a-divider />

            <!-- 题目表格 -->
            <a-table
                :ref="tableRef"
                :columns="columns"
                :data="dataList"
                :pagination="{
                  showTotal: true,
                  pageSize: searchParams.pageSize,
                  current: searchParams.current,
                  total,
                }"
                :header-cell-class="() => 'bold-header'"
                @page-change="onPageChange"
                @cell-click="toQuestionPage"
                class="question-table"
                stripe
                :header-cell-style="{
                fontWeight: 'bold',
                whiteSpace: 'nowrap',
                width: 'auto',
              }"
                :scroll="{ x: 500 }"
                :scrollbar="true"
            >
              <template #status="{ record }">
                <a-checkbox :model-value="record.passed" disabled ></a-checkbox>
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
            <a-divider />
          </a-col>
        </a-row>
      </a-col>

      <!-- 右侧边栏区域 -->
      <a-col :xs="24" :sm="24" :md="7" :lg="7" :xl="7">
        <a-row :gutter="[16, 16]">
          <!-- 用户打卡记录区域 -->
          <a-col :span="24">
            <UserCheckInView class="user-check-in-view" />
          </a-col>

          <!-- 标签筛选区域 -->
          <a-col :span="24">
            <div class="tag-filter-section">
              <h3 class="tag-section-title">
                <icon-tags /> 按标签筛选
              </h3>
              <a-space wrap align="start">
                <a-tag
                    checkable
                    v-for="tag in allTags"
                    :key="tag.id"
                    :checked="isTagSelected(tag.tagName!)"
                    @check="(checked: boolean) => handleTagChange(tag.tagName!, checked)"
                    class="custom-checkable-tag"
                >
                  {{ tag.tagName }}
                </a-tag>
              </a-space>
              <button
                  v-if="searchParams.tags && searchParams.tags.length > 0"
                  @click="clearSelectedTags"
                  class="button-class button-class-clear clear-tags-button"
              >
                <icon-close /> 清除选择
              </button>
            </div>
          </a-col>
        </a-row>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
// ------------------------
// 导入模块
// ------------------------
import { onMounted, ref, watchEffect } from "vue";
import { useRouter } from "vue-router";
import UserCheckInView from "@/views/user/UserCheckInView.vue";
import { Card as ACard, Space as ASpace } from "@arco-design/web-vue";
import { IconTags, IconClose } from "@arco-design/web-vue/es/icon";
import {
  QuestionControllerService,
  QuestionQueryRequest,
  TagControllerService,
  TagVO,
  QuestionVO,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";

// 导入静态资源 (推荐题单图标)
import image1 from "@/assets/image-1.svg";
import image2 from "@/assets/image-2.svg";
import image3 from "@/assets/image-3.svg";
import image4 from "@/assets/image-4.svg";
import image5 from "@/assets/image-5.svg";
import image6 from "@/assets/image-6.svg";

// ------------------------
// 组件状态与引用
// ------------------------
const tableRef = ref(); // 表格引用
const dataList = ref<QuestionVO[]>([]); // 题目列表数据
const total = ref(0); // 题目总数
const searchParams = ref<QuestionQueryRequest>({ // 搜索参数
  title: "",
  tags: [],
  pageSize: 10,
  current: 1,
});
const allTags = ref<TagVO[]>([]); // 所有可用标签列表

// ------------------------
// 静态数据
// ------------------------
// 推荐题单数据
const recommendCards = [
  {
    title: "基本输入输出",
    description: "搭建程序交互桥梁，开启输入输出之旅",
    icon: image1,
    color: "green-card",
    tagId: "1935029007218974722"
  },
  {
    title: "循环结构练习",
    description: "从基础到进阶，全方位突破循环逻辑",
    icon: image2,
    color: "orange-card",
    tagId: "1935029007218974722"
  },
  {
    title: "动态规划经典题型",
    description: "探秘动态规划难题",
    icon: image3,
    color: "pink-card",
    tagId: "1935029007218974722"
  },
  {
    title: "递归练习汇总",
    description: "递归基础练习题集锦",
    icon: image4,
    color: "blue-card",
    tagId: "1935029007218974722"
  },
  {
    title: "排序与排列组合",
    description: "排序与排列组合妙题",
    icon: image5,
    color: "purple-card",
    tagId: "1935029007218974722"
  },
  {
    title: "探寻数学算法的奥秘",
    description: "解锁算法思路，攻克数学难题",
    icon: image6,
    color: "teal-card",
    tagId: "1935029007218974722"
  },
];

// 表格列配置
const columns = [
  { title: "状态", slotName: "status" },
  { title: "题目名称", slotName: "title" },
  { title: "难度", slotName: "difficulty" },
  { title: "标签", slotName: "tags" },
  { title: "通过率", slotName: "acceptedRate" },
];

// ------------------------
// 数据加载逻辑
// ------------------------
/**
 * 加载题目列表数据
 */
const loadData = async () => {
  if (!searchParams.value.tags) {
    searchParams.value.tags = []; // 确保tags始终是一个数组
  }
  try {
    const res = await QuestionControllerService.listQuestionVoByPageUsingPost(
        searchParams.value
    );
    if (res.code === 0) {
      dataList.value = res.data.records as QuestionVO[];
      total.value = res.data.total;
    } else {
      message.error("加载题目列表失败，" + res.message);
    }
  } catch (error) {
    message.error("加载题目列表时发生错误");
    console.error("Error loading question list:", error);
  }
};

/**
 * 加载所有标签数据
 */
const loadAllTags = async () => {
  try {
    // 假设一页获取足够多的标签，更优方案是后端提供获取所有标签的接口
    const res = await TagControllerService.getTagByPageUsingGet(1, 200); // 增加获取数量
    if (res.code === 0 && res.data?.records) {
      allTags.value = res.data.records.filter(
          (tag): tag is TagVO & { tagName: string } => !!tag.tagName
      );
    } else {
      message.error("加载标签列表失败：" + (res.message || "未知错误"));
    }
  } catch (error) {
    message.error("加载标签列表时出错，请查看控制台");
    console.error("Error loading tags:", error);
  }
};

// ------------------------
// 生命周期钩子与侦听器
// ------------------------
/**
 * 监听 searchParams 变量，改变时触发页面的重新加载
 */
watchEffect(() => {
  loadData();
});

/**
 * 页面加载时，请求初始数据
 */
onMounted(() => {
  // loadData(); // watchEffect会首次执行，无需手动调用
  loadAllTags();
});

// ------------------------
// UI交互与事件处理
// ------------------------
/**
 * "查看更多"推荐题单
 */
function viewMore() {
  message.warning("此功能尚未实现，敬请期待！");
}

/**
 * 表格分页变化处理
 * @param page 当前页码
 */
const onPageChange = (page: number) => {
  searchParams.value = {
    ...searchParams.value,
    current: page,
  };
};

/**
 * 确认搜索，重新加载数据
 */
const doSubmit = () => {
  searchParams.value = {
    ...searchParams.value,
    current: 1, // 搜索时重置到第一页
  };
  // loadData() 将由 watchEffect 触发
};

// ------------------------
// 路由逻辑
// ------------------------
const router = useRouter();

/**
 * 跳转到题目详情页面
 * @param question 题目对象
 */
const toQuestionPage = (question: QuestionVO) => {
  if (question && question.id) {
    router.push({
      path: `/view/question/${question.id}`,
    });
  }
};

function toTagQuestionPage(tagId: string) {
  if (tagId) {
    console.log(tagId, "toTagQuestionPage");
    router.push({
      path: `/questions/tag/${tagId}`
    });
  }
}

// ------------------------
// 标签筛选逻辑
// ------------------------
/**
 * 检查标签是否被选中
 * @param tagName 标签名称
 */
const isTagSelected = (tagName: string): boolean => {
  return searchParams.value.tags?.includes(tagName) ?? false;
};

/**
 * 处理标签选中状态变化
 * @param tagName 标签名称
 * @param checked 是否选中
 */
const handleTagChange = (tagName: string, checked: boolean) => {
  const currentSelectedTags = searchParams.value.tags
      ? [...searchParams.value.tags]
      : [];
  if (checked) {
    if (!currentSelectedTags.includes(tagName)) {
      currentSelectedTags.push(tagName);
    }
  } else {
    const index = currentSelectedTags.indexOf(tagName);
    if (index > -1) {
      currentSelectedTags.splice(index, 1);
    }
  }
  searchParams.value.tags = currentSelectedTags;
  searchParams.value.current = 1; // 标签变化时，重置到第一页
  // loadData() 将由 watchEffect 触发
};

/**
 * 清除所有选中的标签
 */
const clearSelectedTags = () => {
  searchParams.value.tags = [];
  searchParams.value.current = 1; // 重置到第一页
  // loadData() 将由 watchEffect 触发
};
</script>

<style scoped>
/* ------------------------ */
/* 页面整体布局 */
/* ------------------------ */
#questionsView {
  width: 80%;
  margin: 0 auto;
}

/* ------------------------ */
/* 表格相关样式 */
/* ------------------------ */
.question-table {
  cursor: pointer;
  min-height: 400px; /* 防止数据加载时晃动 */
}

:deep(.bold-header) {
  font-weight: bold !important;
  background-color: #f7f8fa;
  color: #333;
}

:deep(.arco-table-th) {
  font-weight: bold !important; /* 确保表头加粗 */
}

.table-cell-title {
  color: #333;
}

.table-cell-difficulty {
  color: darkcyan;
}

:deep(.arco-table-pagination) {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

/* ------------------------ */
/* 搜索表单样式 */
/* ------------------------ */
.search-form-container {
  display: flex;
  margin-bottom: -1rem;
  margin-top: 0.5rem;
  flex-direction: row;
  justify-content: start;
  align-content: center;
}

.search-form-item {
  font-weight: bold;
  display: flex;
}

.search-form-button-item {
  display: inline;
}

/* ------------------------ */
/* 推荐题单区域样式 */
/* ------------------------ */
.recommend-section {
  margin-bottom: 30px;
}

.recommend-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  text-align: center; /* 保持标题居中 */
  margin-bottom: 24px; /* 与原样式保持一致 */
  font-size: 24px;
  color: #1d2129;
  padding-bottom: 1rem; /* 与原样式保持一致 */
  border-bottom: 2px solid #22bfa7; /* 与原样式保持一致 */
}

.view-more {
  background-color: white; /* 与原样式保持一致 */
  color: #606266; /* 可选：调整文字颜色 */
}
.view-more:hover {
  border-color: #22bfa7;
  color: #22bfa7;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

/* ------------------------ */
/* 卡片通用样式 */
/* ------------------------ */
.card {
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s;
  cursor: pointer;
}

.card:hover {
  transform: translateY(-4px);
}

/* 自定义卡片内部布局 */
.custom-card-layout {
  display: flex;
  align-items: center;
}

.custom-card-icon {
  width: 48px;
  height: 48px;
  object-fit: contain;
  margin-right: 16px;
  flex-shrink: 0;
}

.custom-card-text-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex-grow: 1;
}

.custom-card-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.custom-card-description {
  font-size: 14px;
  color: #666;
}

/* 卡片颜色主题 */
.green-card {
  background-color: #e6f7ff;
}
.orange-card {
  background-color: #fff7e6;
}
.pink-card {
  background-color: #fff0f6;
}
.blue-card {
  background-color: #e6f3ff;
}
.purple-card {
  background-color: #f3e6ff;
}
.teal-card {
  background-color: #e6f7f7;
}

/* 兼容旧的 Arco Card Meta 样式 (如果内部仍有使用) */
.card :deep(.arco-card-meta-title) {
  font-size: 16px;
  margin-bottom: 4px;
}

.card :deep(.arco-card-meta-description) {
  font-size: 14px;
  color: #666;
}

.card :deep(.arco-card-body) { /* 调整arco card body的padding */
  padding: 16px;
}


/* ------------------------ */
/* 用户打卡组件样式 */
/* ------------------------ */
.user-check-in-view {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border-radius: 12px;
  width: 100%;
}

/* ------------------------ */
/* 标签筛选区域样式 */
/* ------------------------ */
.tag-filter-section {
  margin-top: 20px;
  margin-bottom: 20px;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.tag-section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}

.tag-section-title .arco-icon {
  margin-right: 8px;
  font-size: 20px;
  color: #165dff; /* Arco Blue */
}

.custom-checkable-tag {
  margin: 4px;
  padding: 5px 10px;
  font-size: 14px;
  border-radius: 4px;
  transition: all 0.2s ease-in-out;
  cursor: pointer;
  border: 1px solid #d9d9d9;
  background-color: #f7f8fa;
  color: #4e5969;
}

.custom-checkable-tag:hover {
  border-color: #165dff;
  color: #165dff;
}

.custom-checkable-tag.arco-tag-checked {
  background-color: #e8f3ff !important;
  color: #165dff !important;
  border-color: #165dff !important;
}

.clear-tags-button {
  margin-top: 12px;
  margin-left: 5px;
}

/* ------------------------ */
/* 输入框和按钮样式 */
/* ------------------------ */
.len-input-class {
  /* 默认宽度，大屏幕下会被媒体查询覆盖 */
}

.button-class {
  white-space: nowrap;
  background-color: #8ebc8e;
  border: 2px solid #2d8a55;
  border-radius: 5%; /* 建议使用px或rem以获得更一致的圆角 */
  height: 36px;
  color: white;
  width: 5rem;
  font-size: 16px;
  cursor: pointer; /* 添加指针样式 */
  transition: background-color 0.2s, border-color 0.2s; /* 添加过渡效果 */
}

.button-class:hover {
  background-color: #2d8a55;
  border-color: #1c5d39;
}

.button-class-clear {
  width: 6rem;
  font-size: 12px;
  height: 2rem;
  /* border: 2px solid #2d8a55; (继承自 .button-class) */
  margin: 0 !important;
  margin-left: 0.5rem !important;
}

/* ------------------------ */
/* 响应式布局 */
/* ------------------------ */
@media screen and (min-width: 768px) {
  .len-input-class {
    min-width: 500px; /* 大屏幕下搜索框最小宽度 */
  }
}

@media (max-width: 768px) {
  #questionsView {
    width: 95%; /* 小屏幕上增加内容区域宽度 */
  }

  .search-form-container {
    flex-direction: column; /* 搜索表单垂直排列 */
    align-items: stretch; /* 拉伸表单项以填充宽度 */
    margin-bottom: 1rem;
  }
  .search-form-item {
    margin-bottom: 0.5rem; /* 垂直排列时增加间距 */
  }
  .search-form-button-item button {
    width: 100%; /* 搜索按钮宽度100% */
  }

  .card-grid {
    grid-template-columns: 1fr; /* 小屏幕推荐卡片单列显示 */
    gap: 12px;
  }

  .card {
    margin-bottom: 12px;
  }

  .tag-filter-section {
    padding: 15px;
  }

  .tag-section-title {
    font-size: 17px;
  }

  .custom-checkable-tag {
    padding: 4px 8px;
    font-size: 13px;
  }

  /* 针对移动端表格的特定优化 (如果需要表格横向滚动) */
  .mobile-responsive-table {
    width: 100%;
    overflow-x: auto; /* 改为auto，内容超出时才显示滚动条 */
    -webkit-overflow-scrolling: touch;
  }

  .mobile-responsive-table :deep(.arco-table) {
    min-width: 100%; /* 确保表格内容可以撑开 */
    width: auto; /* 允许表格根据内容调整宽度 */
  }

  .mobile-responsive-table :deep(.arco-table-th),
  .mobile-responsive-table :deep(.arco-table-td) {
    white-space: nowrap; /* 防止表头和单元格内容换行 */
    /* min-width: 120px; /* 可选：为单元格设置最小宽度 */
    padding: 8px 12px;
  }
}
</style>