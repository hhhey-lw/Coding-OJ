package com.longoj.top.domain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longoj.top.domain.entity.MqLocalMessage;
import com.longoj.top.domain.service.MqLocalMessageService;
import com.longoj.top.infrastructure.mapper.MqLocalMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author 韦龙
* @description 针对表【mq_local_message(RabbitMQ 本地消息表 (兜底方案))】的数据库操作Service实现
* @createDate 2025-06-15 21:05:24
*/
@Service
public class MqLocalMessageServiceImpl extends ServiceImpl<MqLocalMessageMapper, MqLocalMessage>
    implements MqLocalMessageService{

}




