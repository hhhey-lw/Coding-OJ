package com.longoj.top.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.controller.dto.question.QuestionSubmitAddRequest;
import com.longoj.top.controller.dto.question.QuestionSubmitQueryRequest;
import com.longoj.top.domain.entity.QuestionSubmit;
import com.longoj.top.controller.dto.question.QuestionSubmitVO;
import com.longoj.top.controller.dto.user.UserSubmitInfoVO;
import com.longoj.top.domain.entity.enums.QuestionPassStatusEnum;

import java.util.List;

/**
* @author 韦龙
* @createDate 2025-05-15 00:13:26
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    /**
     * 提交题目
     */
    Long submit(QuestionSubmitAddRequest questionSubmitAddRequest);

    /**
     * 提交请求是否执行过
     */
    boolean isExecuted(Long id);

    /**
     * 获取通过题目数排名前 N 的用户列表
     * @param topNumber 前 N 名
     * @return 用户提交信息列表
     */
    List<UserSubmitInfoVO> getTopPassedQuestionUserList(int topNumber);

    /**
     * 获取用户通过的题目 ID 列表
     * @param userId 用户 ID
     * @return 题目 ID 列表
     */
    List<Long> listPassedQuestionId(Long userId);

    /**
     * 分页查询题目提交记录
     */
    Page<QuestionSubmitVO> pageQuery(QuestionSubmitQueryRequest questionSubmitQueryRequest);

    /**
     * 分页查询我的提交记录
     */
    Page<QuestionSubmitVO> pageMy(Integer questionId, Integer status, String language, int current, int pageSize);

    /**
     * 修改提交的通过状态
     */
    void updatePassStatus(Long id, QuestionPassStatusEnum passStatusEnum);
}
