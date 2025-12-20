package com.longoj.top.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.longoj.top.controller.dto.comment.CommentAddRequest;
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
    /**
     * 评论ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long commentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论文章ID
     */
    private Long postId;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 父评论ID
     */
    private Long parentId;

    /**
     * 根评论ID
     */
    private Long rootCommentId;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 状态(0-评论, 1-回复)
     */
    private Integer status;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 根据 CommentAddRequest 构建 Comment 实体
     */
    public static Comment buildComment(CommentAddRequest commentAddRequest) {
        return Comment.builder()
                .likeCount(0)
                .isDelete(0)
                .status(commentAddRequest.getStatus())
                .postId(commentAddRequest.getPostId())
                .userId(commentAddRequest.getUserId())
                .parentId(commentAddRequest.getParentId())
                .rootCommentId(commentAddRequest.getRootCommentId())
                .content(commentAddRequest.getContent())
                .build();
    }

}
