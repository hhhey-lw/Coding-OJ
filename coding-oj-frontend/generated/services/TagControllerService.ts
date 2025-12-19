/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_Page_QuestionVO_ } from '../models/BaseResponse_Page_QuestionVO_';
import type { BaseResponse_Page_TagVO_ } from '../models/BaseResponse_Page_TagVO_';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class TagControllerService {

    /**
     * getQuestionByTagId
     * @param current current
     * @param pageSize pageSize
     * @param tagId tagId
     * @returns BaseResponse_Page_QuestionVO_ OK
     * @throws ApiError
     */
    public static getQuestionByTagIdUsingGet(
current: number,
pageSize: number,
tagId: number,
): CancelablePromise<BaseResponse_Page_QuestionVO_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/tag/id/{tagId}/{current}/{pageSize}',
            path: {
                'current': current,
                'pageSize': pageSize,
                'tagId': tagId,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getQuestionByTagName
     * @param current current
     * @param pageSize pageSize
     * @param tagName tagName
     * @returns BaseResponse_Page_QuestionVO_ OK
     * @throws ApiError
     */
    public static getQuestionByTagNameUsingGet(
current: number,
pageSize: number,
tagName: string,
): CancelablePromise<BaseResponse_Page_QuestionVO_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/tag/name/{tagName}/{current}/{pageSize}',
            path: {
                'current': current,
                'pageSize': pageSize,
                'tagName': tagName,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * getTagByPage
     * @param current current
     * @param pageSize pageSize
     * @returns BaseResponse_Page_TagVO_ OK
     * @throws ApiError
     */
    public static getTagByPageUsingGet(
current: number,
pageSize: number,
): CancelablePromise<BaseResponse_Page_TagVO_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/tag/queryTag/{current}/{pageSize}',
            path: {
                'current': current,
                'pageSize': pageSize,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}
