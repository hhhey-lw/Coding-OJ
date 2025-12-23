package com.longoj.top.controller.dto.post;

import lombok.Data;

@Data
public class PostThumbVO {

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 是否点赞
     */
    private boolean isThumb;

    /**
     * 构建VO
     */
    public static PostThumbVO of(Long postId, boolean isThumb) {
        PostThumbVO vo = new PostThumbVO();
        vo.setPostId(postId);
        vo.setThumb(isThumb);
        return vo;
    }

}
