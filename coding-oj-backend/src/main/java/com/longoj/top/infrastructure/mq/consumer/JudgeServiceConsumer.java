package com.longoj.top.infrastructure.mq.consumer;

import cn.hutool.json.JSONUtil;
import com.longoj.top.domain.service.LocalMessageService;
import com.longoj.top.infrastructure.config.JudgeMQConfig;
import com.longoj.top.domain.service.codesandbox.JudgeService;
import com.longoj.top.domain.entity.QuestionSubmit;
import com.longoj.top.domain.service.QuestionSubmitService;
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
            QuestionSubmit questionSubmit = JSONUtil.toBean(content, QuestionSubmit.class);
            // 解析失败
            if (questionSubmit == null) {
                log.error("消息内容解析失败: {}", content);
                // 处理解析失败的情况 - 不重新入队
                channel.basicNack(deliveryTag, false, false);
                return;
            }
            // 幂等性
            if (questionSubmitService.isExecuted(questionSubmit.getId())) {
                log.info("提交记录已经执行完成: 跳过 {}", questionSubmit.getId());
                // 手动确认消息
                channel.basicAck(deliveryTag, false);
                return;
            }
            judgeService.doJudge(questionSubmit);
            // 成功处理消息，手动确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("消息处理失败: {}, 错误原因: {}", content, e.getMessage(), e);
            // 拒绝消息，并不再重新入队 (进入死信队列)
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(queues = JudgeMQConfig.DLQ_NAME)
    public void judgeMQDlxListener(String content, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            log.info("死信交换机收到: {}", content);
            QuestionSubmit questionSubmit = JSONUtil.toBean(content, QuestionSubmit.class);
            // 幂等性
            if (questionSubmitService.isExecuted(questionSubmit.getId())) {
                log.info("提交记录已经执行完成, 跳过: {}", questionSubmit.getId());
                // 手动确认消息
                channel.basicAck(deliveryTag, false);
                return;
            }
            judgeService.setJudgeInfoFailed(questionSubmit.getId());

            // 成功处理消息，手动确认
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("消息处理失败: {}, 错误原因: {}", content, e.getMessage(), e);
            // 拒绝消息，并不再重新入队
            channel.basicNack(deliveryTag, false, false);
        }
    }

}
