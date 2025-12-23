package com.longoj.top.controller.dto.post;

import cn.hutool.core.bean.BeanUtil;
import com.longoj.top.domain.entity.Comment;
import com.longoj.top.controller.dto.user.UserVO;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 评论实体类
 */
@Data
public class CommentVO {
    /** 评论ID */
    private Long commentId;
    /** 评论内容 */
    private String content;
    /** 评论文章ID */
    private Long postId;
    /** 评论用户ID */
    private Long userId;

    private UserVO userVO;
    private List<CommentVO> replies;

    /** 父评论ID */
    private Long parentId;
    /** 点赞数 */
    private Integer likeCount;
    /** 创建时间 */
    private Date createTime;

    public static CommentVO toVO(Comment comment) {
        return BeanUtil.copyProperties(comment, CommentVO.class);
    }
}
