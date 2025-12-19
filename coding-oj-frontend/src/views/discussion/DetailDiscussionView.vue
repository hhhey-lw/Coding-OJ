<template>
  <a-space direction="vertical" size="large" class="mobile-class box-class" style="margin: 0 auto; display: flex; justify-content: center;">
    <!-- 面包屑导航 -->
    <a-breadcrumb class="pointer-class">
      <a-breadcrumb-item @click="toHomePage">首页</a-breadcrumb-item>
      <a-breadcrumb-item @click="toDiscussionPage">讨论</a-breadcrumb-item>
      <a-breadcrumb-item>{{postVO?.title}}</a-breadcrumb-item>
    </a-breadcrumb>

    <!-- 主体内容 -->
    <a-card>
      <!-- 标题 -->
      <a-typography-title :heading="3">
        {{postVO?.title}}
      </a-typography-title>

      <!-- 作者和操作 -->
      <a-space align="center" justify="space-between" style="width: 100%; display: flex; justify-content: space-between">
        <a-space>
          <a-avatar>
            <img :src="postVO?.user?.userAvatar" :size="24" />
          </a-avatar>
          <span class="author-name">{{postVO?.user?.userName}}</span>
          <span class="post-time">{{formatUtcDateTime(postVO?.createTime)}}</span>
        </a-space>
        <a-space>
          <IconThumbUpFill style="font-size: large" v-if="postVO?.hasThumb" @click="thumbPost"/>
          <IconThumbUp style="font-size: large" v-else @click="thumbPost"/>
          <IconStarFill style="font-size: large" v-if="postVO?.hasFavour" @click="favourPost"/>
          <IconStar style="font-size: large" v-else @click="favourPost"/>
<!--          <a-link icon><icon-share-alt /> 分享</a-link>-->
        </a-space>
      </a-space>

      <a-space wrap  style="margin-top: 10px">
        <a-tag v-for="(tag,index) in postVO?.tagList" :key="tag" :color="tagColor[index]">
          {{ tag }}
        </a-tag>
      </a-space>

      <!-- 正文 Markdown -->
      <div style="margin-top: 20px">
        <MdViewer :value="postVO?.content"/>
      </div>
    </a-card>

    <!-- 评论区 -->
    <a-card>
      <a-typography-title :heading="5">评论 ({{ postVO?.commentNum }})</a-typography-title>

      <!-- 评论输入 -->
      <a-textarea v-model="newComment" placeholder="写下你的评论..." auto-size show-word-limit />
      <a-space style="margin-top: 8px" justify="end">
        <button class="send-button" @click="submitComment">发表评论</button>
      </a-space>

      <!-- 评论列表 -->
      <div v-for="item in commentList" :key="item.commentId" style="margin-top: 20px;">
        <a-comment align="right"
                   :author="item.userVO?.userName"
                   :avatar="item.userVO?.userAvatar"
                   :content="item.content"
                   :datetime="formatUtcDateTime(item.createTime)"
        >
          <a-divider/>
          <template #actions>
            <span class="action" style="cursor: pointer" key="reply" @click="toggleReplyInput(item.commentId)">
          <IconMessage /> {{replyStates[item.commentId]? '取消回复' :'回复'}}
        </span>
          </template>

          <!-- 主评论的回复输入框 -->
          <div v-if="replyStates[item.commentId]" class="reply-input-container">
            <a-input
                v-model="replyContents[item.commentId]"
                placeholder="写下你的回复..."
                class="reply-input"
            />
            <button
                @click="submitReply(item)"
                class="send-button"
            >
              发送
            </button>
          </div>

          <!-- 子评论列表 -->
          <a-comment align="right"
                     v-for="subitem in item.replies" :key="subitem.commentId"
                     :author="subitem.userVO?.userName"
                     :avatar="subitem.userVO?.userAvatar"
                     :content="`回复 @${findParentUserName(subitem.parentId, commentList)} ：${subitem.content}`"
                     :datetime="formatUtcDateTime(subitem.createTime)"
          >
            <a-divider/>
            <template #actions>
