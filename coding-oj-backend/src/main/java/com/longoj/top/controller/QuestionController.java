package com.longoj.top.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.controller.dto.post.TagVO;
import com.longoj.top.domain.service.QuestionTagService;
import com.longoj.top.domain.service.TagService;
import com.longoj.top.infrastructure.aop.annotation.AuthCheck;
import com.longoj.top.controller.dto.BaseResponse;
import com.longoj.top.controller.dto.DeleteRequest;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.utils.ResultUtils;
import com.longoj.top.domain.entity.constant.UserConstant;
import com.longoj.top.controller.dto.question.QuestionAddRequest;
import com.longoj.top.controller.dto.question.QuestionQueryRequest;
import com.longoj.top.controller.dto.question.QuestionUpdateRequest;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.infrastructure.utils.ThrowUtils;
import com.longoj.top.domain.entity.Question;
import com.longoj.top.controller.dto.question.QuestionVO;
import com.longoj.top.domain.service.QuestionService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/**
 * 题目接口
 */
@Slf4j
@RequestMapping("/question")
@RestController
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionTagService questionTagService;

    @Resource
    private TagService tagService;

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
     * 获取题目VO
     */
    @ApiOperation("获取题目VO")
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(@RequestParam("id") Long id) {
        return ResultUtils.success(questionService.getQuestionVOById(id));
    }

    /**
     * 分页获取题目列表（仅管理员）
     *
     * @param questionQueryRequest
     * @return
     */
    @ApiOperation("分页获取题目")
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));



        return ResultUtils.success(questionPage);
    }

    /**
     * 分页获取题目封装列表
     *
     * @param questionQueryRequest
     * @return
     */
    @ApiOperation("分页获取题目封装列表")
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        return ResultUtils.success(questionService.getQuestionVO(questionQueryRequest, request));
    }

    // endregion

    /**
     * 更新题目信息
     *
     * @param questionUpdateRequest
     * @return
     */
    @Deprecated
    @ApiOperation("更新题目信息")
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    // ================> 题目标签相关接口 <================

    @GetMapping("/tag/name/{tagName}/{current}/{pageSize}")
    public BaseResponse<Page<QuestionVO>> getQuestionByTagName(@PathVariable(value = "tagName") String tagName,
                                                               @PathVariable(value = "current") Long current,
                                                               @PathVariable(value = "pageSize") Long pageSize) {
        return ResultUtils.success(questionTagService.getQuestionByTagName(tagName, current, pageSize));
    }

    @GetMapping("/tag/id/{tagId}/{current}/{pageSize}")
    public BaseResponse<Page<QuestionVO>> getQuestionByTagId(@PathVariable(value = "tagId") Long tagId,
                                                             @PathVariable(value = "current") Long current,
                                                             @PathVariable(value = "pageSize") Long pageSize) {
        return ResultUtils.success(questionTagService.getQuestionByTagId(tagId, current, pageSize));
    }

    @GetMapping("/tag/queryTag/{current}/{pageSize}")
    public BaseResponse<Page<TagVO>> getTagByPage(@PathVariable(value = "current") Long current,
                                                  @PathVariable(value = "pageSize") Long pageSize) {
        return ResultUtils.success(tagService.getTagBypage(current, pageSize));
    }

}
