package com.longoj.top.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longoj.top.model.dto.questionsubmit.UserPassedCountDTO;
import com.longoj.top.model.entity.QuestionPassed;
import com.longoj.top.service.QuestionPassedService;
import com.longoj.top.mapper.QuestionPassedMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 韦龙
* @description 针对表【question_passed(用户通过题目记录表)】的数据库操作Service实现
* @createDate 2025-06-18 19:27:18
*/
@Service
public class QuestionPassedServiceImpl extends ServiceImpl<QuestionPassedMapper, QuestionPassed>
    implements QuestionPassedService{

    @Override
    public boolean addUserPassedQuestion(Long questionId, Long userId) {
        // 1. 检查记录是否已存在
        long count = this.lambdaQuery()
                .eq(QuestionPassed::getUserId, userId)
                .eq(QuestionPassed::getQuestionId, questionId)
                .count();

        // 2. 如果记录已存在，直接返回 true，表示操作成功（幂等性）
        if (count > 0) {
            return true;
        }

        // 3. 如果记录不存在，则插入新记录
        QuestionPassed questionPassed = new QuestionPassed();
        questionPassed.setQuestionId(questionId);
        questionPassed.setUserId(userId);
        return this.save(questionPassed);
    }

    // 查询用户通过的所有题目ID列表
    @Override
    public List<Long> getQuestionIdsByUserId(Long userId) {
        if (userId == null) {
            return java.util.Collections.emptyList();
        }
        // 使用 lambdaQuery 查询
        return this.lambdaQuery()
                .select(QuestionPassed::getQuestionId)
                .eq(QuestionPassed::getUserId, userId)
                .list()
                .stream()
                .map(QuestionPassed::getQuestionId)
                .collect(Collectors.toList());
    }
}




