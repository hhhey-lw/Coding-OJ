package com.longoj.top.model.dto.comment;

import lombok.Data;
import reactor.util.annotation.Nullable;

import java.io.Serializable;

@Data
public class CommentAddRequest implements Serializable {
     // 评论ID
    private String content;         // 评论内容
    private Long postId;           // 评论文章ID
    private Long userId;           // 评论用户ID
    private Long parentId;         // 父评论ID
    @Nullable
    private Long rootCommentId;         // 根评论ID  - 二级评论啦
    private Integer status;        // 状态(0-评论, 1-回复)
}