<!--          <span class="action" key="heart" @click="">-->
<!--            <span v-if="true">-->
<!--              <IconHeartFill :style="{ color: '#f53f3f' }" />-->
<!--            </span>-->
<!--            <span v-else>-->
<!--              <IconHeart />-->
<!--            </span>-->
<!--            {{ 83 }}-->
<!--          </span>-->
              <span class="action" key="reply" @click="toggleReplyInput(subitem.commentId)">
            <IconMessage /> {{replyStates[subitem.commentId] ? '取消回复' : '回复'}}
          </span>
            </template>

            <!-- 子评论的回复输入框 -->
            <div v-if="replyStates[subitem.commentId]" class="reply-input-container">
              <a-input
                  v-model="replyContents[subitem.commentId]"
                  placeholder="写下你的回复..."
                  class="reply-input"
              />
              <button
                  @click="submitSubReply(item, subitem)"
                  class="send-button">
                发送
              </button>
            </div>
          </a-comment>
        </a-comment>
      </div>

      <!-- 分页导航 -->
      <div class="pagination-container">
        <a-pagination
            v-model:current="pageInfo.current"
            v-model:pageSize="pageInfo.pageSize"
            :total="pageInfo.totalComments"
            @change="handlePageChange"
        />
      </div>
    </a-card>
  </a-space>
</template>

