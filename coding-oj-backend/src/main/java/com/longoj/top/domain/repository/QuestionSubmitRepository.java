package com.longoj.top.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.domain.entity.QuestionSubmit;
import com.longoj.top.domain.entity.enums.QuestionPassStatusEnum;
import com.longoj.top.domain.entity.enums.QuestionSubmitStatusEnum;

import java.util.List;

public interface QuestionSubmitRepository {
    /**
     * 列表查询提交记录
     */
    List<QuestionSubmit> list(Long userId, QuestionPassStatusEnum questionPassStatus) ;

    /**
     * 分页查询提交记录
     */
    Page<QuestionSubmit> page(String language, Integer questionId, QuestionSubmitStatusEnum status, String sortField, String sortOrder, int current, int pageSize);

    /**
     * 分页查询提交记录
     */
    Page<QuestionSubmit> page(String language, Integer questionId, Long userId, QuestionSubmitStatusEnum status, int current, int pageSize);

}
