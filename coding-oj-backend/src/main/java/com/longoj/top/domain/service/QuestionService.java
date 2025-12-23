package com.longoj.top.domain.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.controller.dto.question.*;
import com.longoj.top.controller.dto.user.UserSubmitInfoVO;
import com.longoj.top.domain.entity.Question;
import com.longoj.top.domain.entity.Tag;

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
     * 分页查询
     */
    Page<Question>  page(String searchKey, Integer difficulty, List<String> tags, Long userId, int current, int pageSize);
    /**
     * 分页查询
     */
    Page<QuestionVO>  pageVO(String searchKey, Integer difficulty, List<String> tags, Long userId, int current, int pageSize);

    /**
     * 更新题目信息
     */
    Boolean update(QuestionUpdateRequest questionUpdateRequest);

    /**
     * 更新题目提交数
     */
    int updateQuestionSubmitNum(Long questionId);

    /**
     * 更新题目通过数
     */
    int updateQuestionAcceptedNum(Long questionId);

    /**
     * 判断用户是否通过该题目
     */
    boolean isPassed(Long questionId, Long userId);

}
