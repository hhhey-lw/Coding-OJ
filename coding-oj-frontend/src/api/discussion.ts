import service from './index';

export interface PostAddRequest {
  content?: string;
  tags?: string[];
  title?: string;
}

export interface PostQueryRequest {
  content?: string;
  current?: number;
  favourUserId?: number;
  id?: number;
  notId?: number;
  pageSize?: number;
  searchText?: string;
  sortField?: string;
  sortOrder?: string;
  tags?: string[];
  title?: string;
  userId?: number;
}

export interface PostVO {
  content?: string;
  createTime?: string;
  favourNum?: number;
  hasFavour?: boolean;
  hasThumb?: boolean;
  id?: number;
  tagList?: string[];
  thumbNum?: number;
  title?: string;
  updateTime?: string;
  user?: any; // UserVO
  userId?: number;
}

export interface CommentAddRequest {
  content?: string;
  postId?: number;
  parentId?: number;
}

export interface CommentVO {
    commentId?: number | string;
    content?: string;
    postId?: number;
    userId?: number;
    parentId?: number;
    createTime?: string;
    userVO?: any;
    replies?: CommentVO[];
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
    url: '/post/list/page/vo',
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
    url: '/post_thumb/',
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
    url: '/post_favour/',
    method: 'post',
    data: params,
  });
}

/**
 * 分页获取我收藏的帖子列表
 * @param params
 */
export function listMyFavourPostByPage(params: PostQueryRequest) {
  return service({
    url: '/post_favour/my/list/page',
    method: 'post',
    data: params,
  });
}

/**
 * 添加评论
 */
export function addComment(params: CommentAddRequest) {
    return service({
        url: '/comment/add',
        method: 'post',
        data: params
    })
}

/**
 * 获取评论列表
 */
export function listCommentVOByPage(params: CommentQueryRequest) {
    return service({
        url: '/comment/list/page/vo',
        method: 'post',
        data: params
    })
}
