package com.longoj.top.controller.dto.post;

import lombok.Data;

/**
 * 帖子收藏VO
 */
@Data
public class PostFavourVO {
    /**
     * 帖子ID
     */
    private Long postId;
    /**
     * 是否收藏
     */
    private boolean isFavour;

    /**
     * 构建VO
     */
    public static PostFavourVO of(Long postId, boolean isFavour) {
        PostFavourVO vo = new PostFavourVO();
        vo.setPostId(postId);
        vo.setFavour(isFavour);
        return vo;
    }
}
