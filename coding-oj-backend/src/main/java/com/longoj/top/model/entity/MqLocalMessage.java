package com.longoj.top.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * RabbitMQ 本地消息表 (兜底方案)
 * @TableName mq_local_message
 */
@TableName(value ="mq_local_message")
@Data
public class MqLocalMessage implements Serializable {
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
     * 消息体内容 (根据实际情况选择 TEXT, JSON, BLOB 等)
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
     * 上次发送失败原因
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

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        MqLocalMessage other = (MqLocalMessage) that;
        return (this.getMessageId() == null ? other.getMessageId() == null : this.getMessageId().equals(other.getMessageId()))
            && (this.getExchangeName() == null ? other.getExchangeName() == null : this.getExchangeName().equals(other.getExchangeName()))
            && (this.getRoutingKey() == null ? other.getRoutingKey() == null : this.getRoutingKey().equals(other.getRoutingKey()))
            && (this.getPayload() == null ? other.getPayload() == null : this.getPayload().equals(other.getPayload()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getRetryCount() == null ? other.getRetryCount() == null : this.getRetryCount().equals(other.getRetryCount()))
            && (this.getErrorCause() == null ? other.getErrorCause() == null : this.getErrorCause().equals(other.getErrorCause()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()))
            && (this.getUpdatedAt() == null ? other.getUpdatedAt() == null : this.getUpdatedAt().equals(other.getUpdatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getMessageId() == null) ? 0 : getMessageId().hashCode());
        result = prime * result + ((getExchangeName() == null) ? 0 : getExchangeName().hashCode());
        result = prime * result + ((getRoutingKey() == null) ? 0 : getRoutingKey().hashCode());
        result = prime * result + ((getPayload() == null) ? 0 : getPayload().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getRetryCount() == null) ? 0 : getRetryCount().hashCode());
        result = prime * result + ((getErrorCause() == null) ? 0 : getErrorCause().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        result = prime * result + ((getUpdatedAt() == null) ? 0 : getUpdatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", messageId=").append(messageId);
        sb.append(", exchangeName=").append(exchangeName);
        sb.append(", routingKey=").append(routingKey);
        sb.append(", payload=").append(payload);
        sb.append(", status=").append(status);
        sb.append(", retryCount=").append(retryCount);
        sb.append(", errorCause=").append(errorCause);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}