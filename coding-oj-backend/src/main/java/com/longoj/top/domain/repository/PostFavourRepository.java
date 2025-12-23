package com.longoj.top.domain.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.domain.entity.Post;
import com.longoj.top.domain.entity.PostFavour;

import java.util.List;

public interface PostFavourRepository {
    /**
     * 获取帖子收藏记录
     */
    PostFavour getFavourRecord(Long postId, Long loginUserId);

    /**
     * 添加帖子收藏记录
     */
    boolean addFavourRecord(long postId, Long loginUserId);

    /**
     * 删除帖子收藏记录
     */
    boolean removeFavourRecord(long postId, Long loginUserId);

    /**
     * 分页获取收藏的帖子
     */
    Page<PostFavour> page(Long userId, int current, int pageSize);
}
