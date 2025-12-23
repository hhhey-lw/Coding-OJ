package com.longoj.top.domain.repository.impl;

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

    @Override
    public Page<Question> page(String searchKey, Integer difficulty, List<Long> tagIds, int current, int pageSize) {
        return questionMapper.pageByTagIds(new Page<>(current, pageSize), searchKey, difficulty, tagIds);
    }
}
