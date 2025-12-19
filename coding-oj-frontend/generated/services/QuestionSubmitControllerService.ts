/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_List_UserSubmitInfoVO_ } from '../models/BaseResponse_List_UserSubmitInfoVO_';
import type { BaseResponse_long_ } from '../models/BaseResponse_long_';
import type { BaseResponse_Page_QuestionSubmitVO_ } from '../models/BaseResponse_Page_QuestionSubmitVO_';
import type { QuestionSubmitAddRequest } from '../models/QuestionSubmitAddRequest';
import type { QuestionSubmitQueryRequest } from '../models/QuestionSubmitQueryRequest';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class QuestionSubmitControllerService {

    /**
     * 提交代码
     * @param questionSubmitAddRequest questionSubmitAddRequest
     * @returns BaseResponse_long_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static doSubmitUsingPost(
questionSubmitAddRequest: QuestionSubmitAddRequest,
): CancelablePromise<BaseResponse_long_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/question_submit/do',
            body: questionSubmitAddRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * 分页查询提交问题
     * @param questionSubmitQueryRequest questionSubmitQueryRequest
     * @returns BaseResponse_Page_QuestionSubmitVO_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static listQuestionSubmitByPageUsingPost(
questionSubmitQueryRequest: QuestionSubmitQueryRequest,
): CancelablePromise<BaseResponse_Page_QuestionSubmitVO_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/question_submit/list/page',
            body: questionSubmitQueryRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * 分页查询用户自己提交问题
     * @param questionSubmitQueryRequest questionSubmitQueryRequest
     * @returns BaseResponse_Page_QuestionSubmitVO_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static listUserQuestionSubmitByPageUsingPost(
questionSubmitQueryRequest: QuestionSubmitQueryRequest,
): CancelablePromise<BaseResponse_Page_QuestionSubmitVO_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/question_submit/list/page/user',
            body: questionSubmitQueryRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getTopPassedQuestionUserList
     * @param topNumber topNumber
     * @returns BaseResponse_List_UserSubmitInfoVO_ OK
     * @throws ApiError
     */
    public static getTopPassedQuestionUserListUsingGet(
topNumber: number,
): CancelablePromise<BaseResponse_List_UserSubmitInfoVO_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/question_submit/topPassed/{topNumber}',
            path: {
                'topNumber': topNumber,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}
