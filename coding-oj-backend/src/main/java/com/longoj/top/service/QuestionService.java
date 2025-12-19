package com.longoj.top.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.model.dto.question.QuestionAddRequest;
import com.longoj.top.model.dto.question.QuestionQueryRequest;
import com.longoj.top.model.dto.question.QuestionUpdateRequest;
import com.longoj.top.model.entity.Question;
import com.longoj.top.model.vo.QuestionVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 韦龙
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2025-05-15 00:13:26
*/
public interface QuestionService extends IService<Question> {


    QuestionVO getQuestionVO(Question question);

    List<QuestionVO> getQuestionVOPage(List<Question> records);

    Wrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);

    void validQuestion(Question question, boolean b);

    Long addQuestion(QuestionAddRequest questionAddRequest, HttpServletRequest request);

    int updateQuestionSubmitNum(Long questionId);

    int updateQuestionAcceptedNum(Long questionId);

    Boolean updateQuestion(QuestionUpdateRequest questionUpdateRequest);

    boolean isPassed(Long questionId, Long userId);

    Page<QuestionVO> getQuestionVO(QuestionQueryRequest questionQueryRequest, HttpServletRequest request);
}
