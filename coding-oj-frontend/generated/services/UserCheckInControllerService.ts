/* generated using openapi-typescript-codegen -- do no edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_boolean_ } from '../models/BaseResponse_boolean_';
import type { BaseResponse_UserCheckInVO_ } from '../models/BaseResponse_UserCheckInVO_';
import type { UserCheckInUpdateRequest } from '../models/UserCheckInUpdateRequest';

import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';

export class UserCheckInControllerService {

    /**
     * getUserCheckInByUserIdAndYearMonth
     * @param userId userId
     * @param yearMonth yearMonth
     * @returns BaseResponse_UserCheckInVO_ OK
     * @throws ApiError
     */
    public static getUserCheckInByUserIdAndYearMonthUsingGet(
userId?: number,
yearMonth?: string,
): CancelablePromise<BaseResponse_UserCheckInVO_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/user-check-in/info',
            query: {
                'userId': userId,
                'yearMonth': yearMonth,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

    /**
     * updateUserCheckInByOneDay
     * @param userCheckInUpdateRequest userCheckInUpdateRequest
     * @returns BaseResponse_boolean_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static updateUserCheckInByOneDayUsingPost(
userCheckInUpdateRequest: UserCheckInUpdateRequest,
): CancelablePromise<BaseResponse_boolean_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/user-check-in/update',
            body: userCheckInUpdateRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }

}
