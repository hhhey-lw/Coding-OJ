package com.longoj.top.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.longoj.top.common.ErrorCode;
import com.longoj.top.exception.BusinessException;
import com.longoj.top.mapper.UserCheckInMapper;
import com.longoj.top.mapper.UserSubmitSummaryMapper;
import com.longoj.top.model.entity.UserCheckIn;
import com.longoj.top.model.entity.UserSubmitSummary;
import com.longoj.top.model.vo.UserCheckInVO;
import com.longoj.top.model.vo.UserSubmitSummaryVO;
import com.longoj.top.service.UserCheckInService;
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

    // @Resource
    // private UserCheckInService selfProxy;

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
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未找到签到记录");
        }

        // 3. 转换为VO对象
        UserCheckInVO userCheckInVO = BeanUtil.copyProperties(userCheckIn, UserCheckInVO.class);
        userCheckInVO.setUserSubmitSummaryVO(new ArrayList<>());

        // 4. 解析bitmap，获取所有已签到的天数
        Integer bitmap = userCheckInVO.getBitmap();
        if (bitmap != null) {
            // 遍历31天（假设一个月最多31天）  范围是0-30
            for (int day = 0; day < 31; day++) {
                // 检查当前位是否为1（表示已签到）
                if ((bitmap & (1 << day)) != 0) {
                    // 构造日期字符串（补零，如01, 02,...）
                    String dayStr = String.format("%02d", day + 1);
                    String fullDate = yearMonth + "-" + dayStr;

                    // 查询当天的提交摘要
                    UserSubmitSummary userSubmitSummary = userSubmitSummaryMapper.querySubmitSummary(userId, fullDate);
                    if (userSubmitSummary != null) {
                        userCheckInVO.getUserSubmitSummaryVO().add(BeanUtil.copyProperties(userSubmitSummary, UserSubmitSummaryVO.class));
                    }
                }
            }
        }

        return userCheckInVO;
    }

    @Override
    public Integer queryUserSignTotalCount(Long userId, String yearMonth) {
        if (!checkYearMonthFormat(yearMonth)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "日期不合法");
        }
        return userCheckInMapper.queryUserSignTotalCount(userId, yearMonth);
    }

    @Override
    public boolean checkUserSignInByOneDay(Long userId, String yearMonth, Integer day) {
        if (!checkYearMonthFormat(yearMonth)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "日期不合法");
        }
        return userCheckInMapper.checkUserSignInByOneDay(userId, yearMonth, day);
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
        String fullDate = yearMonth + "-" + dayStr;
        UserSubmitSummary userSubmitSummary = userSubmitSummaryMapper.querySubmitSummary(userId, fullDate);
        if (userSubmitSummary == null) {
            userSubmitSummaryMapper.addSubmitSummary(userId, fullDate);
            userSubmitSummaryMapper.updateSubmitSummary(userId, fullDate, 1, 0);
        } else {
            userSubmitSummaryMapper.updateSubmitSummary(userId, fullDate, 1, 0);
        }
        return true;
    }

    @Override
    public void autoUserSignInOneDaySummary(Long userId) {
        LocalDate now = LocalDate.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        int day = now.getDayOfMonth();
        UserCheckInService proxy = (UserCheckInService) AopContext.currentProxy();
        proxy.updateUserCheckInByOneDay(userId, yearMonth, day);
    }
}
