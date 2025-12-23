package com.longoj.top.infrastructure.scheduler;

import cn.hutool.json.JSONUtil;
import com.longoj.top.infrastructure.mq.publisher.JudgeServicePublisher;
import com.longoj.top.domain.entity.LocalMessage;
import com.longoj.top.domain.entity.QuestionSubmit;
import com.longoj.top.domain.entity.enums.MQMessageStatusEnum;
import com.longoj.top.domain.service.LocalMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class JudgeScheduledTask {

    private final Integer MAX_RETRY_COUNT = 3; // 最大重试次数

    @Resource
    private LocalMessageService localMessageService;

    @Resource
    private JudgeServicePublisher judgeServicePublisher;

    /**
     * 每10分钟扫描一次本地消息表，重新发送失败或待处理的任务
     */
    @Scheduled(cron = "0 */10 * * * ?") // 每10分钟执行一次
    public void resendFailedMessages() {
        log.info("开始执行定时任务：扫描并重发本地消息表中的失败/待处理消息");

        List<LocalMessage> messagesToResend = localMessageService.lambdaQuery()
                .eq(LocalMessage::getStatus, MQMessageStatusEnum.FAILED)
                .list();

        if (CollectionUtils.isEmpty(messagesToResend)) {
            log.info("没有需要重发的消息");
            return;
        }

        log.info("发现 {} 条需要重发的消息", messagesToResend.size());

        for (LocalMessage localMessage : messagesToResend) {
            try {
                // 增加重试次数
                int currentRetryCount = localMessage.getRetryCount() == null ? 0 : localMessage.getRetryCount();
                if (currentRetryCount >= MAX_RETRY_COUNT) {
                    log.warn("消息重试次数已达上限，ID: {}", localMessage.getMessageId());
                    // 可以选择将状态更新为最终失败
                    localMessageService.lambdaUpdate()
                            .set(LocalMessage::getStatus, MQMessageStatusEnum.FINAL_FAILED)
                            .set(LocalMessage::getErrorCause, "Max retry count reached")
                            .eq(LocalMessage::getMessageId, localMessage.getMessageId())
                            .update();
                    continue;
                }

                localMessage.setRetryCount(currentRetryCount + 1);
                // 更新状态为待发送，并增加重试次数
                localMessageService.lambdaUpdate()
                        .set(LocalMessage::getStatus, MQMessageStatusEnum.PENDING)
                        .set(LocalMessage::getRetryCount, localMessage.getRetryCount())
                        .eq(LocalMessage::getMessageId, localMessage.getMessageId())
                        .update();

                QuestionSubmit questionSubmit = JSONUtil.toBean(localMessage.getPayload(), QuestionSubmit.class);
                judgeServicePublisher.retrySendJudgeMessage(localMessage.getPayload(), questionSubmit.getId());
                log.info("消息重发请求已提交，ID: {}", localMessage.getMessageId());
            } catch (Exception e) {
                log.error("重发消息失败，ID: {}, 错误: {}", localMessage.getMessageId(), e.getMessage(), e);
                // 如果重发仍然失败，可以考虑更新状态为最终失败，或根据重试次数决定
                localMessageService.lambdaUpdate()
                        .set(LocalMessage::getStatus, MQMessageStatusEnum.FINAL_FAILED) // 或者根据重试次数判断
                        .set(LocalMessage::getErrorCause, "Resend failed: " + e.getMessage())
                        .eq(LocalMessage::getMessageId, localMessage.getMessageId())
                        .update();
            }
        }
        log.info("定时任务执行完毕：扫描并重发本地消息表中的失败消息");
    }
}
