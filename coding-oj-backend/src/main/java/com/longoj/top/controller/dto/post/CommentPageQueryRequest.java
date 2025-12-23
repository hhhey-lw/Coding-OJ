package com.longoj.top.controller.dto.post;

import com.longoj.top.controller.dto.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommentPageQueryRequest extends PageRequest implements Serializable {

    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * 文章ID
     */
    private Long postId;

}
