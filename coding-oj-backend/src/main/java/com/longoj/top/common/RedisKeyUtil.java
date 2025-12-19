package com.longoj.top.common;

public class RedisKeyUtil {

    public static final String LOCK = "LOCK:";
    public static final String DELIMITER_COLON = ":";

    public static final Integer TOP_PASSED_NUMBER = 10;
    public static final String TOP_PASSED_NUMBER_KEY = "TOP_PASSED_NUMBER_USER_IDS:";

    public static final String USER_PASSED_QUESTION_KEY = "USER_PASSED_QUESTION_KEY:";

    public static final String QUESTION_LIST_KEY = "QUESTION_LIST:";

    public static String getTopPassedNumberKey() {
        return TOP_PASSED_NUMBER_KEY + TOP_PASSED_NUMBER;
    }

    public static String getUserPassedQuestionKey(Long userId) {
        return USER_PASSED_QUESTION_KEY + userId;
    }

    public static String getQuestionListKey(Long current, Long size) {
        return QUESTION_LIST_KEY + current + DELIMITER_COLON + size;
    }

    public static String getQuestionListTotalKey(Long current, Long size) {
        return QUESTION_LIST_KEY + current + DELIMITER_COLON + size + ":total";
    }

}
