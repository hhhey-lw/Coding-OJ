package com.longoj.top.domain.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longoj.top.domain.entity.enums.QuestionPassStatusEnum;
import com.longoj.top.domain.entity.enums.UserRoleEnum;
import com.longoj.top.domain.repository.QuestionSubmitRepository;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.utils.PageUtil;
import com.longoj.top.infrastructure.utils.RedisKeyUtil;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.infrastructure.mq.publisher.JudgeServicePublisher;
import com.longoj.top.controller.dto.question.QuestionSubmitAddRequest;
import com.longoj.top.controller.dto.question.QuestionSubmitQueryRequest;
import com.longoj.top.domain.entity.Question;
import com.longoj.top.domain.entity.QuestionSubmit;
import com.longoj.top.domain.entity.User;
import com.longoj.top.domain.entity.enums.QuestionSubmitLanguageEnum;
import com.longoj.top.domain.entity.enums.QuestionSubmitStatusEnum;
import com.longoj.top.controller.dto.question.QuestionSubmitVO;
import com.longoj.top.controller.dto.question.QuestionVO;
import com.longoj.top.controller.dto.user.UserSubmitInfoVO;
import com.longoj.top.domain.service.QuestionService;
import com.longoj.top.domain.service.QuestionSubmitService;
import com.longoj.top.infrastructure.mapper.QuestionSubmitMapper;
import com.longoj.top.domain.service.UserService;
import com.longoj.top.infrastructure.utils.UserContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * question_submit(题目提交记录)
 *
 * @author 韦龙
 * @createDate 2025-05-15 00:13:26
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit> implements QuestionSubmitService {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private QuestionSubmitRepository questionSubmitRepository;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JudgeServicePublisher judgeServicePublisher;

    @Override
    @Transactional
    public Long submit(QuestionSubmitAddRequest questionSubmitAddRequest) {
        // 1. 校验参数;
        // 1.1. 提交语言
        if (!QuestionSubmitLanguageEnum.isExist(questionSubmitAddRequest.getLanguage())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        // 1.2. 提交题目
        Question question = questionService.getById(questionSubmitAddRequest.getQuestionId());
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 1.3. 窗口防抖
        // TODO

        // 2. 设置初始状态
        User loginUser = UserContext.getUser();
        QuestionSubmit submit = QuestionSubmit.toEntity(questionSubmitAddRequest, question, loginUser);

        // 3. 保存判题信息
        boolean save = save(submit);
        if (!save) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "插入失败");
        }

        // 4. 异步执行服务
        QuestionSubmit questionSubmit = getById(submit.getId());
        judgeServicePublisher.sendDoJudgeMessage(questionSubmit);

        // mybatis 自动回填
        return questionSubmit.getId();
    }

    @Override
    public boolean isExecuted(Long id) {
        QuestionSubmit questionSubmit = baseMapper.selectById(id);
        if (questionSubmit == null) {
            return false;
        }
        Integer status = questionSubmit.getStatus();
        return !(status.intValue() == QuestionSubmitStatusEnum.WAITING.getCode().intValue());
    }

    /* 从Redis中查询Top10通过数最多的用户 */
    @Override
    public List<UserSubmitInfoVO> getTopPassedQuestionUserList(int topNumber) {
        if (topNumber <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Top number must be greater than 0");
        }

        String topPassedNumberKey = RedisKeyUtil.getTopPassedNumberKey();
        String res = stringRedisTemplate.opsForValue().get(topPassedNumberKey);

        if (StrUtil.isBlank(res)) {
            return Collections.emptyList();
        }

        return JSONUtil.toList(res, UserSubmitInfoVO.class);
    }

    @Override
    public List<Long> listPassedQuestionId(Long userId) {
        List<QuestionSubmit> submits = questionSubmitRepository.list(userId, QuestionPassStatusEnum.ACCEPTED);
        if (CollectionUtil.isEmpty(submits)) {
            return Collections.emptyList();
        }
        return submits.stream().map(QuestionSubmit::getId).collect(Collectors.toList());
    }

    @Override
    public Page<QuestionSubmitVO> pageQuery(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        Page<QuestionSubmit> questionSubmitPage = questionSubmitRepository.page(questionSubmitQueryRequest.getLanguage().getCode(), questionSubmitQueryRequest.getQuestionId(), questionSubmitQueryRequest.getStatus(),
                questionSubmitQueryRequest.getCurrent(), questionSubmitQueryRequest.getPageSize());
        return PageUtil.convertToVO(questionSubmitPage, questionSubmit -> getQuestionSubmitVO(questionSubmit, UserContext.getUser()));
    }

    @Override
    public Page<QuestionSubmitVO> pageMy(Integer questionId, QuestionSubmitStatusEnum status, QuestionSubmitLanguageEnum language, int current, int pageSize) {
        User loginUser = UserContext.getUser();
        Page<QuestionSubmit> questionSubmitPage = questionSubmitRepository.page(language.getCode(), questionId,
                loginUser.getId(), status,
                current, pageSize);
        return PageUtil.convertToVO(questionSubmitPage, questionSubmit -> getQuestionSubmitVO(questionSubmit, UserContext.getUser()));
    }

    @Override
    public void updatePassStatus(Long id, QuestionPassStatusEnum passStatusEnum) {
        LambdaUpdateWrapper<QuestionSubmit> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(QuestionSubmit::getPassStatus, passStatusEnum.getEnCode());
        updateWrapper.eq(QuestionSubmit::getId, id);
        baseMapper.update(null, updateWrapper);
    }

    /**
     * 脱敏处理并转换为 VO 对象
     */
    private QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        // 脱敏处理
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.convertToVO(questionSubmit);

        // 仅本人和管理员 可以查看提交的代码
        if (loginUser == null || !UserRoleEnum.ADMIN.getCode().equals(loginUser.getUserRole()) || !loginUser.getId().equals(questionSubmit.getUserId())) {
            questionSubmitVO.setCode("");
        }
        questionSubmitVO.setUserVO(userService.getUserVOByUserId(questionSubmit.getUserId()));
        Question question = questionService.getById(questionSubmit.getQuestionId());
        // 隐藏题目答案和代码模版
        question.setAnswer(null);
        question.setSourceCode(null);
        questionSubmitVO.setQuestionVO(QuestionVO.convertToVo(question));
        return questionSubmitVO;
    }
}
