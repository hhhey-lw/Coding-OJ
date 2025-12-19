package com.longoj.top.job.cycle;

import cn.hutool.json.JSONUtil;
import com.longoj.top.common.RedisKeyUtil;
import com.longoj.top.mapper.UserSubmitSummaryMapper;
import com.longoj.top.model.dto.questionsubmit.UserPassedCountDTO;
import com.longoj.top.model.entity.User;
import com.longoj.top.model.vo.UserSubmitInfoVO;
import com.longoj.top.model.vo.UserVO;
import com.longoj.top.service.QuestionPassedService;
import com.longoj.top.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class QuestionPassedScheduledTask {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserSubmitSummaryMapper userSubmitSummaryMapper;

    @Resource
    private UserService userService;

    @Scheduled(cron = "0 0/10 * * * ?")
    public void updateTopPassedQuestionUserList() {
        String key = RedisKeyUtil.getTopPassedNumberKey();

        // 1. 从数据库查询 TopN 用户的 ID 列表（已排序）
        List<UserPassedCountDTO> userPassedCountDTOList = userSubmitSummaryMapper.selectUserPassedCountsByTopNumber(RedisKeyUtil.TOP_PASSED_NUMBER);
        if (userPassedCountDTOList.isEmpty()) {
            log.info("没有用户通过题目数量达到前 {} 名", RedisKeyUtil.TOP_PASSED_NUMBER);
            stringRedisTemplate.opsForValue().set(key, "");
            return;
        }
        // 2. 查询用户信息
        List<Long> userIds = userPassedCountDTOList.stream().map(UserPassedCountDTO::getUserId)
                .collect(Collectors.toList());
        List<User> userList = userService.listByIds(userIds);

        // 3. 关键：保持排序。listByIds 不保证顺序，需要根据原始 userIds 重新排序
        Map<Long, User> userMap = userList.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 4. 整理数据 并存入 Redis
        ArrayList<UserSubmitInfoVO> userSubmitInfoVOS = new ArrayList<>(userIds.size());
        for (UserPassedCountDTO userPassedCountDTO : userPassedCountDTOList) {
            Long userId = userPassedCountDTO.getUserId();
            Integer passedCount = userPassedCountDTO.getPassedCount();
            Integer submitCount = userPassedCountDTO.getSubmitCount();
            User user = userMap.get(userId);

            UserSubmitInfoVO userSubmitInfoVO = new UserSubmitInfoVO();
            userSubmitInfoVO.setTotalSubmitNumber(submitCount);
            userSubmitInfoVO.setPassedQuestionNumber(passedCount);
            userSubmitInfoVO.setUserName(user.getUserName());
            userSubmitInfoVO.setUserAvatar(user.getUserAvatar());
            userSubmitInfoVO.setId(userId);
            userSubmitInfoVO.setUserProfile(user.getUserProfile());
            userSubmitInfoVO.setCreateTime(user.getCreateTime());
            userSubmitInfoVO.setUserRole(user.getUserRole());

            userSubmitInfoVOS.add(userSubmitInfoVO);
        }

        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(userSubmitInfoVOS));
        log.info("更新通过题目数量前 {} 名用户列表成功，用户数量: {}", RedisKeyUtil.TOP_PASSED_NUMBER, userSubmitInfoVOS.size());
    }

    @PostConstruct
    public void init() {
        // 初始化时执行一次，确保定时任务开始前数据已更新
        updateTopPassedQuestionUserList();
    }

}
