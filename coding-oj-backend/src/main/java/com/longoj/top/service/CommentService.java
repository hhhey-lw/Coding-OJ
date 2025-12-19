package com.longoj.top.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.model.dto.comment.CommentAddRequest;
import com.longoj.top.model.dto.comment.CommentPageQueryRequest;
import com.longoj.top.model.entity.Comment;
import com.longoj.top.model.vo.CommentVO;

public interface CommentService extends IService<Comment> {
    // Wrapper<Comment> getPageQueryFirstLevelWrapper(CommentPageQueryRequest commentPageQueryRequest);

    Page<CommentVO> listCommentVOByPage(CommentPageQueryRequest commentPageQueryRequest);

    CommentVO addComment(CommentAddRequest commentAddRequest);

    Boolean deleteComment(long commentId);
}
