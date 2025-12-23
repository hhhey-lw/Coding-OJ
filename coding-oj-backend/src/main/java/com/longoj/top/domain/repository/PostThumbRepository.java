package com.longoj.top.domain.repository;

import com.longoj.top.domain.entity.PostThumb;

public interface PostThumbRepository {
    /**
     * 根据帖子ID和用户ID获取帖子点赞记录
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 帖子点赞记录
     */
    PostThumb getThumbPostByUserId(long postId, long userId);

    /**
     * 添加帖子点赞记录
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    void addPostThumb(long userId, long postId);

    /**
     * 移除帖子点赞记录
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    void removePostThumb(long userId, long postId);
}
