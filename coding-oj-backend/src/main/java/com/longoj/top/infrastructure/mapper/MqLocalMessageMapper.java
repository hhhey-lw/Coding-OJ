package com.longoj.top.infrastructure.mapper;

import com.longoj.top.domain.entity.LocalMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author 韦龙
* @description 针对表【mq_local_message(RabbitMQ 本地消息表 (兜底方案))】的数据库操作Mapper
* @createDate 2025-06-15 21:05:24
* @Entity com.longoj.top.model.entity.MqLocalMessage
*/
public interface MqLocalMessageMapper extends BaseMapper<LocalMessage> {

}




