package com.longoj.top.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Slf4j
@Configuration
public class JudgeMQConfig {
    // 主队列、交换机、路由键
    public static final String QUEUE_NAME = "judgeQueue";
    public static final String EXCHANGE_NAME = "judgeExchange";
    public static final String ROUTING_KEY = "judgeRoutingKey";

    // 死信队列、交换机、路由键
    public static final String DLX_EXCHANGE_NAME = "judgeDlxExchange";
    public static final String DLQ_NAME = "judgeDlxQueue";
    public static final String DLQ_ROUTING_KEY = "judgeDlxRoutingKey";

    // --- 死信队列配置 ---

    @Bean
    public Queue judgeDlxQueue() {
        // 创建死信队列
        return new Queue(DLQ_NAME, true, false, false);
    }

    @Bean
    public Exchange judgeDlxExchange() {
        // 创建死信交换机 (通常也是 DirectExchange 或 FanoutExchange)
        return new DirectExchange(DLX_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Binding judgeDlxBinding(Queue judgeDlxQueue, Exchange judgeDlxExchange) {
        // 将死信队列绑定到死信交换机
        return BindingBuilder.bind(judgeDlxQueue).to(judgeDlxExchange).with(DLQ_ROUTING_KEY).noargs();
    }

    // --- 主队列配置 ---

    @Bean
    public Queue judgeQueue() {
        // 创建一个名为 "judgeQueue" 的队列
        HashMap<String, Object> args = new HashMap<>();
        // 设置死信交换机
        args.put("x-dead-letter-exchange", DLX_EXCHANGE_NAME);
        // 设置死信 路由键
        args.put("x-dead-letter-routing-key", DLQ_ROUTING_KEY);
        return new Queue(QUEUE_NAME, true, false, false, args);
    }

    @Bean
    public Exchange judgeExchange() {
        // 创建一个名为 "judgeExchange" 的交换机
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Binding judgeBinding(Queue judgeQueue, Exchange judgeExchange) {
        // 将队列和交换机绑定
        return BindingBuilder.bind(judgeQueue).to(judgeExchange).with(ROUTING_KEY).noargs();
    }

}
