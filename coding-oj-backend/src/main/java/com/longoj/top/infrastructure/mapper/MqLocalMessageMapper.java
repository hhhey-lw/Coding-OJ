package com.longoj.top.infrastructure.mapper;

import com.longoj.top.domain.entity.LocalMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * RabbitMQ 本地消息表
 *
* @author 韦龙
* @createDate 2025-06-15 21:05:24
*/
@Mapper
public interface MqLocalMessageMapper extends BaseMapper<LocalMessage> {

}




