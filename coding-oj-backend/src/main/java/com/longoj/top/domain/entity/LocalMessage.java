package com.longoj.top.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * RabbitMQ 本地消息表
 * @TableName local_message
 */
@TableName(value ="local_message")
@Data
public class LocalMessage implements Serializable {
    /**
     * 消息唯一ID
     */
    @TableId
    private String messageId;

    /**
     * 目标交换机名称
     */
    private String exchangeName;

    /**
     * 目标路由键
     */
    private String routingKey;

    /**
     * 消息体内容
     */
    private String payload;

    /**
     * 消息状态: 0-待发送, 1-发送成功, 2-发送失败待重试, 3-最终失败
     */
    private Integer status;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 发送失败原因
     */
    private String errorCause;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 最后更新时间
     */
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}