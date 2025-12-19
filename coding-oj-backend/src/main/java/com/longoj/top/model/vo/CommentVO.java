package com.longoj.top.model.vo;

import cn.hutool.core.bean.BeanUtil;
import com.longoj.top.model.entity.Comment;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 评论实体类
 */
@Data
public class CommentVO {
    private Long commentId;         // 评论ID
    private String content;         // 评论内容
    private Long postId;           // 评论文章ID
    private Long userId;           // 评论用户ID

    private UserVO userVO;
    private List<CommentVO> replies;

    private Long parentId;         // 父评论ID
    private Integer likeCount;     // 点赞数
    private Date createTime; // 创建时间

    public static CommentVO toVO(Comment comment) {
        CommentVO commentVO = BeanUtil.copyProperties(comment, CommentVO.class);
        return commentVO;
    }
}
