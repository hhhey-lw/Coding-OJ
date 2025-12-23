package com.longoj.top.domain.service;

import com.longoj.top.controller.dto.user.UserCheckInVO;

public interface UserCheckInService {
    /**
     * 根据用户ID和年月查询用户签到信息
     */
    UserCheckInVO getUserCheckInByUserIdAndYearMonth(Long userId, String yearMonth);

    /**
     * 更新用户某天的签到情况为已签到
     */
    boolean updateUserCheckInByOneDay();

}
