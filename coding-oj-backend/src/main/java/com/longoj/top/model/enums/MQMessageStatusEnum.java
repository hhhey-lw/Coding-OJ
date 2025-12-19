package com.longoj.top.model.enums;

public class MQMessageStatusEnum {

    public static final int PENDING = 0;

    public static final int SUCCESS = 1; // 成功

    public static final int FAILED = 2; // 失败

    public static final int FINAL_FAILED = 3; // 最终失败，无法路由

}
