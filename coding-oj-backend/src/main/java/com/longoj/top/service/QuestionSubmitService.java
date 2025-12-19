package com.longoj.top.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.longoj.top.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.longoj.top.model.entity.QuestionSubmit;
import com.longoj.top.model.entity.User;
import com.longoj.top.model.vo.QuestionSubmitVO;
import com.longoj.top.model.vo.UserSubmitInfoVO;
import com.longoj.top.model.vo.UserVO;

import java.util.List;

/**
* @author 韦龙
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2025-05-15 00:13:26
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {

    Long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    Wrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);

    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> page, User loginUser);

    boolean isQuestionSubmitExecuted(Long id);

    List<UserSubmitInfoVO> getTopPassedQuestionUserList(int topNumber);
}
