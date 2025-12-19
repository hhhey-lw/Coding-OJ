package com.longoj.top.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.model.dto.questionsubmit.UserPassedCountDTO;
import com.longoj.top.model.entity.QuestionPassed;

import java.util.List;

/**
* @author 韦龙
* @description 针对表【question_passed(用户通过题目记录表)】的数据库操作Service
* @createDate 2025-06-18 19:27:18
*/
public interface QuestionPassedService extends IService<QuestionPassed> {

    boolean addUserPassedQuestion(Long questionId, Long userId);

    List<Long> getQuestionIdsByUserId(Long userId);

}
