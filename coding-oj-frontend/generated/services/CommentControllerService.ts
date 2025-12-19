/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_boolean_ } from '../models/BaseResponse_boolean_';
import type { BaseResponse_CommentVO_ } from '../models/BaseResponse_CommentVO_';
import type { BaseResponse_Page_CommentVO_ } from '../models/BaseResponse_Page_CommentVO_';
import type { CommentAddRequest } from '../models/CommentAddRequest';
import type { CommentPageQueryRequest } from '../models/CommentPageQueryRequest';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class CommentControllerService {

    /**
     * addComment
     * @param commentAddRequest commentAddRequest
     * @returns BaseResponse_CommentVO_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static addCommentUsingPost(
commentAddRequest: CommentAddRequest,
): CancelablePromise<BaseResponse_CommentVO_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/comment/add',
            body: commentAddRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * delComment
     * @param commentId commentId
     * @returns BaseResponse_boolean_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static delCommentUsingPost(
commentId: number,
): CancelablePromise<BaseResponse_boolean_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/comment/delete',
            body: commentId,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * listCommentVOByPage
     * @param commentPageQueryRequest commentPageQueryRequest
     * @returns BaseResponse_Page_CommentVO_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static listCommentVoByPageUsingPost(
commentPageQueryRequest: CommentPageQueryRequest,
): CancelablePromise<BaseResponse_Page_CommentVO_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/comment/list/page/vo',
            body: commentPageQueryRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}
