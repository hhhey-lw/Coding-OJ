package com.longoj.top.domain.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.domain.entity.Post;
import com.longoj.top.domain.entity.PostFavour;
import com.longoj.top.domain.repository.PostFavourRepository;
import com.longoj.top.infrastructure.mapper.PostFavourMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class PostFavourRepositoryImpl implements PostFavourRepository {

    @Resource
    private PostFavourMapper postFavourMapper;

    @Override
    public PostFavour getFavourRecord(Long postId, Long loginUserId) {
        LambdaQueryWrapper<PostFavour> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostFavour::getPostId, postId);
        queryWrapper.eq(PostFavour::getUserId, loginUserId);
        queryWrapper.eq(PostFavour::getIsDelete, Boolean.FALSE);
        return postFavourMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean addFavourRecord(long postId, Long loginUserId) {
        PostFavour postFavour = PostFavour.buildEntity(postId, loginUserId);
        return postFavourMapper.insert(postFavour) > 0;
    }

    @Override
    public boolean removeFavourRecord(long postId, Long loginUserId) {
        LambdaUpdateWrapper<PostFavour> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PostFavour::getPostId, postId);
        updateWrapper.eq(PostFavour::getUserId, loginUserId);
        updateWrapper.eq(PostFavour::getIsDelete, Boolean.FALSE);
        updateWrapper.set(PostFavour::getIsDelete, Boolean.TRUE);
        return postFavourMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public Page<PostFavour> page(Long userId, int current, int pageSize) {
        LambdaQueryWrapper<PostFavour> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostFavour::getUserId, userId);
        queryWrapper.eq(PostFavour::getIsDelete, Boolean.FALSE);
        return postFavourMapper.selectPage(new Page<>(current, pageSize), queryWrapper);
    }
}
