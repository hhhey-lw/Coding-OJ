package com.longoj.top.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longoj.top.common.ErrorCode;
import com.longoj.top.common.RedisKeyUtil;
import com.longoj.top.constant.CommonConstant;
import com.longoj.top.exception.BusinessException;
import com.longoj.top.job.publisher.JudgeServicePublisher;
import com.longoj.top.model.dto.questionsubmit.JudgeInfo;
import com.longoj.top.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.longoj.top.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.longoj.top.model.entity.Question;
import com.longoj.top.model.entity.QuestionSubmit;
import com.longoj.top.model.entity.User;
import com.longoj.top.model.enums.QuestionSubmitLanguageEnum;
import com.longoj.top.model.enums.QuestionSubmitStatusEnum;
import com.longoj.top.model.vo.QuestionSubmitVO;
import com.longoj.top.model.vo.QuestionVO;
import com.longoj.top.model.vo.UserSubmitInfoVO;
import com.longoj.top.model.vo.UserVO;
import com.longoj.top.service.QuestionService;
import com.longoj.top.service.QuestionSubmitService;
import com.longoj.top.mapper.QuestionSubmitMapper;
import com.longoj.top.service.UserService;
import com.longoj.top.utils.SqlUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 韦龙
* @description 针对表【question_submit(题目提交)】的数据库操作Service实现
* @createDate 2025-05-15 00:13:26
*/
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
implements QuestionSubmitService{

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    // @Resource
    // private JudgeExecutor judgeExecutor;
    @Resource
    private JudgeServicePublisher judgeServicePublisher;

    @Override
    @Transactional
    public Long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
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
        QuestionSubmit submit = QuestionSubmit.builder()
                .questionId(question.getId())
                .code(questionSubmitAddRequest.getCode())
                .language(questionSubmitAddRequest.getLanguage())
                .judgeInfo(JSONUtil.toJsonStr(JudgeInfo.builder()
                                .memory(null)
                                .message(QuestionSubmitStatusEnum.WAITING.getValue())
                                .time(null)
                        .build()))
                .userId(loginUser.getId())
                .status(QuestionSubmitStatusEnum.WAITING.getStatus())
                .build();

        // 3. 保存判题信息
        boolean save = save(submit);
        if (!save) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "插入失败");
        }

        // 4. 异步执行服务
        // 调用代码沙箱！
        QuestionSubmit questionSubmit = getById(submit.getId());
        // 放队列中，异步执行
        // judgeExecutor.pushTaskQueue(questionSubmit);
        // CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
        //     judgeService.doJudge(questionSubmit);
        // });
        // 线程池
        // JudgeInfo judgeInfo = judgeService.doJudge(questionSubmit);
        // 消息队列
        judgeServicePublisher.sendDoJudgeMessage(questionSubmit);

        // mybatis 自动回填
        return questionSubmit.getId();
    }

    @Override
    public Wrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }

        // 获取请求
        String language = questionSubmitQueryRequest.getLanguage();
        Long userId = questionSubmitQueryRequest.getUserId();
        Integer status = questionSubmitQueryRequest.getStatus();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 构造查询表达包装
        // 拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.eq("is_delete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> page, User loginUser) {
        List<QuestionSubmit> pageRecords = page.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (CollectionUtil.isEmpty(pageRecords)) {
            return questionSubmitVOPage;
        }

        List<QuestionSubmitVO> submitVOList = pageRecords.stream().map(
                questionSubmit -> getQuestionSubmitVO(questionSubmit, loginUser)
        ).collect(Collectors.toList());

        questionSubmitVOPage.setRecords(submitVOList);
        return questionSubmitVOPage;
    }

    @Override
    public boolean isQuestionSubmitExecuted(Long id) {
        QuestionSubmit questionSubmit = baseMapper.selectById(id);
        if (questionSubmit == null) {
            return true;
        }
        Integer status = questionSubmit.getStatus();
        return !(status.intValue() == QuestionSubmitStatusEnum.WAITING.getStatus().intValue());
    }

    /* 从Redis中查询Top10通过数最多的用户 */
    @Override
    public List<UserSubmitInfoVO> getTopPassedQuestionUserList(int topNumber) {
        if (topNumber <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Top number must be greater than 0");
        }

        String topPassedNumberKey = RedisKeyUtil.getTopPassedNumberKey();
        String res = stringRedisTemplate.opsForValue().get(topPassedNumberKey);

        if (StrUtil.isBlank(res)){
            return Collections.emptyList();
        }

        return JSONUtil.toList(res, UserSubmitInfoVO.class);
    }

    private QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        // 脱敏处理
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVO(questionSubmit);

        // 仅本人和管理员 可以查看提交的代码
        if (loginUser == null || !userService.isAdmin(loginUser) || !loginUser.getId().equals(questionSubmit.getUserId())) {
            questionSubmitVO.setCode("");
        }
        // questionSubmitVO.setUserVO(BeanUtil.copyProperties(loginUser, UserVO.class));

        questionSubmitVO.setUserVO(userService.getUserVOByUserId(questionSubmit.getUserId()));
        Question question = questionService.getById(questionSubmit.getQuestionId());
        questionSubmitVO.setQuestionVO(QuestionVO.objToVo(question));
        return questionSubmitVO;
    }
}
