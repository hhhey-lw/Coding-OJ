package com.longoj.top.domain.entity.enums;

import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum QuestionSubmitLanguageEnum {

     JAVA("java", "java"),
     CPP("cpp", "cpp"),
     Go("go", "go");

     private final String code;
     private final String description;

     public static boolean isExist(String code) {
          if (StrUtil.isBlank(code)) {
               return false;
          }
          for (QuestionSubmitLanguageEnum languageEnum : values()) {
               if (languageEnum.description.equals(code)){
                    return true;
               }
          }
          return false;
     }

}
