<template>
  <a-space direction="vertical" size="large" class="mobile-class box-class" style="margin: 0 auto; display: flex; justify-content: center;">
    <!-- 搜索与发帖 -->
    <a-row justify="space-between" style="display: flex; flex-direction: row">
      <div class="mobile-header-class">
        <a-input
            placeholder="搜索讨论..."
            v-model="queryParams.searchKey"
            class="discussion-search"
        />
        <button class="btn-class search-class" @click="doSearchPost">搜索</button>
      </div>
      <button class="btn-class" @click="submitPost">+ 发布讨论</button>
    </a-row>

    <!-- 筛选标签 -->
    <a-row justify="space-between">
      <a-space>
        <a-tag checkable :checked="true" :checkable="false">置顶</a-tag>
        <a-tag checkable :checkable="false">官方</a-tag>
      </a-space>
      <a-radio-group v-model="sortType" type="button" @change="handleRadioChange">
        <a-radio value="latest">最新</a-radio>
        <a-radio value="hot">最热</a-radio>
      </a-radio-group>
    </a-row>

    <!-- 帖子列表 -->
    <div v-for="item in currentPageData" :key="item.id">
      <a-card :bordered="true"
              :style="{
          borderRadius: '12px',
          boxShadow: '0 2px 2px rgba(0, 0, 0, 0.1)',
          marginBottom: '16px'
        }"
        :body-style="{
          padding: '16px'
        }"
        @click="toDetailDiscussion(item.id)"
        style="cursor: pointer">
        <a-space direction="vertical" style="width: 100%;">
          <!-- 卡片内容保持不变... -->
          <!-- 标签 -->
          <a-space wrap>
            <!--       :color="tag.color"      -->
            <a-tag v-for="(tag,index) in item.tagList" :key="tag" :color="tagColor[index]">
              {{ tag }}
            </a-tag>
          </a-space>

          <!-- 标题 -->
          <a-typography-title :heading="5">{{ item.title }}</a-typography-title>

          <!-- 内容摘要 -->
<!--          <div>-->
<!--            <Viewer :value="item.content" :plugins="plugins"/>-->
<!--          </div>-->
          <a-typography-paragraph :ellipsis="{ rows: 2 }">
            {{ parseMarkdownPreview(item.content) }}
          </a-typography-paragraph>

          <!-- 底部信息 -->
          <a-row
              type="flex"
              align="middle"
              justify="space-between"
              style="width: 100%"
          >
            <a-space>
              <a-avatar v-if="item.user">
                <img v-if="item.user.userAvatar" :src="item.user.userAvatar" :size="24" />
                <icon-user v-else />
              </a-avatar>
              <span v-if="item.user">{{ item.user.userName }}</span>
            </a-space>
            <a-space>
              <a-space>
                <svg viewBox="0 0 24 24" width="16" height="16">
                  <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                </svg>{{ item.viewNum || 0 }}
              </a-space>
              <a-space>
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/>
                </svg>
                {{ item.commentNum }}
              </a-space>
              <a-space>
                <span class="icon-hover"> <IconThumbUpFill /> </span>
                {{ item.thumbNum }}
              </a-space>
              <span>&nbsp&nbsp{{ formatUtcDateTime(item.createTime) }}</span>
            </a-space>
          </a-row>
        </a-space>
      </a-card>
    </div>

    <!-- 分页导航 -->
    <a-pagination
        :current="queryParams.current"
        :total="queryParams.total"
        :page-size="queryParams.pageSize"
        show-total
        :style="{ marginTop: '20px', justifyContent: 'center' }"
        @change="handlePageChange"
    />
  </a-space>
</template>

<script setup lang="ts">
import {ref, computed, onMounted} from 'vue'
import {listPostVOByPage, PostQueryRequest, PostVO} from "@/api/discussion";
import { useRouter } from "vue-router";
import {
  IconThumbUpFill,
  IconUser
} from '@arco-design/web-vue/es/icon';
import * as path from "node:path";

// import { Viewer } from '@bytemd/vue-next'
// import frontmatter from "@bytemd/plugin-frontmatter";
// const plugins = [frontmatter()];

// =====> 变量定义 <=====
const router = useRouter();

const sortType = ref('latest')

let queryParams = ref({
  current: 1,
  pageSize: 4,
  total: 0,
  searchKey: '',
  sortField: 'create_time',
  sortOrder: 'DESC'
})

let postList = ref<PostVO[]>([]);

const tagColor = [
  "#19be6b",
  "#ed4014",
  "#ff9900",
  "#2d8cf0"
]


// =====> 生命周期 <=====
onMounted(() => {
  loadPostData();
});

// =====> 函数定义 <=====
const loadPostData = async () => {
  try {
    console.log("加载数据中...")
    const res:any = await listPostVOByPage({
      current: queryParams.value.current,
      pageSize: queryParams.value.pageSize,
      searchKey: queryParams.value.searchKey,
      sortField: queryParams.value.sortField,
      sortOrder: queryParams.value.sortOrder
    });
    console.log(res)
    if (res && res.records) {
      postList.value = res.records; // 使用.value更新响应式数据
      queryParams.value.total = res.total;
    }
  } catch (error) {
    console.error('加载数据失败:', error);
  }
}

