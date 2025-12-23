package com.longoj.top.domain.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longoj.top.domain.repository.QuestionRepository;
import com.longoj.top.domain.repository.QuestionSubmitRepository;
import com.longoj.top.domain.service.QuestionSubmitService;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.utils.PageUtil;
import com.longoj.top.infrastructure.utils.RedisKeyUtil;
import com.longoj.top.controller.dto.question.QuestionAddRequest;
import com.longoj.top.controller.dto.question.QuestionUpdateRequest;
import com.longoj.top.domain.entity.Question;
import com.longoj.top.domain.entity.User;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.infrastructure.utils.ThrowUtils;
import com.longoj.top.controller.dto.question.QuestionVO;
import com.longoj.top.controller.dto.user.UserVO;
import com.longoj.top.domain.service.QuestionService;
import com.longoj.top.infrastructure.mapper.QuestionMapper;
import com.longoj.top.domain.service.QuestionTagService;
import com.longoj.top.domain.service.UserService;
import com.longoj.top.infrastructure.utils.UserContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 题目服务
 *
 * @author 韦龙
 * @createDate 2025-05-15 00:13:26
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserService userService;

    @Resource
    private QuestionTagService questionTagService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private QuestionRepository questionRepository;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Long addQuestion(QuestionAddRequest questionAddRequest) {
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 构建题目实体并校验
        Question question = QuestionAddRequest.buildEntity(questionAddRequest);
        validQuestion(question);

        // 2. 保存题目
        boolean result = save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        // 3. 题目标签关联
        if (CollectionUtil.isNotEmpty(questionAddRequest.getTags())) {
            questionTagService.setTagsByQuestionId(question.getId(), questionAddRequest.getTags());
        }

        return question.getId();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean deleteQuestion(Long id) {
        // 1. 删除题目
        removeById(id);

        // 2. 删除题目标签关联
        questionTagService.removeByQuestionId(id);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Boolean updateQuestion(QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = QuestionUpdateRequest.toEntity(questionUpdateRequest);
        // 参数校验
        validQuestion(question);

        // 更新题目
        boolean update = updateById(question);
        ThrowUtils.throwIf(!update, ErrorCode.UPDATE_ERROR);

        // 更新标签
        if (CollectionUtil.isNotEmpty(questionUpdateRequest.getTags())) {
            Boolean tagUpdate = questionTagService.delAndSetTagsByQuestionId(question.getId(), questionUpdateRequest.getTags());
            ThrowUtils.throwIf(!tagUpdate, ErrorCode.UPDATE_ERROR);
        }

        return true;
    }

    @Override
    public boolean isPassed(Long questionId, Long userId) {
        // 1. 参数校验
        if (questionId == null || userId == null || userId <= 0) {
            return false;
        }
        String userPassedQuestionKey = RedisKeyUtil.getUserPassedQuestionKey(userId);

        // 2. 尝试从缓存获取
        Set<String> passedSet = stringRedisTemplate.opsForSet().members(userPassedQuestionKey);
        if (CollectionUtil.isNotEmpty(passedSet)) {
            return passedSet.contains(questionId.toString());
        }

        // 3. 重数据库查询并缓存
        List<Long> passedQuestionIds = questionSubmitService.listPassedQuestionId(userId);
        if (CollectionUtil.isEmpty(passedQuestionIds)) {
            return false;
        }

        String[] idArray = passedQuestionIds.stream().map(String::valueOf).toArray(String[]::new);
        stringRedisTemplate.opsForSet().add(userPassedQuestionKey, idArray);
        stringRedisTemplate.expire(userPassedQuestionKey, 10, TimeUnit.MINUTES);

        return passedQuestionIds.contains(questionId);
    }

    @Override
    public int updateQuestionSubmitNum(Long questionId) {
        return baseMapper.updateSubmitNum(questionId);
    }

    @Override
    public int updateQuestionAcceptedNum(Long questionId) {
        return baseMapper.updateAcceptedNum(questionId);
    }

    @Override
    public QuestionVO getQuestionVOById(Long id) {
        // 1. 查询题目信息
        Question question = getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
        QuestionVO questionVO = QuestionVO.convertToVo(question);

        // 2. 查询用户信息
        User user = userService.getById(question.getUserId());
        questionVO.setUserVO(UserVO.toVO(user));

        // 3. 设置通过状态
        questionVO.setPassed(isPassed(question.getId(), user.getId()));

        return questionVO;
    }

    @Override
    public Page<QuestionVO> pageVO(String searchKey, Integer difficulty, List<String> tags, Long userId, int current, int pageSize) {
        Page<Question> questionPage = questionRepository.page(searchKey, difficulty, tags, userId, current, pageSize);
        Page<QuestionVO> questionVOPage = PageUtil.convertToVO(questionPage, QuestionVO::convertToVo);
        User currentUser = UserContext.getUser();
        questionVOPage.getRecords().forEach(questionVO -> {
            questionVO.setPassed(isPassed(questionVO.getId(), currentUser.getId()));
        });
        return questionVOPage;
    }

    @Override
    public Page<Question> page(String searchKey, Integer difficulty, List<String> tags, Long userId, int current, int pageSize) {
        return questionRepository.page(searchKey, difficulty, tags, userId, current, pageSize);
    }

    @Override
    public Boolean update(QuestionUpdateRequest questionUpdateRequest) {
        Question question = QuestionUpdateRequest.toEntity(questionUpdateRequest);
        return updateById(question);
    }

    /**
     * 校验题目参数
     */
    private void validQuestion(Question question) {
        if (question == null) {
            return;
        }

        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String answer = question.getAnswer();
        String judgeCase = question.getJudgeCase();
        String judgeConfig = question.getJudgeConfig();

        ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);

        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
        if (StringUtils.isNotBlank(answer) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "答案过长");
        }
        if (StringUtils.isNotBlank(judgeCase) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题用例过长");
        }
        if (StringUtils.isNotBlank(judgeConfig) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题配置过长");
        }

    }

}
