package com.longoj.top.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.infrastructure.aop.annotation.RateLimit;
import com.longoj.top.controller.dto.BaseResponse;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.utils.ResultUtils;
import com.longoj.top.domain.service.QuestionService;
import com.longoj.top.domain.service.QuestionSubmitService;
import com.longoj.top.domain.service.UserCheckInService;
import com.longoj.top.domain.service.UserService;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.controller.dto.question.QuestionSubmitAddRequest;
import com.longoj.top.controller.dto.question.QuestionSubmitQueryRequest;
import com.longoj.top.domain.entity.QuestionSubmit;
import com.longoj.top.domain.entity.User;
import com.longoj.top.controller.dto.question.QuestionSubmitVO;
import com.longoj.top.controller.dto.user.UserSubmitInfoVO;
import com.longoj.top.service.*;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题目提交接口
 */
@Slf4j
@RequestMapping("/question_submit")
@RestController
public class QuestionSubmitController {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;

    @Resource
    private UserCheckInService userCheckInService;

    /**
     *
     * @param questionSubmitAddRequest
     * @param request
     */
    @RateLimit
    @ApiOperation("提交代码")
    @PostMapping("/do")
    public BaseResponse<Long> doSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                          HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        final User loginUser = userService.getLoginUser(request);

        // 提交代码进行判题
        Long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);

        // 更新用户当天签到和每日提交统计
        userCheckInService.autoUserSignInOneDaySummary(loginUser.getId());
        // 更新题目提交数
        questionService.updateQuestionSubmitNum(questionSubmitAddRequest.getQuestionId());

        return ResultUtils.success(questionSubmitId);
    }


    @ApiOperation("分页查询提交问题")
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        int current = questionSubmitQueryRequest.getCurrent();
        int pageSize = questionSubmitQueryRequest.getPageSize();

        Page<QuestionSubmit> page = questionSubmitService.page(new Page<>(current, pageSize),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));

        User loginUser;
        try {
            loginUser = userService.getLoginUser(request);
        } catch (Exception e) {
            log.error("获取登录用户失败", e);
            // 如果获取登录用户失败，则不返回用户信息
            loginUser = null;
        }
        // final User loginUser = UserContext.getUser();
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(page, loginUser));
    }

    @ApiOperation("分页查询用户自己提交问题")
    @PostMapping("/list/page/user")
    public BaseResponse<Page<QuestionSubmitVO>> listUserQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        int current = questionSubmitQueryRequest.getCurrent();
        int pageSize = questionSubmitQueryRequest.getPageSize();

        final User loginUser = userService.getLoginUser(request);
        // final User loginUser = UserContext.getUser();
        questionSubmitQueryRequest.setUserId(loginUser.getId());
        Page<QuestionSubmit> page = questionSubmitService.page(new Page<>(current, pageSize),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));

        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(page, loginUser));
    }

    @GetMapping("/topPassed/{topNumber}")
    public BaseResponse<List<UserSubmitInfoVO>> getTopPassedQuestionUserList(@PathVariable int topNumber) {
        return ResultUtils.success(questionSubmitService.getTopPassedQuestionUserList(topNumber));
    }
}