// 计算当前页显示的数据
const currentPageData = computed(() => {
  return postList.value
})

// 时间格式化
const formatUtcDateTime = (isoString: string): string => {
  // 验证输入
  if (!isoString || typeof isoString !== 'string') {
    console.warn('Invalid input: Expected ISO 8601 date string');
    return 'Invalid date';
  }

  const date = new Date(isoString);

  // 验证日期是否有效
  if (isNaN(date.getTime())) {
    console.warn('Invalid date format: Could not parse date');
    return 'Invalid date';
  }

  const pad = (num: number): string => num.toString().padStart(2, '0');

  // 使用 getUTC* 方法获取 UTC 时间
  const year = date.getUTCFullYear();
  const month = pad(date.getUTCMonth() + 1);
  const day = pad(date.getUTCDate());
  const hours = pad(date.getUTCHours());
  const minutes = pad(date.getUTCMinutes());
  const seconds = pad(date.getUTCSeconds());

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

// 页码变化处理
const handlePageChange = (page: number) => {
  queryParams.value.current = page
  loadPostData();
  // 这里可以添加滚动到顶部的逻辑
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleRadioChange = (value: string) => {
  console.log("Radio Change: ", value);
  if (value === 'hot') {
    queryParams.value.searchKey = '';
    queryParams.value.sortField = 'thumb_num';
    queryParams.value.sortOrder = 'DESC';
    queryParams.value.current = 1;
    loadPostData();
  }
  else {
    queryParams.value.searchKey = '';
    queryParams.value.sortField = 'create_time';
    queryParams.value.sortOrder = 'DESC';
    queryParams.value.current = 1;
    loadPostData();
  }
}

const doSearchPost = () => {
  console.log("doSearchPost : ", queryParams.value.searchKey);
  try {
    if (queryParams.value.searchKey === '') {
      alert("查询内容不能为空！");
      return ;
    }
    queryParams.value.sortField = 'create_time';
    queryParams.value.sortOrder = 'DESC';
    queryParams.value.current = 1;
    loadPostData();
    } catch (e) {
    alert("查询失败！");
  }
}

// 解析Markdown预览
// 解析 Markdown 为纯文本预览（只保留前两行）
const parseMarkdownPreview = (markdown?: string): string => {
  if (!markdown) return '';

  // 移除代码块
  let text = markdown.replace(/```[\s\S]*?```/g, '[代码块]');

  // 移除图片
  text = text.replace(/!\[.*?\]\(.*?\)/g, '[图片]');

  // 移除链接但保留文本
  text = text.replace(/\[(.*?)\]\(.*?\)/g, '$1');

  // 移除标题符号 #
  text = text.replace(/^#+\s/gm, '');

  // 移除粗体、斜体等格式
  // text = text.replace(/\*\*(.*?)\*\*/g, '$1');
  // text = text.replace(/\*(.*?)\*/g, '$1');
  // text = text.replace(/__(.*?)__/g, '$1');
  // text = text.replace(/_(.*?)_/g, '$1');

  // 移除表情符号
  // text = text.replace(/:[a-z_]+:/g, '');

  // 保留复选框文本
  text = text.replace(/- \[([ x])\] (.*)/g, '• $2');

  // 保留普通列表文本
  text = text.replace(/- (.*)/g, '• $1');
  text = text.replace(/\d+\. (.*)/g, '• $1');

  // 分割为行
  // const lines = text.split('\n')
  //     .filter(line => line.trim() !== '')  // 过滤空行
  //     .slice(0, 2);  // 只取前两行

  // return lines.join('\n') + (lines.length < 2 && markdown.length > text.length ? '...' : '');
  return text;
};

// =====> 路由部分 <=====
const toDetailDiscussion = (id:number) => {
  router.push({
    path: `/discussion/detail/${id}`
  });
};

const submitPost = () => {
  router.push({
        path: '/discussion/add'
      }
  )
}

</script>

<style>
.box-class{
  width: 80%
}
@media screen and (max-width: 768px)  {
  .mobile-class {
    width: 100%;
  }

  .btn-class {
    width: 6REM;
  }

  .mobile-header-class {
    width: 60% !important;
  }
}

.btn-class {
  background-color: #8ebc8e;
  border: 2px solid #2d8a55;
  border-radius: 5%;
  height: 36px;
  color: white;
  width: 8REM;
  font-size: 14px;
  cursor: pointer;
}

.search-class {
  width: 4REM;
  font-size: 14px;
}

.discussion-search {
  max-width: 60%;
  border: 2px solid #8ebc8e;
}

.discussion-search:focus{
  border: 2px solid #2d8a55 !important;
}

:deep(.arco-table-pagination) {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

</style>