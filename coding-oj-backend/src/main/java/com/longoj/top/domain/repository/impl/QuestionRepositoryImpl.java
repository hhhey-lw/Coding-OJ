package com.longoj.top.domain.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.domain.entity.Question;
import com.longoj.top.domain.repository.QuestionRepository;
import com.longoj.top.infrastructure.mapper.QuestionMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class QuestionRepositoryImpl implements QuestionRepository {

    @Resource
    private QuestionMapper questionMapper;

    // TODO 标签
    @Override
    public Page<Question> page(String searchKey, Integer difficulty, List<String> tags, Long userId, int current, int pageSize) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Question::getDifficulty, difficulty);
        queryWrapper.eq(Question::getUserId, userId);
        queryWrapper.like(Question::getTitle, searchKey);
        return questionMapper.selectPage(new Page<>(current, pageSize), queryWrapper);
    }
}
