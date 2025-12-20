package com.longoj.top.domain.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.controller.dto.question.QuestionAddRequest;
import com.longoj.top.controller.dto.question.QuestionQueryRequest;
import com.longoj.top.controller.dto.question.QuestionUpdateRequest;
import com.longoj.top.domain.entity.Question;
import com.longoj.top.controller.dto.question.QuestionVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题目服务
 * @author 韦龙
*/
public interface QuestionService extends IService<Question> {

    /**
     * 创建题目
     */
    Long addQuestion(QuestionAddRequest questionAddRequest);

    /**
     * 删除题目
     */
    boolean deleteQuestion(Long id);

    /**
     * 更新题目
     */
    Boolean updateQuestion(QuestionUpdateRequest questionUpdateRequest);

    /**
     * 获取题目对象
     */
    QuestionVO getQuestionVOById(Long id);








    /**
     * 获取题目对象列表
     *
     * @param records
     * @return
     */
    List<QuestionVO> getQuestionVOPage(List<Question> records);

    Wrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);

    int updateQuestionSubmitNum(Long questionId);

    int updateQuestionAcceptedNum(Long questionId);

    boolean isPassed(Long questionId, Long userId);

    Page<QuestionVO> getQuestionVO(QuestionQueryRequest questionQueryRequest, HttpServletRequest request);

}
