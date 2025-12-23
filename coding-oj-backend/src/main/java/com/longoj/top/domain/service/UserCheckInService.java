package com.longoj.top.domain.service;

import com.longoj.top.controller.dto.user.UserCheckInVO;

public interface UserCheckInService {
    /**
     * 根据用户ID和年月查询用户签到信息
     *
     * @param userId    用户ID
     * @param yearMonth 年月，格式为yyyyMM
     * @return 用户签到信息
     */
    UserCheckInVO getUserCheckInByUserIdAndYearMonth(Long userId, String yearMonth);

    /**
     * 更新用户某天的签到情况为已签到
     *
     * @param userId    用户ID
     * @param yearMonth 年月，格式为yyyyMM
     * @param day       天，格式为dd
     * @return 是否更新成功
     */
    boolean updateUserCheckInByOneDay(Long userId, String yearMonth, Integer day);

    /**
     * 为用户签到某一天
     */
    void checkInOfLoginUser();
}
