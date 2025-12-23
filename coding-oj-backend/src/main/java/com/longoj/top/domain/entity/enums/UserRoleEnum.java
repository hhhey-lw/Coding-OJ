package com.longoj.top.domain.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 用户角色枚举
 *
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    USER("user", "用户"),
    ADMIN("admin", "管理员"),
    BAN("ban", "被封号");

    private final String code;
    private final String desc;

    public static UserRoleEnum getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (UserRoleEnum roleEnum : UserRoleEnum.values()) {
            if (roleEnum.getCode().equals(code)) {
                return roleEnum;
            }
        }
        return null;
    }

}
