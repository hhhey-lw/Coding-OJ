package com.longoj.top.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.controller.dto.comment.CommentAddRequest;
import com.longoj.top.controller.dto.comment.CommentPageQueryRequest;
import com.longoj.top.domain.entity.Comment;
import com.longoj.top.controller.dto.comment.CommentVO;

public interface CommentService extends IService<Comment> {

    /**
     * 分页获取评论列表
     *
     * @param commentPageQueryRequest 评论分页查询请求
     * @return 评论分页列表
     */
    Page<CommentVO> listCommentVOByPage(CommentPageQueryRequest commentPageQueryRequest);

    /**
     * 添加评论
     *
     * @param commentAddRequest 评论添加请求
     * @return 新增的评论
     */
    CommentVO addComment(CommentAddRequest commentAddRequest);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     */
    void deleteComment(long commentId);
}
