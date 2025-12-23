package com.longoj.top.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.domain.entity.Question;

import java.util.List;

public interface QuestionRepository {
    /**
     * 分页查询
     */
    Page<Question> page(String searchKey, Integer difficulty, List<String> tags, Long userId, int current, int pageSize);

}
