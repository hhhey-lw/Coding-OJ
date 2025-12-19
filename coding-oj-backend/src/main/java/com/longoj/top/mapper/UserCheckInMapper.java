package com.longoj.top.mapper;

import com.longoj.top.model.entity.UserCheckIn;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserCheckInMapper {
    // 查询是否存在
    boolean isExist(Long userId, String yearMonth);
    boolean save(Long userId, String yearMonth);
    // 全部信息
    UserCheckIn getUserCheckInByUserIdAndYearMonth(Long userId, String yearMonth);
    // 某月的总签到天数
    Integer queryUserSignTotalCount(Long userId, String yearMonth);
    // 查询某月某天的签到情况
    boolean checkUserSignInByOneDay(Long userId, String yearMonth, Integer day);
    // 某天签到
    boolean updateUserCheckInByOneDay(Long userId, String yearMonth, Integer day);
 }
