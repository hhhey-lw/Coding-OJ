package com.longoj.top.domain.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.longoj.top.domain.entity.PostThumb;
import com.longoj.top.domain.repository.PostThumbRepository;
import com.longoj.top.infrastructure.mapper.PostThumbMapper;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class PostThumbRepositoryImpl implements PostThumbRepository {

    @Resource
    private PostThumbMapper postThumbMapper;

    @Override
    public PostThumb getThumbPostByUserId(long postId, long userId) {
        LambdaQueryWrapper<PostThumb> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PostThumb::getPostId, postId);
        queryWrapper.eq(PostThumb::getUserId, userId);
        queryWrapper.eq(PostThumb::getIsDelete, Boolean.FALSE);

        return postThumbMapper.selectOne(queryWrapper);
    }

    @Override
    public void addPostThumb(long userId, long postId) {
        PostThumb postThumb = PostThumb.buildEntity(userId, postId);
        postThumbMapper.insert(postThumb);
    }

    @Override
    public void removePostThumb(long userId, long postId) {
        LambdaUpdateWrapper<PostThumb> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PostThumb::getUserId, userId);
        updateWrapper.eq(PostThumb::getPostId, postId);
        updateWrapper.eq(PostThumb::getIsDelete, Boolean.FALSE);
        updateWrapper.set(PostThumb::getIsDelete, Boolean.TRUE);
        postThumbMapper.update(null, updateWrapper);
    }
}
