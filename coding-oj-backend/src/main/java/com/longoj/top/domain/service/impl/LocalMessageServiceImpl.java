package com.longoj.top.domain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longoj.top.domain.entity.LocalMessage;
import com.longoj.top.domain.service.LocalMessageService;
import com.longoj.top.infrastructure.mapper.MqLocalMessageMapper;
import org.springframework.stereotype.Service;

/**
 * 本地消息表服务实现
 *
 * @author 韦龙
 * @createDate 2025-06-15 21:05:24
 */
@Service
public class LocalMessageServiceImpl extends ServiceImpl<MqLocalMessageMapper, LocalMessage>
        implements LocalMessageService {

}




