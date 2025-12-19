package com.longoj.top.service;

import com.longoj.top.config.JudgeMQConfig;
import com.longoj.top.model.entity.MqLocalMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@Slf4j
@SpringBootTest
public class RabbitMQTest implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private MqLocalMessageService mqLocalMessageService;

    @Test
    public void testRabbitMQConnection() throws InterruptedException {
        // 这里可以添加测试代码来验证 RabbitMQ 的连接和功能
        rabbitTemplate.setConfirmCallback(this); // 设置 ConfirmCallback
        rabbitTemplate.setReturnsCallback(this); // 设置 ReturnsCallback

        // 例如，发送一条消息到队列，或者从队列中接收消息
        for (int i = 1; i <= 1; i++) {
            String message = "Test message " + i;

            // 先存本地方法表
            // MqLocalMessage mqLocalMessage = new MqLocalMessage();
            // mqLocalMessage.setMessageId("test2-message-id-"+i);
            // mqLocalMessage.setExchangeName("judgeExchange");
            // mqLocalMessage.setRoutingKey("judgeRoutingKey");
            // mqLocalMessage.setPayload(message);
            // mqLocalMessage.setStatus(0); // 0-待发送
            // mqLocalMessage.setRetryCount(0);
            // mqLocalMessageService.save(mqLocalMessage);

            // 发送消息到 RabbitMQ 队列
            CorrelationData correlationData = new CorrelationData();
            correlationData.setId("test-message-id-"+i);
            rabbitTemplate.convertAndSend(JudgeMQConfig.EXCHANGE_NAME, JudgeMQConfig.ROUTING_KEY, message, correlationData);
            System.out.println("Sent message: " + message);
            System.out.println();
            Thread.sleep(10000); // 等待1秒钟
        }
    }

    /**
     * ConfirmCallback 实现方法。
     * 当消息发送到 Broker（Exchange）后，Broker 会回调此方法，告知生产者消息是否成功到达 Exchange。
     * @param correlationData 相关的元数据，通常包含消息的唯一ID。发送消息时可以指定。
     * @param ack             true 表示消息成功到达 Exchange，false 表示失败。
     * @param cause           如果失败 (ack=false)，则包含失败的原因。
     *
     *  交换机ID错误，连不上MQ都会在这里呢
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String id = correlationData != null ? correlationData.getId() : "";
        if (ack) {
            log.info("ConfirmCallback: 消息发送成功, id: {}", id);
            mqLocalMessageService.lambdaUpdate()
                    .set(MqLocalMessage::getStatus, 1)
                    .eq(MqLocalMessage::getMessageId, correlationData.getId())
                    .update();
        } else {
            log.error("ConfirmCallback: 消息发送失败, id: {}, 原因: {}", id, cause);
            // 在这里处理消息发送失败的逻辑，例如记录日志、重试、告警等
            mqLocalMessageService.lambdaUpdate()
                    .set(MqLocalMessage::getStatus, 2)
                    .set(MqLocalMessage::getErrorCause, cause)
                    .eq(MqLocalMessage::getMessageId, correlationData.getId())
                    .update();
        }
    }

    /**
     * ReturnsCallback 实现方法。
     * 当消息成功发送到 Exchange，但 Exchange 无法将消息路由到任何 Queue 时（例如，路由键不匹配），
     * 并且 mandatory 标志为 true，Broker 会将消息返回给生产者，并回调此方法。
     * @param returned 包含被退回消息的详细信息，如消息体、replyCode、replyText、exchange、routingKey。
     *
     *  路由Key不正确
     */
    @Override
    public void returnedMessage(ReturnedMessage returned) {
        log.warn("ReturnsCallback: 消息被退回, message: {}, replyCode: {}, replyText: {}, exchange: {}, routingKey: {}",
                new String(returned.getMessage().getBody()),
                returned.getReplyCode(),
                returned.getReplyText(),
                returned.getExchange(),
                returned.getRoutingKey());
        // 在这里处理被退回消息的逻辑，例如记录日志、发送到备用队列、告警等
        String correlationId = returned.getMessage().getMessageProperties().getCorrelationId();
        mqLocalMessageService.lambdaUpdate()
                .set(MqLocalMessage::getStatus, 3)
                .set(MqLocalMessage::getErrorCause, returned.getReplyCode())
                .eq(MqLocalMessage::getMessageId, correlationId)
                .update();
    }

}
