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
          <IconThumbUpFill style="font-size: large; color: #165dff; cursor: pointer;" v-if="postVO?.isThumb" @click="thumbPost"/>
          <IconThumbUp style="font-size: large; cursor: pointer;" v-else @click="thumbPost"/>
          <IconStarFill style="font-size: large; color: #ffb400; cursor: pointer;" v-if="postVO?.isFavour" @click="favourPost"/>
          <IconStar style="font-size: large; cursor: pointer;" v-else @click="favourPost"/>
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
                   :author="item.fromUser?.userName"
                   :avatar="item.fromUser?.userAvatar"
                   :content="item.content"
                   :datetime="formatUtcDateTime(item.createTime)"
        >
          <a-divider/>
          <template #actions>
            <span class="action" style="cursor: pointer" key="reply" @click="toggleReplyInput(item.commentId)">
          <IconMessage /> {{activeReplyId === item.commentId ? '取消回复' :'回复'}}
        </span>
          </template>

          <!-- 主评论的回复输入框 -->
          <div v-if="activeReplyId === item.commentId" class="reply-input-container">
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
          <template v-if="item.replies && item.replies.length > 0">
            <a-comment align="right"
                       v-for="subitem in item.replies" :key="subitem.commentId"
                       :author="subitem.fromUser?.userName"
                       :avatar="subitem.fromUser?.userAvatar"
                       :content="getCommentContent(subitem)"
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
            <IconMessage /> {{activeReplyId === subitem.commentId ? '取消回复' : '回复'}}
          </span>
            </template>

            <!-- 子评论的回复输入框 -->
            <div v-if="activeReplyId === subitem.commentId" class="reply-input-container">
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
          </template>
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
  listCommentVOByPage,
  getPostVOById,
  postThumb,
  postFavour,
  addComment,
  PostVO,
  CommentVO, CommentAddRequest, CommentQueryRequest
} from "@/api/discussion";
import { Message } from "@arco-design/web-vue";
import MdViewer from "@/components/MdViewer.vue";
import { h, render } from 'vue';
import {
  IconMessage,
  IconThumbUp,
  IconThumbUpFill,
  IconStar,
  IconStarFill,
  IconLoading
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
const activeReplyId = ref<number | string | null>(null); // 当前激活的回复框 ID
const replyContents = ref<Record<number | string, string>>({});

// 直接回复Post的评论内容
const newComment = ref('');

// 加载状态
const isSubmitting = ref(false);

// =====> 生命周期 <=====
onMounted(() => {
  console.log('详情页加载完成');
  loadPostData();
  loadCommentData();
});

// =====> 函数定义 <=====
const loadPostData = async () => {
  const res:any = await getPostVOById(
      props.id
  );
  if (res) {
    console.log('res.data',res);
    postVO.value = res;
  } else {
    Message.error("加载失败");
  }
};

const loadCommentData = async () => {
  const req:CommentQueryRequest = {
    postId: props.id,
    pageSize: pageInfo.value.pageSize,
    current: pageInfo.value.current
  }
  const res:any = await listCommentVOByPage(
      req
  );
  if (res) {
    console.log('res.data',res);
    pageInfo.value.totalComments = res.total
    commentList.value = res.records;
  } else {
    Message.error("加载失败");
  }
};

// 直接回复Post的评论
const submitComment = async () => {
  if (newComment.value == '') {
    Message.warning("评论内容不能为空！");
    return;
  }
  
  if (isSubmitting.value) {
    return; // 防止重复提交
  }
  
  isSubmitting.value = true;
  
  // 创建遮罩层
  const maskDiv = document.createElement('div');
  maskDiv.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
  `;
  
  // 使用 Vue 的 h 和 render 渲染 IconLoading
  const vnode = h(IconLoading, { 
    style: { fontSize: '48px', color: '#fff' },
    spin: true 
  });
  render(vnode, maskDiv);
  document.body.appendChild(maskDiv);
  
  try {
    const commentAddReq:CommentAddRequest = {
      content: newComment.value,
      postId: postVO.value?.id,
    }
    const res:any = await addComment(commentAddReq);
    
    if (res) {
      Message.success("评论成功！");
      newComment.value = "";
      await loadCommentData(); // 重新加载评论列表
    } else {
      Message.error("评论失败");
    }
  } catch (error) {
    Message.error("评论失败");
  } finally {
    document.body.removeChild(maskDiv);
    isSubmitting.value = false;
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

// 获取评论内容，根据 parentId 和 rootCommentId 判断是否显示“回复 @username”
const getCommentContent = (comment: CommentVO): string => {
  // 如果 parentId 等于 rootCommentId，说明是直接回复一级评论（第二层级），不显示“回复 @username”
  if (comment.parentId && comment.rootCommentId && comment.parentId === comment.rootCommentId) {
    return comment.content || '';
  }
  // 否则是三级及以后的回复，显示“回复 @username”
  if (comment.toUser && comment.toUser.userName) {
    return `回复 @${comment.toUser.userName}：${comment.content}`;
  }
  return comment.content || '';
};

function findParentUserName(parentId: number | undefined, list: CommentVO[]): string | undefined {
  if (!parentId) return undefined;

  for (const comment of list) {
    if (comment.commentId === parentId) {
      return comment.fromUser?.userName;
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
  let res:any = await postThumb({
    postId: postVO.value.id
  });
  console.log("res");
  if (res) {
    postVO.value.isThumb = !postVO.value.isThumb
  }else {
    alert("点赞失败！");
  }
}

const favourPost = async () => {
  console.log("收藏！");
  let res:any = await postFavour({
    postId: postVO.value.id
  });
  console.log("res");
  if (res) {
    postVO.value.isFavour = !postVO.value.isFavour
  }else {
    alert("收藏失败！");
  }
}

// 切换回复输入框显示/隐藏（互斥逻辑）
const toggleReplyInput = (commentId: number | string) => {
  if (activeReplyId.value === commentId) {
    // 如果点击的是当前已打开的，则关闭
    activeReplyId.value = null;
  } else {
    // 否则打开新的
    activeReplyId.value = commentId;
    // 初始化内容
    if (!replyContents.value[commentId]) {
      replyContents.value = {
        ...replyContents.value,
        [commentId]: ''
      };
    }
  }
};

// 提交回复 这是回复一级评论
const submitReply = async (comment: CommentVO) => {
  const content = replyContents.value[comment.commentId!];
  if (!content || !content.trim()) {
    Message.warning("回复内容不能为空！");
    return;
  }
  
  if (isSubmitting.value) {
    return; // 防止重复提交
  }
  
  isSubmitting.value = true;
  
  // 创建遮罩层
  const maskDiv = document.createElement('div');
  maskDiv.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
  `;
  
  // 使用 Vue 的 h 和 render 渲染 IconLoading
  const vnode = h(IconLoading, { 
    style: { fontSize: '48px', color: '#fff' },
    spin: true 
  });
  render(vnode, maskDiv);
  document.body.appendChild(maskDiv);
  
  try {
    const commentAddReq:CommentAddRequest = {
      content: content,
      parentId: comment.commentId as number,
      postId: postVO.value.id,
    }
    let res:any = await addComment(commentAddReq);
    
    if (res) {
      Message.success("回复成功！");
      await loadCommentData();
      // 清空并关闭输入框
      replyContents.value = {
        ...replyContents.value,
        [comment.commentId!]: ''
      };
      activeReplyId.value = null;
    } else {
      Message.error("回复失败！");
    }
  } catch (error) {
    Message.error("回复失败！");
  } finally {
    document.body.removeChild(maskDiv);
    isSubmitting.value = false;
  }
};

// 提交回复 这是回复二级评论
const submitSubReply = async (rootComment: CommentVO, parentComment: CommentVO) => {
  const content = replyContents.value[parentComment.commentId!];
  if (!content || !content.trim()) {
    Message.warning("回复内容不能为空！");
    return;
  }
  
  if (isSubmitting.value) {
    return; // 防止重复提交
  }
  
  isSubmitting.value = true;
  
  // 创建遮罩层
  const maskDiv = document.createElement('div');
  maskDiv.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 9999;
  `;
  
  // 使用 Vue 的 h 和 render 渲染 IconLoading
  const vnode = h(IconLoading, { 
    style: { fontSize: '48px', color: '#fff' },
    spin: true 
  });
  render(vnode, maskDiv);
  document.body.appendChild(maskDiv);
  
  try {
    const commentAddReq:CommentAddRequest = {
      content: content,
      parentId: parentComment.commentId as number,
      postId: postVO.value.id,
    }
    let res:any = await addComment(commentAddReq);
    
    if (res) {
      Message.success("回复成功！");
      await loadCommentData();
      // 清空并关闭输入框
      replyContents.value = {
        ...replyContents.value,
        [parentComment.commentId!]: ''
      };
      activeReplyId.value = null;
    } else {
      Message.error("回复失败！");
    }
  } catch (error) {
    Message.error("回复失败！");
  } finally {
    document.body.removeChild(maskDiv);
    isSubmitting.value = false;
  }

  // 清空并关闭输入框
  replyContents.value = {
    ...replyContents.value,
    [parentComment.commentId!]: ''
  };
  activeReplyId.value = null;
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

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.pointer-class {
  cursor: pointer;
}
</style>
