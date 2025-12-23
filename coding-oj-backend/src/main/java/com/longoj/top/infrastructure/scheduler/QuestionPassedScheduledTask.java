package com.longoj.top.infrastructure.scheduler;

import cn.hutool.json.JSONUtil;
import com.longoj.top.infrastructure.utils.RedisKeyUtil;
import com.longoj.top.infrastructure.mapper.QuestionSubmitMapper;
import com.longoj.top.domain.entity.dto.UserPassedCountDTO;
import com.longoj.top.domain.entity.User;
import com.longoj.top.controller.dto.user.UserSubmitInfoVO;
import com.longoj.top.domain.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class QuestionPassedScheduledTask {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    @Resource
    private UserService userService;

    // 内存中缓存排行榜数据（Redis 不可用时的降级缓存）
    private volatile List<UserSubmitInfoVO> cachedTopUsers = new ArrayList<>();

    /**
     * 检查 Redis 是否可用
     */
    private boolean isRedisAvailable() {
        try {
            if (stringRedisTemplate.getConnectionFactory() == null) {
                return false;
            }
            stringRedisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            log.warn("Redis 不可用: {}", e.getMessage());
            return false;
        }
    }

    @Scheduled(cron = "0 0/10 * * * ?")
    public void updateTopPassedQuestionUserList() {
        // 1. 从 question_submit 表直接统计 TopN 用户（已排序）
        List<UserPassedCountDTO> userPassedCountDTOList = questionSubmitMapper.selectUserPassedCountsByTopNumber(RedisKeyUtil.TOP_PASSED_NUMBER);
        if (userPassedCountDTOList.isEmpty()) {
            log.info("没有用户通过题目数量达到前 {} 名", RedisKeyUtil.TOP_PASSED_NUMBER);
            cachedTopUsers = new ArrayList<>();
            if (isRedisAvailable()) {
                try {
                    stringRedisTemplate.opsForValue().set(RedisKeyUtil.getTopPassedNumberKey(), "");
                } catch (Exception e) {
                    log.warn("Redis 写入失败: {}", e.getMessage());
                }
            }
            return;
        }

        // 2. 查询用户信息
        List<Long> userIds = userPassedCountDTOList.stream().map(UserPassedCountDTO::getUserId)
                .collect(Collectors.toList());
        List<User> userList = userService.listByIds(userIds);

        // 3. 保持排序
        Map<Long, User> userMap = userList.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        // 4. 整理数据
        ArrayList<UserSubmitInfoVO> userSubmitInfoVOS = new ArrayList<>(userIds.size());
        for (UserPassedCountDTO userPassedCountDTO : userPassedCountDTOList) {
            Long userId = userPassedCountDTO.getUserId();
            Integer passedCount = userPassedCountDTO.getPassedCount();
            Integer submitCount = userPassedCountDTO.getSubmitCount();
            User user = userMap.get(userId);
            if (user == null) continue;

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

        // 5. 保存到内存缓存（降级用）
        cachedTopUsers = userSubmitInfoVOS;

        // 6. 尝试写入 Redis
        if (isRedisAvailable()) {
            try {
                stringRedisTemplate.opsForValue().set(RedisKeyUtil.getTopPassedNumberKey(), JSONUtil.toJsonStr(userSubmitInfoVOS));
                log.info("更新通过题目数量前 {} 名用户列表成功，用户数量: {}", RedisKeyUtil.TOP_PASSED_NUMBER, userSubmitInfoVOS.size());
            } catch (Exception e) {
                log.warn("Redis 写入失败，数据已保存到内存缓存: {}", e.getMessage());
            }
        } else {
            log.info("Redis 不可用，排行榜数据已保存到内存缓存，用户数量: {}", userSubmitInfoVOS.size());
        }
    }

    /**
     * 获取排行榜数据（供其他服务调用，支持降级）
     */
    public List<UserSubmitInfoVO> getTopPassedUsers() {
        // 优先从 Redis 获取
        if (isRedisAvailable()) {
            try {
                String json = stringRedisTemplate.opsForValue().get(RedisKeyUtil.getTopPassedNumberKey());
                if (json != null && !json.isEmpty()) {
                    return JSONUtil.toList(json, UserSubmitInfoVO.class);
                }
            } catch (Exception e) {
                log.warn("Redis 读取失败，使用内存缓存: {}", e.getMessage());
            }
        }
        // 降级：返回内存缓存
        return cachedTopUsers;
    }

    @PostConstruct
    public void init() {
        // 初始化时执行一次，确保定时任务开始前数据已更新
        updateTopPassedQuestionUserList();
    }

}
