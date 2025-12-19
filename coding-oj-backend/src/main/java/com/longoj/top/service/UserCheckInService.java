package com.longoj.top.service;

import com.longoj.top.model.entity.UserCheckIn;
import com.longoj.top.model.vo.UserCheckInVO;

public interface UserCheckInService {
    // 全部信息
    public UserCheckInVO getUserCheckInByUserIdAndYearMonth(Long userId, String yearMonth);
    // 某月的总签到天数
    public Integer queryUserSignTotalCount(Long userId, String yearMonth);
    // 查询某月某天的签到情况
    public boolean checkUserSignInByOneDay(Long userId, String yearMonth, Integer day);
    // 某天签到
    public boolean updateUserCheckInByOneDay(Long userId, String yearMonth, Integer day);

    void autoUserSignInOneDaySummary(Long userId);
}
