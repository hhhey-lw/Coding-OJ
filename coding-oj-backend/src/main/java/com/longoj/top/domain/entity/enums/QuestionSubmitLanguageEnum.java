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

     private final String text;
     private final String value;

     public List<String> getValuesList() {
         return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
     }

     public static boolean isExist(String value) {
          if (StrUtil.isBlank(value)) {
               return false;
          }
          for (QuestionSubmitLanguageEnum languageEnum : values()) {
               if (languageEnum.value.equals(value)){
                    return true;
               }
          }
          return false;
     }

}
