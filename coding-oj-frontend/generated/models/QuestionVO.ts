/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { JudgeConfig } from './JudgeConfig';
import type { UserVO } from './UserVO';

export type QuestionVO = {
    acceptedNum?: number;
    answer?: string;
    content?: string;
    createTime?: string;
    difficulty?: number;
    favourNum?: number;
    id?: number;
    judgeConfig?: JudgeConfig;
    passed?: boolean;
    sourceCode?: string;
    submitNum?: number;
    tags?: Array<string>;
    thumbNum?: number;
    title?: string;
    updateTime?: string;
    userVO?: UserVO;
};
