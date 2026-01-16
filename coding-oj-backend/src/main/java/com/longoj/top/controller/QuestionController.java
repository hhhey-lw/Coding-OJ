package com.longoj.top.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.controller.dto.post.TagVO;
import com.longoj.top.controller.dto.question.*;
import com.longoj.top.controller.dto.user.UserSubmitInfoVO;
import com.longoj.top.domain.service.*;
import com.longoj.top.infrastructure.aop.annotation.AuthCheck;
import com.longoj.top.controller.dto.BaseResponse;
import com.longoj.top.controller.dto.DeleteRequest;
import com.longoj.top.infrastructure.aop.annotation.RateLimit;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.utils.ResultUtils;
import com.longoj.top.domain.entity.constant.UserConstant;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.infrastructure.utils.ThrowUtils;
import com.longoj.top.domain.entity.Question;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * 题目接口
 */
@Slf4j
@Api("题目接口")
@RequestMapping("/question")
@RestController
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionTagService questionTagService;

    @Resource
    private TagService tagService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserCheckInService userCheckInService;

    // Prometheus 指标
    private final Counter submitCounter;      // 提交次数计数器
    private final Counter submitSuccessCounter; // 提交成功计数器
    private final Counter submitFailCounter;    // 提交失败计数器
    private final Timer submitTimer;          // 提交耗时计时器

    public QuestionController(MeterRegistry registry) {
        this.submitCounter = Counter.builder("oj.submit.total")
                .description("代码提交总次数")
                .register(registry);
        this.submitSuccessCounter = Counter.builder("oj.submit.success")
                .description("代码提交成功次数")
                .register(registry);
        this.submitFailCounter = Counter.builder("oj.submit.fail")
                .description("代码提交失败次数")
                .register(registry);
        this.submitTimer = Timer.builder("oj.submit.duration")
                .description("代码提交处理耗时")
                .register(registry);
    }

    // region 增删改查

    /**
     * 创建题目
     */
    @ApiOperation("创建题目")
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest) {
        return ResultUtils.success(questionService.addQuestion(questionAddRequest));
    }

    /**
     * 删除题目
     */
    @ApiOperation("删除题目")
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(questionService.deleteQuestion(deleteRequest.getId()));
    }

    /**
     * 更新题目
     */
    @ApiOperation("更新题目")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        return ResultUtils.success(questionService.updateQuestion(questionUpdateRequest));
    }

    /**
     * 获取题目实体
     */
    @ApiOperation("获取题目实体")
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Question> getQuestionById(@RequestParam("id") Long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(questionService.getById(id));
    }

    /**
     * 分页获取题目列表（仅管理员）
     */
    @ApiOperation("分页获取题目")
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        return ResultUtils.success(questionService.page(questionQueryRequest.getSearchKey(), questionQueryRequest.getDifficulty(), questionQueryRequest.getTags(),
                questionQueryRequest.getCurrent(), questionQueryRequest.getPageSize()));
    }

    // ==============> 题目VO相关接口 <================
    /**
     * 分页获取题目封装列表
     */
    @ApiOperation("分页获取题目封装列表")
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        return ResultUtils.success(questionService.pageVO(questionQueryRequest.getSearchKey(), questionQueryRequest.getDifficulty(), questionQueryRequest.getTags(),
                questionQueryRequest.getCurrent(), questionQueryRequest.getPageSize()));
    }

    /**
     * 获取题目VO
     */
    @ApiOperation("获取题目VO")
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(@RequestParam("id") Long id) {
        return ResultUtils.success(questionService.getQuestionVOById(id));
    }

    // endregion

    // ================> 题目标签相关接口 <================

    @ApiOperation("根据标签分页查询题目列表")
    @GetMapping("/tag/id/{tagId}/{current}/{pageSize}")
    public BaseResponse<Page<QuestionVO>> getQuestionByTagId(@PathVariable(value = "tagId") Long tagId,
                                                             @PathVariable(value = "current") Long current,
                                                             @PathVariable(value = "pageSize") Long pageSize) {
        return ResultUtils.success(questionTagService.pageQuestionByTagId(tagId, current, pageSize));
    }

    @ApiOperation("分页查询标签列表")
    @GetMapping("/tag/queryTag/{current}/{pageSize}")
    public BaseResponse<Page<TagVO>> getTagByPage(@PathVariable(value = "current") Long current,
                                                  @PathVariable(value = "pageSize") Long pageSize) {
        return ResultUtils.success(tagService.getTagBypage(current, pageSize));
    }

    // ================> 题目提交相关接口 <================
    /**
     * 提交&执行代码
     */
    @RateLimit
    @ApiOperation("提交代码")
    @PostMapping("/submit/do")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Long> doSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 记录提交次数
        submitCounter.increment();

        return submitTimer.record(() -> {
            try {
                // 提交代码进行判题
                Long questionSubmitId = questionSubmitService.submit(questionSubmitAddRequest);

                // 更新用户当天签到和每日提交统计
                userCheckInService.updateUserCheckInByOneDay();
                // 更新题目提交数
                questionService.updateQuestionSubmitNum(questionSubmitAddRequest.getQuestionId());

                // 记录成功次数
                submitSuccessCounter.increment();
                return ResultUtils.success(questionSubmitId);
            } catch (Exception e) {
                // 记录失败次数
                submitFailCounter.increment();
                throw e;
            }
        });
    }


    @ApiOperation("分页查询提交的记录")
    @PostMapping("/submit/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        return ResultUtils.success(questionSubmitService.pageQuery(questionSubmitQueryRequest));
    }

    @ApiOperation("分页查询我的提交")
    @PostMapping("/submit/list/page/user")
    public BaseResponse<Page<QuestionSubmitVO>> listUserQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        int current = questionSubmitQueryRequest.getCurrent();
        int pageSize = questionSubmitQueryRequest.getPageSize();
        return ResultUtils.success(questionSubmitService.pageMy(questionSubmitQueryRequest.getQuestionId(), questionSubmitQueryRequest.getStatus(), questionSubmitQueryRequest.getLanguage(), current, pageSize));
    }

    @ApiOperation("提交榜单Top N 用户")
    @GetMapping("/submit/topPassed/{topNumber}")
    public BaseResponse<List<UserSubmitInfoVO>> getTopPassedQuestionUserList(@PathVariable int topNumber) {
        return ResultUtils.success(questionSubmitService.getTopPassedQuestionUserList(topNumber));
    }

}
