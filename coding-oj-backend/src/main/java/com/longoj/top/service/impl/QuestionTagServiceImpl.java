package com.longoj.top.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longoj.top.common.ErrorCode;
import com.longoj.top.exception.BusinessException;
import com.longoj.top.exception.ThrowUtils;
import com.longoj.top.model.entity.Question;
import com.longoj.top.model.entity.QuestionTag;
import com.longoj.top.model.entity.Tag;
import com.longoj.top.model.vo.QuestionVO;
import com.longoj.top.service.QuestionService;
import com.longoj.top.service.QuestionTagService;
import com.longoj.top.mapper.QuestionTagMapper;
import com.longoj.top.service.TagService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 韦龙
* @description 针对表【question_tag(题目-标签关联表)】的数据库操作Service实现
* @createDate 2025-06-16 14:50:42
*/
@Service
public class QuestionTagServiceImpl extends ServiceImpl<QuestionTagMapper, QuestionTag>
    implements QuestionTagService{

    @Resource
    private TagService tagService;

    @Lazy
    @Resource
    private QuestionService questionService;

    @Override
    public List<String> getTagsByQuestionId(Long questionId) {
        // 根据问题ID，查询关联表，再查询Tag表得到name，最后组装成["tag1", "tag2", ...]的JSON字符串
        if (null == questionId) {
            return Collections.EMPTY_LIST;
        }

        // 1. 先查询 关联表得到关联的标签
        List<Long> tagIds = lambdaQuery()
                .select(QuestionTag::getTagId)
                .eq(QuestionTag::getQuestionId, questionId)
                .list()
                .stream()
                .map(QuestionTag::getTagId)
                .toList();

        if (CollectionUtil.isEmpty(tagIds)) {
            return Collections.EMPTY_LIST;
        }
        // 2. 根据标签的ID, 查询Tag表得到标签名称
        List<String> tagNames = tagService.lambdaQuery()
                .select(Tag::getTagName)
                .in(Tag::getId, tagIds)
                .eq(Tag::getIsDelete, 0) // 确保只获取未删除的标签
                .list()
                .stream()
                .map(Tag::getTagName)
                .toList();

        return tagNames;
    }

    @Override
    @Transactional(propagation =  Propagation.MANDATORY)
    public Boolean setTagsByQuestionId(Long questionId, List<String> tagList) {
        try {
            // 1.解析tagsJSONStr为List<Tag>数组
            tagList = tagList.stream()
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .toList();

            if (CollectionUtil.isEmpty(tagList)) {
                // 如果标签列表为空，直接返回
                return true;
            }

            // 2. 获取Tag标签ID，并创建Tag中没有的标签
            List<Long> tagIds = tagService.queryTagIdByTagNameBatch(tagList);

            // 3. 然后关联 Question和Tag表
            List<QuestionTag> questionTags = tagIds.stream()
                    .map(tagId -> {
                        QuestionTag questionTag = new QuestionTag();
                        questionTag.setQuestionId(questionId);
                        questionTag.setTagId(tagId);
                        questionTag.setCreateTime(new Date());
                        return questionTag;
                    })
                    .toList();
            saveBatch(questionTags);
        } catch (Exception e) {
            // 如果解析失败，返回空字符串
            return false;
        }
        return true;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Boolean delAndSetTagsByQuestionId(Long questionId, List<String> tagList) {
        int delete = baseMapper.delete(lambdaQuery()
                .eq(QuestionTag::getQuestionId, questionId)
                .getWrapper());
        // if (delete == 0)
        //     return false;

        return setTagsByQuestionId(questionId, tagList);
    }

    @Override
    public Page<QuestionVO> getQuestionByTagName(String tagName, Long current, Long pageSize) {
        Tag tag = tagService.lambdaQuery()
                .eq(Tag::getTagName, tagName)
                .one();
        ThrowUtils.throwIf(null == tag, ErrorCode.PARAMS_ERROR,"标签不存在");

        Page<QuestionTag> questionTagPage = page(new Page<>(current, pageSize), lambdaQuery()
                .eq(QuestionTag::getTagId, tag.getId()).getWrapper()
        );

        if (CollectionUtil.isEmpty(questionTagPage.getRecords())) {
            return new Page<>(current, pageSize);
        }

        List<Long> questionIds = questionTagPage.getRecords()
                .stream()
                .map(QuestionTag::getQuestionId)
                .toList();

        // 根据questionId 查， 再转VO对象
        List<Question> questions = questionService.listByIds(questionIds);

        Page<QuestionVO> questionVOPage = new Page<>(questionTagPage.getCurrent(), questionTagPage.getSize(), questionTagPage.getTotal());
        List<QuestionVO> voRecords = new ArrayList<>((int) questionTagPage.getSize());
        for (Question question : questions) {
            voRecords.add(QuestionVO.objToVo(question));
        }
        questionVOPage.setRecords(voRecords);
        return questionVOPage;
    }

    @Override
    public Page<QuestionVO> getQuestionByTagId(Long tagId, Long current, Long pageSize) {
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

        List<Long> questionIds = questionTagPage.getRecords()
                .stream()
                .map(QuestionTag::getQuestionId)
                .toList();

        // 根据questionId 查， 再转VO对象
        List<Question> questions = questionService.listByIds(questionIds);

        Page<QuestionVO> questionVOPage = new Page<>(questionTagPage.getCurrent(), questionTagPage.getSize(), questionTagPage.getTotal());
        List<QuestionVO> voRecords = new ArrayList<>((int) questionTagPage.getSize());
        for (Question question : questions) {
            voRecords.add(QuestionVO.objToVo(question));
        }
        questionVOPage.setRecords(voRecords);
        return questionVOPage;
    }

}




