import service from './index';

export interface QuestionAddRequest {
  answer?: string;
  content?: string;
  judgeCase?: JudgeCase[];
  judgeConfig?: JudgeConfig;
  tags?: string[];
  title?: string;
}

export interface QuestionEditRequest {
  answer?: string;
  content?: string;
  id?: number;
  judgeCase?: JudgeCase[];
  judgeConfig?: JudgeConfig;
  tags?: string[];
  title?: string;
}

export interface QuestionQueryRequest {
  answer?: string;
  content?: string;
  current?: number;
  id?: number;
  pageSize?: number;
  sortField?: string;
  sortOrder?: string;
  tags?: string[];
  title?: string;
  userId?: number;
}

export interface QuestionUpdateRequest {
  answer?: string;
  content?: string;
  id?: number;
  judgeCase?: JudgeCase[];
  judgeConfig?: JudgeConfig;
  tags?: string[];
  title?: string;
}

export interface JudgeCase {
  input?: string;
  output?: string;
}

export interface JudgeConfig {
  memoryLimit?: number;
  stackLimit?: number;
  timeLimit?: number;
}

export interface QuestionVO {
  content?: string;
  createTime?: string;
  favourNum?: number;
  id?: number;
  judgeConfig?: JudgeConfig;
  submitNum?: number;
  tags?: string[];
  thumbNum?: number;
  title?: string;
  userId?: number;
  userVO?: any;
}

/**
 * 创建题目
 * @param params
 */
export function addQuestion(params: QuestionAddRequest) {
  return service({
    url: '/question/add',
    method: 'post',
    data: params,
  });
}

/**
 * 删除题目
 * @param params
 */
export function deleteQuestion(params: { id: number }) {
  return service({
    url: '/question/delete',
    method: 'post',
    data: params,
  });
}

/**
 * 更新题目
 * @param params
 */
export function updateQuestion(params: QuestionUpdateRequest) {
  return service({
    url: '/question/update',
    method: 'post',
    data: params,
  });
}

/**
 * 编辑题目
 * @param params
 */
export function editQuestion(params: QuestionEditRequest) {
    return service({
        url: '/question/edit',
        method: 'post',
        data: params,
    });
}

/**
 * 根据 id 获取题目（管理员）
 * @param id
 */
export function getQuestionById(id: number) {
  return service({
    url: '/question/get',
    method: 'get',
    params: { id },
  });
}

/**
 * 根据 id 获取题目 VO（用户）
 * @param id
 */
export function getQuestionVOById(id: number) {
  return service({
    url: '/question/get/vo',
    method: 'get',
    params: { id },
  });
}

/**
 * 分页获取用户提交题目列表（包含用户提交信息）
 * @param params
 */
export function listUserQuestionSubmitByPage(params: QuestionSubmitQueryRequest) {
    return service({
        url: '/question_submit/list/page/user',
        method: 'post',
        data: params
    })
}

/**
 * 分页获取题目列表（管理员）
 * @param params
 */
export function listQuestionByPage(params: QuestionQueryRequest) {
  return service({
    url: '/question/list/page',
    method: 'post',
    data: params,
  });
}

/**
 * 分页获取题目 VO 列表（用户）
 * @param params
 */
export function listQuestionVOByPage(params: QuestionQueryRequest) {
  return service({
    url: '/question/list/page/vo',
    method: 'post',
    data: params,
  });
}

/**
 * 题目提交
 */
export interface QuestionSubmitAddRequest {
  code?: string;
  language?: string;
  questionId?: number;
}

export interface QuestionSubmitQueryRequest {
  current?: number;
  language?: string;
  pageSize?: number;
  questionId?: number;
  sortField?: string;
  sortOrder?: string;
  status?: number;
  userId?: number;
}

/**
 * 提交题目
 * @param params
 */
export function doQuestionSubmit(params: QuestionSubmitAddRequest) {
  return service({
    url: '/question_submit/',
    method: 'post',
    data: params,
  });
}

/**
 * 分页获取题目提交列表
 * @param params
 */
export function listQuestionSubmitByPage(params: QuestionSubmitQueryRequest) {
  return service({
    url: '/question_submit/list/page',
    method: 'post',
    data: params,
  });
}

/**
 * 分页获取题目提交列表
 * @param params
 */
export interface UserSubmitInfoVO {
    id?: number;
    userName?: string;
    userAvatar?: string;
    passedQuestionNumber?: number;
    totalSubmitNumber?: number;
}

/**
 * 获取题目提交排名
 * @param limit
 */
export function getTopPassedQuestionUserList(limit: number) {
    return service({
        url: `/question_submit/topPassed/${limit}`,
        method: 'get',
    })
}
