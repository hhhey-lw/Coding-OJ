package com.longoj.top.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 帖子收藏
 *
 **/
@TableName(value = "post_favour")
@Data
public class PostFavour implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 帖子 id
     */
    private Long postId;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 构建实体
     */
    public static PostFavour buildEntity(Long postId, Long loginUserId) {
        PostFavour postFavour = new PostFavour();
        postFavour.setPostId(postId);
        postFavour.setUserId(loginUserId);
        postFavour.setIsDelete(0);
        Date now = new Date();
        postFavour.setCreateTime(now);
        postFavour.setUpdateTime(now);
        return postFavour;
    }
}