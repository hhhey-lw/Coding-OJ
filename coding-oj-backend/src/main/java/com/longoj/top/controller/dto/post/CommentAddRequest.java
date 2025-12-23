package com.longoj.top.controller.dto.post;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentAddRequest implements Serializable {
    /** 评论内容 */
    private String content;
    /** 评论文章ID */
    private Long postId;
    /** 父评论ID */
    private Long parentId;
    /** 根评论ID - 二级评论 */
    private Long rootCommentId;
}