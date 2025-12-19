/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { UserVO } from './UserVO';

export type CommentVO = {
    commentId?: number;
    content?: string;
    createTime?: string;
    likeCount?: number;
    parentId?: number;
    postId?: number;
    replies?: Array<CommentVO>;
    userId?: number;
    userVO?: UserVO;
};
