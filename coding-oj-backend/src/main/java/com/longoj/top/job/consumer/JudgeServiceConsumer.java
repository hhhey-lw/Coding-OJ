package com.longoj.top.job.consumer;

import cn.hutool.json.JSONUtil;
import com.longoj.top.config.JudgeMQConfig;
import com.longoj.top.judge.codesandbox.service.JudgeService;
import com.longoj.top.model.entity.QuestionSubmit;
import com.longoj.top.service.QuestionSubmitService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Slf4j
@Service
public class JudgeServiceConsumer {

    @Resource
    private JudgeService judgeService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @RabbitListener(queues = JudgeMQConfig.QUEUE_NAME)
    public void judgeMQListener(String content, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("Received message: {}", content);
            // 在这里添加你的业务处理逻辑
            QuestionSubmit questionSubmit = JSONUtil.toBean(content, QuestionSubmit.class);
            // 解析失败
            if (questionSubmit == null) {
                log.error("Failed to parse QuestionSubmit from content: {}", content);
                // 处理解析失败的情况
                channel.basicNack(deliveryTag, false, false); // 不重新入队
                return;
            }
            // 幂等性
            if (questionSubmitService.isQuestionSubmitExecuted(questionSubmit.getId())) {
                log.info("Question submit already exists, skipping: {}", questionSubmit.getId());
                channel.basicAck(deliveryTag, false); // 手动确认消息
                return;
            }
            judgeService.doJudge(questionSubmit);
            // 成功处理消息，手动确认
            channel.basicAck(deliveryTag, false); // false 表示只确认当前这条消息
        } catch (Exception e) {
            log.error("Error processing message: {}, error: {}", content, e.getMessage(), e);
            // 根据业务需求决定如何处理异常
            // 选项1: 拒绝消息，并让其重新入队 (如果希望重试)
            // channel.basicNack(deliveryTag, false, true);
            // 选项2: 拒绝消息，并不再重新入队 (如果消息有问题或不应重试，可能会进入死信队列)
            channel.basicNack(deliveryTag, false, false);
            // 或者 channel.basicReject(deliveryTag, false);
        }
    }

    @RabbitListener(queues = JudgeMQConfig.DLQ_NAME)
    public void judgeMQDlxListener(String content, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("judgeDlxQueue Received message: {}", content);
            // 在这里添加你的业务处理逻辑
            QuestionSubmit questionSubmit = JSONUtil.toBean(content, QuestionSubmit.class);
            // 幂等性
            if (questionSubmitService.isQuestionSubmitExecuted(questionSubmit.getId())) {
                log.info("Question submit already exists, skipping: {}", questionSubmit.getId());
                channel.basicAck(deliveryTag, false); // 手动确认消息
                return;
            }
            judgeService.setJudgeInfoFailed(questionSubmit.getId());

            // 成功处理消息，手动确认
            channel.basicAck(deliveryTag, false); // false 表示只确认当前这条消息
        } catch (Exception e) {
            log.error("Error processing message: {}, error: {}", content, e.getMessage(), e);
            // 根据业务需求决定如何处理异常
            // 选项1: 拒绝消息，并让其重新入队 (如果希望重试)
            // channel.basicNack(deliveryTag, false, true);
            // 选项2: 拒绝消息，并不再重新入队 (如果消息有问题或不应重试，可能会进入死信队列)
            channel.basicNack(deliveryTag, false, false);
            // 或者 channel.basicReject(deliveryTag, false);
        }
    }

}
