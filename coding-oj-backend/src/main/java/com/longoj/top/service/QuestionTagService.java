package com.longoj.top.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.model.entity.QuestionTag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.model.vo.QuestionVO;

import java.util.List;

/**
* @author 韦龙
* @description 针对表【question_tag(题目-标签关联表)】的数据库操作Service
* @createDate 2025-06-16 14:50:42
*/
public interface QuestionTagService extends IService<QuestionTag> {
    List<String> getTagsByQuestionId(Long questionId);

    Boolean setTagsByQuestionId(Long questionId, List<String> tagList);

    Boolean delAndSetTagsByQuestionId(Long questionId, List<String> tagList);

    Page<QuestionVO> getQuestionByTagName(String tagName, Long current, Long pageSize);

    Page<QuestionVO> getQuestionByTagId(Long tagId, Long current, Long pageSize);
}
