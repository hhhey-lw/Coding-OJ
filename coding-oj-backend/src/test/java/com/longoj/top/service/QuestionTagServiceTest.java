// package com.longoj.top.service;
//
// import cn.hutool.json.JSONUtil;
// import org.junit.jupiter.api.Test;
// import org.springframework.boot.test.context.SpringBootTest;
//
// import javax.annotation.Resource;
// import java.util.ArrayList;
// import java.util.Arrays;
//
// @SpringBootTest
// public class QuestionTagServiceTest {
//
//     @Resource
//     private QuestionTagService questionTagService;
//
//     @Test
//     public void testGetTagsJSONStrByQuestionId() {
//
//         Boolean b = questionTagService.setTagsByQuestionId(333L, Arrays.asList("测试Tag-1", "测试Tag-2"));
//         System.out.println(String.format("set status {}", b));
//
//         System.out.println(JSONUtil.toJsonStr(questionTagService.getTagsByQuestionId(333L)));
//     }
//
// }
