import service from './index';

export interface TagVO {
  id?: string;
  tagName?: string;
}

/**
 * 分页获取标签
 * @param current
 * @param pageSize
 */
export function getTagByPage(current: number, pageSize: number) {
  return service({
    url: `/question/tag/queryTag/${current}/${pageSize}`,
    method: 'get',
  });
}

/**
 * 根据标签 ID 获取题目列表
 * @param tagId
 * @param current
 * @param pageSize
 */
export function getQuestionByTagId(tagId: string, current: number, pageSize: number) {
    return service({
        url: `/question/tag/id/${tagId}/${current}/${pageSize}`,
        method: 'get',
    })
}
