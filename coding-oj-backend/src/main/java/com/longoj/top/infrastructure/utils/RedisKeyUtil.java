package com.longoj.top.infrastructure.utils;

public class RedisKeyUtil {

    public static final String LOCK = "LOCK:";
    public static final String DELIMITER_COLON = ":";

    public static final Integer TOP_PASSED_NUMBER = 10;
    public static final String TOP_PASSED_NUMBER_KEY = "TOP_PASSED_NUMBER_USER_IDS:";

    public static final String USER_PASSED_QUESTION_KEY = "USER_PASSED_QUESTION_KEY:";

    public static final String QUESTION_LIST_KEY = "QUESTION_LIST:";

    // 用户签到 bitmap key: user:checkin:{userId}:{yyyy-MM}
    public static final String USER_CHECKIN_KEY = "user:checkin:";

    // 用户每日提交统计 hash key: user:submit:daily:{userId}:{yyyy-MM}
    // hash field: {day}:submit, {day}:accept
    public static final String USER_SUBMIT_DAILY_KEY = "user:submit:daily:";

    /**
     * 获取通过题目数排名前 N 用户 key
     */
    public static String getTopPassedNumberKey() {
        return TOP_PASSED_NUMBER_KEY + TOP_PASSED_NUMBER;
    }

    /**
     * 获取用户通过题目 key
     * @param userId 用户ID
     */
    public static String getUserPassedQuestionKey(Long userId) {
        return USER_PASSED_QUESTION_KEY + userId;
    }

    /**
     * 获取用户签到 key
     * @param userId 用户ID
     * @param yearMonth 年月，格式 yyyy-MM
     */
    public static String getUserCheckInKey(Long userId, String yearMonth) {
        return USER_CHECKIN_KEY + userId + DELIMITER_COLON + yearMonth;
    }

    /**
     * 获取用户每日提交统计 key
     * @param userId 用户ID
     * @param yearMonth 年月，格式 yyyy-MM
     */
    public static String getUserSubmitDailyKey(Long userId, String yearMonth) {
        return USER_SUBMIT_DAILY_KEY + userId + DELIMITER_COLON + yearMonth;
    }

}
