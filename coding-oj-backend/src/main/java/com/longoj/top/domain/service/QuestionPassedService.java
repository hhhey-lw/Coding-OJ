package com.longoj.top.domain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.domain.entity.QuestionPassed;

import java.util.List;

/**
* @author 韦龙
* @description 针对表【question_passed(用户通过题目记录表)】的数据库操作Service
* @createDate 2025-06-18 19:27:18
*/
public interface QuestionPassedService extends IService<QuestionPassed> {

    /**
     * 添加用户通过题目记录
     *
     * @param questionId 题目ID
     * @param userId     用户ID
     * @return 是否添加成功
     */
    boolean addUserPassedQuestion(Long questionId, Long userId);

    /**
     * 根据用户ID获取通过的题目ID列表
     *
     * @param userId 用户ID
     * @return 通过的题目ID列表
     */
    List<Long> getQuestionIdsByUserId(Long userId);

}
