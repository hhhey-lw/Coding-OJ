package com.longoj.top.domain.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.infrastructure.mapper.UserCheckInMapper;
import com.longoj.top.infrastructure.mapper.UserSubmitSummaryMapper;
import com.longoj.top.domain.entity.UserCheckIn;
import com.longoj.top.domain.entity.UserSubmitSummary;
import com.longoj.top.controller.dto.user.UserCheckInVO;
import com.longoj.top.controller.dto.user.UserSubmitSummaryVO;
import com.longoj.top.domain.service.UserCheckInService;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.regex.Pattern;

@Service
public class UserCheckInServiceImpl implements UserCheckInService {

    private static final Pattern YYYY_MM_PATTERN = Pattern.compile("^(19|20)\\d{2}-(0[1-9]|1[0-2])$");

    @Resource
    private UserCheckInMapper userCheckInMapper;

    @Resource
    private UserSubmitSummaryMapper userSubmitSummaryMapper;

    private boolean checkYearMonthFormat(String yearMonth) {
        if (StrUtil.isBlank(yearMonth)) {
            return false;
        }
        if (yearMonth.length() != 7){
            return false;
        }
        return YYYY_MM_PATTERN.matcher(yearMonth).matches();
    }

    @Override
    public UserCheckInVO getUserCheckInByUserIdAndYearMonth(Long userId, String yearMonth) {
        // 1. 验证日期格式
        if (!checkYearMonthFormat(yearMonth)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "日期不合法");
        }

        // 2. 获取用户签到记录
        UserCheckIn userCheckIn = userCheckInMapper.getUserCheckInByUserIdAndYearMonth(userId, yearMonth);
        if (userCheckIn == null) {
            userCheckIn = UserCheckIn.createEmpty(userId, yearMonth);
        }

        // 3. 转换为VO对象
        UserCheckInVO userCheckInVO = BeanUtil.copyProperties(userCheckIn, UserCheckInVO.class);
        userCheckInVO.setUserSubmitSummaryVO(new ArrayList<>());

        // 4.补充每日提交信息
        fillDaySubmitInfo(userId, yearMonth, userCheckInVO);

        return userCheckInVO;
    }

    @Override
    @Transactional
    public boolean updateUserCheckInByOneDay(Long userId, String yearMonth, Integer day) {
        // 不存在则新建
        boolean exist = userCheckInMapper.isExist(userId, yearMonth);
        if (!exist) {
            userCheckInMapper.save(userId, yearMonth);
        }
        // 签到
        userCheckInMapper.updateUserCheckInByOneDay(userId, yearMonth, day);

        // 统计对应的天数的提交信息
        String dayStr = String.format("%02d", day);
        String dateStr = yearMonth + "-" + dayStr;
        UserSubmitSummary userSubmitSummary = userSubmitSummaryMapper.querySubmitSummary(userId, dateStr);
        if (userSubmitSummary == null) {
            userSubmitSummaryMapper.addSubmitSummary(userId, dateStr);
            userSubmitSummaryMapper.updateSubmitSummary(userId, dateStr, 1, 0);
        } else {
            userSubmitSummaryMapper.updateSubmitSummary(userId, dateStr, 1, 0);
        }
        return true;
    }

    @Override
    public void autoUserSignInOneDaySummary(Long userId) {
        LocalDate now = LocalDate.now();
        UserCheckInService proxy = (UserCheckInService) AopContext.currentProxy();
        proxy.updateUserCheckInByOneDay(userId,
                now.format(DateTimeFormatter.ofPattern("yyyy-MM")), now.getDayOfMonth());
    }

    /**
     * 填充每天的提交信息
     */
    private void fillDaySubmitInfo(Long userId, String yearMonth, UserCheckInVO userCheckInVO) {
        Integer bitmap = userCheckInVO.getBitmap();
        if (bitmap != null) {
            // 遍历每个月的每天
            for (int day = 0; day < 31; day++) {
                // 当日是否有提交
                if ((bitmap & (1 << day)) != 0) {
                    // 构造日期字符串
                    String dayStr = String.format("%02d", day + 1);
                    String dateStr = yearMonth + "-" + dayStr;

                    // 查询当天的提交摘要
                    UserSubmitSummary userSubmitSummary = userSubmitSummaryMapper.querySubmitSummary(userId, dateStr);
                    if (userSubmitSummary != null) {
                        userCheckInVO.getUserSubmitSummaryVO().add(BeanUtil.copyProperties(userSubmitSummary, UserSubmitSummaryVO.class));
                    }
                }
            }
        }
    }

}
