package com.longoj.top.domain.service.impl;

import cn.hutool.core.util.StrUtil;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.infrastructure.mapper.QuestionSubmitMapper;
import com.longoj.top.controller.dto.user.UserCheckInVO;
import com.longoj.top.controller.dto.user.UserSubmitSummaryVO;
import com.longoj.top.domain.service.UserCheckInService;
import com.longoj.top.infrastructure.utils.RedisKeyUtil;
import com.longoj.top.infrastructure.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@Service
public class UserCheckInServiceImpl implements UserCheckInService {

    /**
     * yyyy-MM 格式校验正则表达式
     */
    private static final Pattern YYYY_MM_PATTERN = Pattern.compile("^(19|20)\\d{2}-(0[1-9]|1[0-2])$");

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    @Override
    public UserCheckInVO getUserCheckInByUserIdAndYearMonth(Long userId, String yearMonth) {
        // 1. 验证日期格式
        if (!checkYearMonthFormat(yearMonth)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "日期不合法");
        }

        // 2. 尝试从 Redis 读取
        try {
            String checkInKey = RedisKeyUtil.getUserCheckInKey(userId, yearMonth);

            // 先判断 Redis key 是否存在
            Boolean hasKey = stringRedisTemplate.hasKey(checkInKey);
            if (Boolean.TRUE.equals(hasKey)) {
                // Redis 有缓存，直接返回（即使 bitmap=0 也是有效缓存）
                return getUserCheckInFromRedis(userId, yearMonth);
            }
        } catch (Exception e) {
            // Redis 连接失败，降级到 DB
            log.warn("Redis 读取失败，降级到数据库，用户: {}, 年月: {}, 原因: {}",
                userId, yearMonth, e.getMessage());
        }

        // 3. 从 DB 读取最新数据
        UserCheckInVO voFromDB = getUserCheckInFromDB(userId, yearMonth);

        // 4. 重建 Redis 缓存
        rebuildRedisCache(userId, yearMonth, voFromDB);

