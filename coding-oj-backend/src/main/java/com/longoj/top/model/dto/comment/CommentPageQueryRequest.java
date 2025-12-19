package com.longoj.top.model.dto.comment;

import com.longoj.top.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class CommentPageQueryRequest extends PageRequest implements Serializable {

    private Long commentId;         // 评论ID
    private Long postId;           // 评论文章ID
    private Long userId;           // 评论用户ID
    // private Long parentId;         // 父评论ID
    // private Integer likeCount;     // 点赞数

}
