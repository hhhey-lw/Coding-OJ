package com.longoj.top.domain.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 文件上传业务类型枚举
 *
 */
@Getter
@AllArgsConstructor
public enum FileUploadBizEnum {

    USER_AVATAR(0, "user_avatar", "用户头像");

    private final Integer code;

    private final String enCode;

    private final String description;

    /**
     * 根据 enCode 获取枚举
     */
    public static FileUploadBizEnum getByEnCode(String enCode) {
        if (StringUtils.isEmpty(enCode)) {
            return null;
        }
        for (FileUploadBizEnum bizEnum : FileUploadBizEnum.values()) {
            if (bizEnum.getEnCode().equals(enCode)) {
                return bizEnum;
            }
        }
        return null;
    }
}
