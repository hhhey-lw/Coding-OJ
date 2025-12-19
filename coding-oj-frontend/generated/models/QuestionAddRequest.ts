/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */

import type { JudgeCase } from './JudgeCase';
import type { JudgeConfig } from './JudgeConfig';

export type QuestionAddRequest = {
    answer?: string;
    content?: string;
    difficulty?: number;
    judgeCase?: Array<JudgeCase>;
    judgeConfig?: JudgeConfig;
    sourceCode?: string;
    tags?: Array<string>;
    title?: string;
};
