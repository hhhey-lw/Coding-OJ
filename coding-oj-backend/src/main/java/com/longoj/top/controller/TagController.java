package com.longoj.top.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.common.BaseResponse;
import com.longoj.top.common.ResultUtils;
import com.longoj.top.model.vo.QuestionVO;
import com.longoj.top.model.vo.TagVO;
import com.longoj.top.service.QuestionTagService;
import com.longoj.top.service.TagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    private QuestionTagService questionTagService;

    @Resource
    private TagService tagService;

    @GetMapping("/name/{tagName}/{current}/{pageSize}")
    public BaseResponse<Page<QuestionVO>> getQuestionByTagName(@PathVariable(value = "tagName") String tagName,
                                                               @PathVariable(value = "current") Long current,
                                                               @PathVariable(value = "pageSize") Long pageSize) {
        return ResultUtils.success(questionTagService.getQuestionByTagName(tagName, current, pageSize));
    }

    @GetMapping("/id/{tagId}/{current}/{pageSize}")
    public BaseResponse<Page<QuestionVO>> getQuestionByTagId(@PathVariable(value = "tagId") Long tagId,
                                                               @PathVariable(value = "current") Long current,
                                                               @PathVariable(value = "pageSize") Long pageSize) {
        return ResultUtils.success(questionTagService.getQuestionByTagId(tagId, current, pageSize));
    }

    @GetMapping("/queryTag/{current}/{pageSize}")
    public BaseResponse<Page<TagVO>> getTagByPage(@PathVariable(value = "current") Long current,
                                                                @PathVariable(value = "pageSize") Long pageSize) {
        return ResultUtils.success(tagService.getTagBypage(current, pageSize));
    }

}
