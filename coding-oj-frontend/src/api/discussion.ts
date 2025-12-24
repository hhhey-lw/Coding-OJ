import service from './index';

export interface PostAddRequest {
  content?: string;
  tags?: string[];
  title?: string;
}

export interface PostQueryRequest {
  searchKey?: string;
  current?: number;
  pageSize?: number;
  sortField?: string;
  sortOrder?: string;
}

export interface PostUpdateRequest {
  id?: number;
  content?: string;
  tags?: string[];
  title?: string;
}

export interface PageRequest {
  current?: number;
  pageSize?: number;
}

export interface PostVO {
  id?: number;
  title?: string;
  content?: string;
  commentNum?: number;
  viewNum?: number;
  thumbNum?: number;
  favourNum?: number;
  userId?: number;
  createTime?: string;
  updateTime?: string;
  tagList?: string[];
  user?: any; // UserVO
  isThumb?: boolean;
  isFavour?: boolean;
}

export interface CommentAddRequest {
  content?: string;
  postId?: number;
  parentId?: number;
}

export interface CommentVO {
    commentId?: number;
    content?: string;
    postId?: number;
    userId?: number;
    fromUser?: any; // UserVO
    toUser?: any; // UserVO
    replies?: CommentVO[];
    parentId?: number;
    rootCommentId?: number;
    likeCount?: number;
    createTime?: string;
}

export interface CommentQueryRequest {
    current?: number;
    pageSize?: number;
    postId?: number;
    sortField?: string;
    sortOrder?: string;
}

/**
 * 创建帖子
 * @param params
 */
export function addPost(params: PostAddRequest) {
  return service({
    url: '/post/add',
    method: 'post',
    data: params,
  });
}

/**
 * 分页获取帖子 VO 列表（封装类）
 * @param params
 */
export function listPostVOByPage(params: PostQueryRequest) {
  return service({
    url: '/post/page/vo',
    method: 'post',
    data: params,
  });
}

/**
 * 根据 id 获取帖子 VO
 * @param id
 */
export function getPostVOById(id: number) {
  return service({
    url: '/post/get/vo',
    method: 'get',
    params: { id },
  });
}

/**
 * 帖子点赞
 * @param params
 */
export function postThumb(params: { postId: number }) {
  return service({
    url: '/post/thumb/toggle',
    method: 'post',
    data: params,
  });
}

/**
 * 帖子收藏
 * @param params
 */
export function postFavour(params: { postId: number }) {
  return service({
    url: '/post/favour/toggle',
    method: 'post',
    data: params,
  });
}

/**
 * 分页获取我收藏的帖子列表
 * @param params
 */
export function listMyFavourPostByPage(params: PageRequest) {
  return service({
    url: '/post/favour/my/page',
    method: 'post',
    data: params,
  });
}

/**
 * 添加评论
 */
export function addComment(params: CommentAddRequest) {
    return service({
        url: '/post/comment/add',
        method: 'post',
        data: params
    })
}

/**
 * 获取评论列表
 */
export function listCommentVOByPage(params: CommentQueryRequest) {
    return service({
        url: '/post/comment/list/page',
        method: 'post',
        data: params
    })
}

/**
 * 删除评论
 */
export function deleteComment(params: { id: number }) {
    return service({
        url: '/post/comment/delete',
        method: 'post',
        data: params
    })
}

/**
 * 分页获取我的帖子列表
 */
export function listMyPostVOByPage(params: PageRequest) {
    return service({
        url: '/post/my/page/vo',
        method: 'post',
        data: params
    })
}

/**
 * 更新帖子
 */
export function updatePost(params: PostUpdateRequest) {
    return service({
        url: '/post/update',
        method: 'post',
        data: params
    })
}

/**
 * 删除帖子
 */
export function deletePost(params: { id: number }) {
    return service({
        url: '/post/delete',
        method: 'post',
        data: params
    })
}