<script setup lang="ts">
import {ref, onMounted, UnwrapRef} from 'vue';
import { useRouter } from 'vue-router';
import {
  CommentControllerService,
  CommentPageQueryRequest,
  PostControllerService,
  PostVO,
  CommentVO, CommentAddRequest, PostThumbControllerService, PostFavourControllerService,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import MdViewer from "@/components/MdViewer.vue";
import {
  IconMessage,
  IconThumbUp,
  IconThumbUpFill,
  IconStar,
  IconStarFill
} from '@arco-design/web-vue/es/icon';
import store from "@/store";

// =====> 变量定义 <=====
const router = useRouter();
const postVO = ref<PostVO>()
const commentList = ref<CommentVO[]>([]);

const pageInfo = ref({
  current:1,
  pageSize:5,
  totalComments: -1
})

interface Props {
  id: number; // 这里定义了接收名为id的prop
}

const props = withDefaults(defineProps<Props>(), {
  id: () => 0, // 设置默认值
});

const tagColor = [
  "#19be6b",
  "#ed4014",
  "#ff9900",
  "#2d8cf0"
]

// 响应式状态
const replyStates = ref<Record<number, boolean>>({});
const replyContents = ref<Record<number, string>>({});

// 直接回复Post的评论内容
const newComment = ref('');

// =====> 生命周期 <=====
onMounted(() => {
  console.log('详情页加载完成');
  loadPostData();
  loadCommentData();
});

// =====> 函数定义 <=====
const loadPostData = async () => {
  const res = await PostControllerService.getPostVoByIdUsingGet(
      props.id
  );
  if (res.code === 0) {
    console.log('res.data',res.data);
    postVO.value = res.data;
  } else {
    message.error("加载失败，" + res.message);
  }
};

const loadCommentData = async () => {
  const req:CommentPageQueryRequest = {
    postId: props.id,
    pageSize: pageInfo.value.pageSize,
    current: pageInfo.value.current
  }
  const res = await CommentControllerService.listCommentVoByPageUsingPost(
      req
  );
  if (res.code === 0) {
    console.log('res.data',res.data);
    pageInfo.value.totalComments = res.data.total
    commentList.value = res.data.records;
  } else {
    message.error("加载失败，" + res.message);
  }
};

// 直接回复Post的评论
const submitComment = async () => {
  if (newComment.value == '') {
    console.log("评论内容不能为空！");
    return;
  }
  console.log("store.state.user.loginUser.value", store.state.user.loginUser.value)
  const commentAddReq:CommentAddRequest = {
    content: newComment.value,
    postId: postVO.value?.id,
    status: 0,
    userId: store.state.user.loginUser.id
  }
  const res = await CommentControllerService.addCommentUsingPost(commentAddReq);
  console.log("评论插入Res", res)
  if (res.code == 0) {
    alert("评论成功！");
    newComment.value = "";
    commentList.value.push(res.data)
  }else {
    alert("评论失败", res.message)
  }
}

const formatUtcDateTime = (isoString: UnwrapRef<CommentVO["createTime"]> | undefined): string => {
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

function findParentUserName(parentId: number | undefined, list: CommentVO[]): string | undefined {
  if (!parentId) return undefined;

  for (const comment of list) {
    if (comment.commentId === parentId) {
      return comment.userVO?.userName;
    }
    // 如果有子评论，递归去找
    if (comment.replies && comment.replies.length > 0) {
      const name = findParentUserName(parentId, comment.replies);
      if (name) return name;
    }
  }

  return undefined;
}

const thumbPost = async () => {
  console.log("点赞！");
  let res = await PostThumbControllerService.doThumbUsingPost({
    postId: postVO.value.id
  });
  console.log("res");
  if (res.code === 0) {
    postVO.value.hasThumb = !postVO.value.hasThumb
  }else {
    alert("点赞失败！");
  }
}

const favourPost = async () => {
  console.log("收藏！");
  let res = await PostFavourControllerService.doPostFavourUsingPost({
    postId: postVO.value.id
  });
  console.log("res");
  if (res.code === 0) {
    postVO.value.hasFavour = !postVO.value.hasFavour
  }else {
    alert("收藏失败！");
  }
}

// 切换回复输入框显示/隐藏
const toggleReplyInput = (commentId: number) => {
  replyStates.value = {
    ...replyStates.value,
    [commentId]: !replyStates.value[commentId]
  };

  // 初始化或清空内容
  if (!replyContents.value[commentId]) {
    replyContents.value = {
      ...replyContents.value,
      [commentId]: ''
    };
  } else if (!replyStates.value[commentId]) {
    replyContents.value = {
      ...replyContents.value,
      [commentId]: ''
    };
  }
};

// 提交回复 这是回复一级评论
const submitReply = async (comment: CommentVO) => {
  const content = replyContents.value[comment.commentId];
  if (!content || !content.trim()) return;

  console.log('提交回复:', {
    postId: postVO.value.id,
    content: content,
    parentId: comment.commentId
  });
  const commentAddReq:CommentAddRequest = {
    content: content,
    parentId: comment.commentId,
    rootCommentId: comment.commentId,
    postId: postVO.value.id,
    status: 1,
    userId: store.state.user.loginUser.id
  }
  let res = await CommentControllerService.addCommentUsingPost(commentAddReq);
  if (res.code === 0) {
    alert("评论成功！");
    await loadCommentData();
  }else {
    alert("评论失败！");
  }
  // 清空并关闭输入框
  replyContents.value = {
    ...replyContents.value,
    [comment.commentId]: ''
  };
  replyStates.value = {
    ...replyStates.value,
    [comment.commentId]: false
  };
};

// 提交回复 这是回复二级评论
const submitSubReply = async (rootComment: CommentVO, parentComment: CommentVO) => {
  const content = replyContents.value[parentComment.commentId];
  if (!content || !content.trim()) return;

  console.log('提交回复:', {
    postId: postVO.value?.id,
    content: content,
    parentId: parentComment.commentId,
    rootComment: rootComment.commentId,
  });
  const commentAddReq:CommentAddRequest = {
    content: content,
    parentId: parentComment.commentId,
    postId: postVO.value.id,
    rootCommentId: rootComment.commentId,
    status: 1,
    userId: store.state.user.loginUser.id
  }
  let res = await CommentControllerService.addCommentUsingPost(commentAddReq);
  if (res.code === 0) {
    alert("评论成功！");
    await loadCommentData();
  }else {
    alert("评论失败！");
  }

  // 清空并关闭输入框
  replyContents.value = {
    ...replyContents.value,
    [parentComment.commentId]: ''
  };
  replyStates.value = {
    ...replyStates.value,
    [parentComment.commentId]: false
  };
};

// 处理分页
const handlePageChange = async (page: number) => {
  pageInfo.value.current = page;
  // 这里可以添加获取新页数据的逻辑
  await loadCommentData();
};

// =====> 页面路由 <=====
const toHomePage = () => {
  router.push({
    path: `/`
  });
}

const toDiscussionPage = () => {
  router.push({
    path: `/discussion`
  });
}

</script>

<style scoped>
.author-name {
  font-weight: bold;
  margin-left: 8px;
}
.post-time {
  color: #888;
  margin-left: 8px;
}
.box-class{
  width: 80%
}
@media screen and (max-width: 768px)  {
  .mobile-class {
    width: 100%;
  }
}

.reply-input-container {
  display: flex;
  margin-top: 16px;
  align-items: center;
}

.reply-input {
  flex: 1;
  margin-right: 8px;
}

.send-button {
  white-space: nowrap;
  background-color: #8ebc8e;
  border: 2px solid #2d8a55;
  border-radius: 5%;
  height: 36px;
  color: white;
  width: 6REM;
  font-size: 16px;
}

.action {
  cursor: pointer;
  margin-right: 12px;
}

.pagination-container {
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.pointer-class {
  cursor: pointer;
}
</style>
