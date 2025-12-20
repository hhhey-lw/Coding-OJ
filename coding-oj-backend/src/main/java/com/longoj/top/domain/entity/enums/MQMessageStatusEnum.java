package com.longoj.top.domain.entity.enums;

public interface MQMessageStatusEnum {

    int PENDING = 0;

    int SUCCESS = 1; // 成功

    int FAILED = 2; // 失败

    int FINAL_FAILED = 3; // 最终失败，无法路由

}
