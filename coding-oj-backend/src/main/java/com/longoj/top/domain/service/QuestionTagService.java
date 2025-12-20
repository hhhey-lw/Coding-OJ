package com.longoj.top.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.domain.entity.QuestionTag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.controller.dto.question.QuestionVO;

import java.util.List;

/**
 * 题目标签关联服务
 *
 * @author 韦龙
 */
public interface QuestionTagService extends IService<QuestionTag> {
    List<String> getTagsByQuestionId(Long questionId);

    /**
     * 关联题目和标签
     */
    Boolean setTagsByQuestionId(Long questionId, List<String> tagList);

    /**
     * 根据题目ID删除关联
     */
    void removeByQuestionId(Long id);

    Boolean delAndSetTagsByQuestionId(Long questionId, List<String> tagList);

    Page<QuestionVO> getQuestionByTagName(String tagName, Long current, Long pageSize);

    Page<QuestionVO> getQuestionByTagId(Long tagId, Long current, Long pageSize);

}
