package com.longoj.top.controller.dto.comment;


import lombok.Data;

import java.io.Serializable;

/**
 * 评论实体类
 */
@Data
public class CommentQueryRequest implements Serializable {
    private Long commentId;         // 评论ID
    // private String content;         // 评论内容
    private Long postId;           // 评论文章ID
    private Long userId;           // 评论用户ID
    // private Long parentId;         // 父评论ID
    // private Integer likeCount;     // 点赞数
    // private Integer status;        // 状态(0-正常,1-审核中,2-删除)
    // private Date createTime; // 创建时间
    // private Date updateTime; // 更新时间

}