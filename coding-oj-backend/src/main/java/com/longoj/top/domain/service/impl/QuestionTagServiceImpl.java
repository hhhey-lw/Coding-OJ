package com.longoj.top.domain.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.infrastructure.utils.PageUtil;
import com.longoj.top.infrastructure.utils.ThrowUtils;
import com.longoj.top.domain.entity.Question;
import com.longoj.top.domain.entity.QuestionTag;
import com.longoj.top.domain.entity.Tag;
import com.longoj.top.controller.dto.question.QuestionVO;
import com.longoj.top.domain.service.QuestionService;
import com.longoj.top.domain.service.QuestionTagService;
import com.longoj.top.infrastructure.mapper.QuestionTagMapper;
import com.longoj.top.domain.service.TagService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 题目-标签关联表
 *
 * @author 韦龙
 * @createDate 2025-06-16 14:50:42
 */
@Service
public class QuestionTagServiceImpl extends ServiceImpl<QuestionTagMapper, QuestionTag> implements QuestionTagService {

    @Resource
    private TagService tagService;

    @Lazy
    @Resource
    private QuestionService questionService;

    @Override
    public List<Tag> getTagsByQuestionId(Long questionId) {
        // 根据问题ID，查询关联表，再查询Tag表得到name，最后组装成["tag1", "tag2", ...]的JSON字符串
        if (null == questionId) {
            return Collections.emptyList();
        }

        // 1. 先查询关联表得到关联的标签
        Set<Long> tagIds = lambdaQuery()
                .select(QuestionTag::getTagId)
                .eq(QuestionTag::getQuestionId, questionId)
                .list()
                .stream()
                .map(QuestionTag::getTagId)
                .collect(Collectors.toSet());

        if (CollectionUtil.isEmpty(tagIds)) {
            return Collections.emptyList();
        }

        // 2. 根据标签的ID, 查询Tag表得到标签名称
        return tagService.lambdaQuery()
                .select(Tag::getTagName)
                .in(Tag::getId, tagIds)
                .eq(Tag::getIsDelete, Boolean.FALSE)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Boolean setTagsByQuestionId(Long questionId, List<String> tagList) {
        // 1. 标签预处理
        tagList = tagList.stream()
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();

        if (CollectionUtil.isEmpty(tagList)) {
            return true;
        }

        // 2. 获取Tag标签ID，并创建Tag中没有的标签
        List<Long> tagIds = tagService.batchQueryTagIdByTagName(tagList);

        // 3. 然后关联 Question和Tag表
        saveBatch(tagIds.stream()
                .map(tagId -> {
                    QuestionTag questionTag = new QuestionTag();
                    questionTag.setQuestionId(questionId);
                    questionTag.setTagId(tagId);
                    questionTag.setCreateTime(new Date());
                    return questionTag;
                })
                .toList());

        return true;
    }

    @Override
    public void removeByQuestionId(Long id) {
        LambdaUpdateWrapper<QuestionTag> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(QuestionTag::getIsDelete, Boolean.TRUE)
                .eq(QuestionTag::getQuestionId, id);

        baseMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Boolean delAndSetTagsByQuestionId(Long questionId, List<String> tagList) {
        baseMapper.delete(lambdaQuery()
                .eq(QuestionTag::getQuestionId, questionId)
                .getWrapper());

        return setTagsByQuestionId(questionId, tagList);
    }

    @Override
    public Page<QuestionVO> pageQuestionByTagId(Long tagId, Long current, Long pageSize) {
        if (tagId == null || tagId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Tag ID must be a positive number");
        }
        if (current <= 0 || pageSize <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Current page and page size must be positive numbers");
        }

        Page<QuestionTag> questionTagPage = page(new Page<>(current, pageSize), lambdaQuery()
                .eq(QuestionTag::getTagId, tagId).getWrapper()
        );

        if (CollectionUtil.isEmpty(questionTagPage.getRecords())) {
            return new Page<>(current, pageSize);
        }

        Set<Long> questionIds = questionTagPage.getRecords()
                .stream()
                .map(QuestionTag::getQuestionId)
                .collect(Collectors.toSet());

        // 根据questionId 查， 再转VO对象
        List<Question> questions = questionService.listByIds(questionIds);
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        return PageUtil.convertToVO(questionTagPage, questionTag ->
                QuestionVO.convertToVo(questionMap.get(questionTag.getQuestionId())));
    }

}




