package com.longoj.top.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class QuestionPassedServiceTest {

    @Resource
    private QuestionPassedService questionPassedService;

    @Resource
    private QuestionService questionService;

    @Test
    public void testAddUserPassedQuestion() {
        questionPassedService.addUserPassedQuestion(1923032662442586113L, 1L);
        questionPassedService.addUserPassedQuestion(1923032662442586113L, 1923016196733345794L);
        questionPassedService.addUserPassedQuestion(1923032662442586113L, 1925133048482111490L);
        questionPassedService.addUserPassedQuestion(1923032662442586113L, 1926897342450282497L);

        questionPassedService.getQuestionIdsByUserId(1L).stream().forEach(System.out::println);

        System.out.println(questionService.isPassed(1923032662442586113L, 1L));
    }

}
