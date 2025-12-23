package com.longoj.top.domain.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.domain.entity.QuestionSubmit;
import com.longoj.top.domain.entity.enums.QuestionPassStatusEnum;
import com.longoj.top.domain.entity.enums.QuestionSubmitStatusEnum;
import com.longoj.top.domain.repository.QuestionSubmitRepository;
import com.longoj.top.infrastructure.mapper.QuestionSubmitMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class QuestionSubmitRepositoryImpl implements QuestionSubmitRepository {

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    @Override
    public List<QuestionSubmit> list(Long userId, QuestionPassStatusEnum questionPassStatus) {
        LambdaQueryWrapper<QuestionSubmit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuestionSubmit::getUserId, userId);
        queryWrapper.eq(QuestionSubmit::getIsDelete, Boolean.FALSE);
        if (questionPassStatus != null) {
            queryWrapper.eq(QuestionSubmit::getPassStatus, questionPassStatus.getEnCode());
        }
        return questionSubmitMapper.selectList(queryWrapper);
    }

    @Override
    public Page<QuestionSubmit> page(String language, Integer questionId, Long userId, QuestionSubmitStatusEnum status, int current, int pageSize) {
        LambdaQueryWrapper<QuestionSubmit> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuestionSubmit::getUserId, userId);
        queryWrapper.eq(QuestionSubmit::getIsDelete, Boolean.FALSE);
        queryWrapper.eq(QuestionSubmit::getLanguage, language);
        queryWrapper.eq(QuestionSubmit::getQuestionId, questionId);
        queryWrapper.eq(QuestionSubmit::getStatus, status.getCode());

        return questionSubmitMapper.selectPage(new Page<>(current, pageSize), queryWrapper);
    }
}