        return voFromDB;
    }

    @Override
    public boolean updateUserCheckInByOneDay() {
        Long userId = UserContext.getUser().getId();
        LocalDate now = LocalDate.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        // 删除缓存
        deleteRedisCache(userId, yearMonth);

        return true;
    }

    /**
     * 删除 Redis 缓存
     */
    private void deleteRedisCache(Long userId, String yearMonth) {
        try {
            String checkInKey = RedisKeyUtil.getUserCheckInKey(userId, yearMonth);
            String submitKey = RedisKeyUtil.getUserSubmitDailyKey(userId, yearMonth);

            stringRedisTemplate.delete(checkInKey);
            stringRedisTemplate.delete(submitKey);

            log.debug("已删除用户 {} 在 {} 的 Redis 缓存", userId, yearMonth);
        } catch (Exception e) {
            log.warn("删除 Redis 缓存失败（不影响业务）: {}", e.getMessage());
        }
    }

    /**
     * 【重建缓存】从 DB 数据重建 Redis 缓存
     */
    private void rebuildRedisCache(Long userId, String yearMonth, UserCheckInVO voFromDB) {
        try {
            // 1. 重建签到 bitmap
            Integer bitmap = voFromDB.getBitmap();
            if (bitmap != null) {
                String checkInKey = RedisKeyUtil.getUserCheckInKey(userId, yearMonth);

                stringRedisTemplate.opsForValue().set(checkInKey, String.valueOf(bitmap));

                // 设置过期时间
                YearMonth ym = YearMonth.parse(yearMonth);
                LocalDate expireDate = ym.plusMonths(2).atDay(1).minusDays(1);
                long daysToExpire = DAYS.between(LocalDate.now(), expireDate);
                if (daysToExpire > 0) {
                    stringRedisTemplate.expire(checkInKey, daysToExpire, TimeUnit.DAYS);
                }
            }

            // 2. 重建每日提交统计（使用 Pipeline 批量操作）
            List<UserSubmitSummaryVO> summaryList = voFromDB.getUserSubmitSummaryVO();
            if (summaryList != null && !summaryList.isEmpty()) {
                String submitKey = RedisKeyUtil.getUserSubmitDailyKey(userId, yearMonth);

                // 使用 Pipeline 批量写入 Hash
                stringRedisTemplate.executePipelined((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
                    for (UserSubmitSummaryVO summary : summaryList) {
                        String yearMonthDay = summary.getYearMonthDay();
                        if (yearMonthDay != null && yearMonthDay.length() == 10) {
                            int day = Integer.parseInt(yearMonthDay.substring(8, 10));

                            if (summary.getSubmitCount() != null && summary.getSubmitCount() > 0) {
                                connection.hSet(submitKey.getBytes(),
                                        (day + ":submit").getBytes(),
                                        String.valueOf(summary.getSubmitCount()).getBytes());
                            }
                            if (summary.getAcceptCount() != null && summary.getAcceptCount() > 0) {
                                connection.hSet(submitKey.getBytes(),
                                        (day + ":accept").getBytes(),
                                        String.valueOf(summary.getAcceptCount()).getBytes());
                            }
                        }
                    }
                    return null;
                });

                // 设置过期时间：1 天
                stringRedisTemplate.expire(submitKey, 1, TimeUnit.DAYS);
            }

            log.info("已重建用户 {} 在 {} 的 Redis 缓存", userId, yearMonth);
        } catch (Exception e) {
            log.warn("重建 Redis 缓存失败（不影响业务）: {}", e.getMessage());
        }
    }

    /**
     * 从 Redis 获取签到和提交信息
     * 优化：使用 GET 命令直接获取 bitmap 的字节数据，避免循环读取
     */
    private UserCheckInVO getUserCheckInFromRedis(Long userId, String yearMonth) {
        String checkInKey = RedisKeyUtil.getUserCheckInKey(userId, yearMonth);
        Integer bitmap = getBitmapAsInteger(checkInKey);

        UserCheckInVO userCheckInVO = new UserCheckInVO();
        userCheckInVO.setUserId(userId);
        userCheckInVO.setYearMonth(yearMonth);
        userCheckInVO.setBitmap(bitmap);
        userCheckInVO.setUserSubmitSummaryVO(new ArrayList<>());

        // 填充每日提交统计
        fillDaySubmitInfoFromRedis(userId, yearMonth, userCheckInVO);
        return userCheckInVO;
    }

    /**
     * 从 Redis 获取 bitmap 的整数值
     * 优化：直接读字符串解析，不需要操作字节数组
     */
    private Integer getBitmapAsInteger(String key) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null || value.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    /**
     * 检查年月格式是否为 yyyy-MM
     */
    private boolean checkYearMonthFormat(String yearMonth) {
        if (StrUtil.isBlank(yearMonth)) {
            return false;
        }
        if (yearMonth.length() != 7) {
            return false;
        }
        return YYYY_MM_PATTERN.matcher(yearMonth).matches();
    }

    /**
     * 【降级方案】从数据库 question_submit 表获取签到和提交信息
     * 优化：一次查询获取所有数据
     */
    private UserCheckInVO getUserCheckInFromDB(Long userId, String yearMonth) {
        // 查询每日提交统计
        List<UserSubmitSummaryVO> dailySummary = questionSubmitMapper.selectDailySubmitSummary(userId, yearMonth);

        // 从统计结果中生成 bitmap（有数据的日期才设置 bit）
        int bitmap = 0;
        if (dailySummary != null) {
            for (UserSubmitSummaryVO summary : dailySummary) {
                // 从 yyyy-MM-dd 中提取 day
                String yearMonthDay = summary.getYearMonthDay();
                if (yearMonthDay != null && yearMonthDay.length() == 10) {
                    int day = Integer.parseInt(yearMonthDay.substring(8, 10));
                    bitmap |= (1 << (day - 1));
                }
            }
        }

        // 构建 VO
        UserCheckInVO userCheckInVO = new UserCheckInVO();
        userCheckInVO.setUserId(userId);
        userCheckInVO.setYearMonth(yearMonth);
        userCheckInVO.setBitmap(bitmap);
        userCheckInVO.setUserSubmitSummaryVO(dailySummary != null ? dailySummary : new ArrayList<>());

        return userCheckInVO;
    }


    /**
     * 从 Redis Hash 填充每天的提交信息
     */
    private void fillDaySubmitInfoFromRedis(Long userId, String yearMonth, UserCheckInVO userCheckInVO) {
        Integer bitmap = userCheckInVO.getBitmap();
        if (bitmap == null || bitmap == 0) {
            return;
        }

        String submitKey = RedisKeyUtil.getUserSubmitDailyKey(userId, yearMonth);
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(submitKey);

        for (int day = 1; day <= 31; day++) {
            if ((bitmap & (1 << (day - 1))) != 0) {
                String submitField = day + ":submit";
                String acceptField = day + ":accept";

                int submitCount = 0;
                int acceptCount = 0;

                Object submitVal = entries.get(submitField);
                Object acceptVal = entries.get(acceptField);

                if (submitVal != null) {
                    submitCount = Integer.parseInt(submitVal.toString());
                }
                if (acceptVal != null) {
                    acceptCount = Integer.parseInt(acceptVal.toString());
                }

                if (submitCount > 0 || acceptCount > 0) {
                    UserSubmitSummaryVO summaryVO = new UserSubmitSummaryVO();
                    String dayStr = String.format("%02d", day);
                    summaryVO.setYearMonthDay(yearMonth + "-" + dayStr);
                    summaryVO.setSubmitCount(submitCount);
                    summaryVO.setAcceptCount(acceptCount);
                    userCheckInVO.getUserSubmitSummaryVO().add(summaryVO);
                }
            }
        }
    }

}
