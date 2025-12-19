package com.longoj.top.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 评论实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "comment")
public class Comment {
    @TableId(type = IdType.ASSIGN_ID)
    private Long commentId;         // 评论ID
    private String content;         // 评论内容
    private Long postId;           // 评论文章ID
    private Long userId;           // 评论用户ID
    private Long parentId;         // 父评论ID
    private Long rootCommentId;         // 根评论ID  - 二级评论啦
    private Integer likeCount;     // 点赞数
    private Integer status;        // 状态(0-评论, 1-回复)
    @TableLogic
    private Integer isDelete; // 逻辑删除
    private Date createTime; // 创建时间
    private Date updateTime; // 更新时间
}
