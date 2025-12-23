package com.longoj.top.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.domain.entity.QuestionTag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.controller.dto.question.QuestionVO;
import com.longoj.top.domain.entity.Tag;

import java.util.List;

/**
 * 题目标签关联服务
 *
 * @author 韦龙
 */
public interface QuestionTagService extends IService<QuestionTag> {

    /**
     * 根据题目ID获取标签列表
     */
    List<Tag> getTagsByQuestionId(Long questionId);

    /**
     * 关联题目和标签
     */
    Boolean setTagsByQuestionId(Long questionId, List<String> tagList);

    /**
     * 根据题目ID删除关联
     */
    void removeByQuestionId(Long id);

    /**
     * 关联题目和标签（移动过期的，补充新增的）
     */
    Boolean delAndSetTagsByQuestionId(Long questionId, List<String> tagList);

    /**
     * 根据标签ID分页获取题目列表
     */
    Page<QuestionVO> pageQuestionByTagId(Long tagId, Long current, Long pageSize);

}
