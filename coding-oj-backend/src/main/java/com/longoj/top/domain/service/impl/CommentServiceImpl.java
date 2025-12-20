package com.longoj.top.domain.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longoj.top.domain.entity.Post;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.domain.entity.constant.CommonConstant;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.infrastructure.mapper.CommentMapper;
import com.longoj.top.controller.dto.comment.CommentAddRequest;
import com.longoj.top.controller.dto.comment.CommentPageQueryRequest;
import com.longoj.top.domain.entity.Comment;
import com.longoj.top.controller.dto.comment.CommentVO;
import com.longoj.top.controller.dto.user.UserVO;
import com.longoj.top.domain.service.CommentService;
import com.longoj.top.domain.service.PostService;
import com.longoj.top.domain.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private UserService userService;

    @Resource
    private PostService postService;

    @Override
    public Page<CommentVO> listCommentVOByPage(CommentPageQueryRequest commentPageQueryRequest) {
        // 1. 一级评论分页
        Page<Comment> page = getCommentPage(commentPageQueryRequest);

        // 2. 封装评论VO
        Page<CommentVO> commentVOPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        ArrayList<CommentVO> commentVOS = new ArrayList<>();
        HashMap<Long, UserVO> userMap = new HashMap<>();
        for (Comment comment : page.getRecords()) {
            // 封装一级评论
            CommentVO commentVO = CommentVO.toVO(comment);
            commentVO.setUserVO(userMap.computeIfAbsent(commentVO.getUserId(), userId -> userService.getUserVOByUserId(userId)));

            // 封装子评论
            List<Comment> childrenList = getCommentByRootCommentId(comment.getCommentId());
            if (CollectionUtil.isEmpty(childrenList)) {
                commentVO.setReplies(Collections.emptyList());
            }
            else {
                commentVO.setReplies(childrenList.stream()
                        .map(c -> {
                            CommentVO cVO = CommentVO.toVO(c);
                            cVO.setUserVO(userMap.computeIfAbsent(cVO.getUserId(), userId -> userService.getUserVOByUserId(userId)));
                            return cVO;
                        })
                        .collect(Collectors.toList()));
            }
            commentVOS.add(commentVO);
        }
        commentVOPage.setRecords(commentVOS);
        return commentVOPage;
    }

    @Override
    @Transactional
    public CommentVO addComment(CommentAddRequest commentAddRequest) {
        if (commentAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (commentAddRequest.getPostId() == null || commentAddRequest.getPostId() == 0L) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StrUtil.isBlank(commentAddRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Comment comment = Comment.buildComment(commentAddRequest);
        boolean save = save(comment);
        if (!save) {
            throw new BusinessException(ErrorCode.INSERT_ERROR);
        }

        // 更新评论数
        postService.incrementCommentCount(comment.getPostId());

        CommentVO commentVO = CommentVO.toVO(getById(comment.getCommentId()));
        commentVO.setUserVO(userService.getUserVOByUserId(comment.getUserId()));
        return commentVO;
    }

    @Override
    public void deleteComment(long commentId) {
        int i = baseMapper.deleteById(commentId);
        if (i != 1) {
            throw new BusinessException("删除评论失败");
        }
    }

    /**
     * 分页查询一级评论
     *
     * @param commentPageQueryRequest 评论分页查询请求体
     * @return 评论分页
     */
    private Page<Comment> getCommentPage(CommentPageQueryRequest commentPageQueryRequest) {
        Long postId = commentPageQueryRequest.getPostId();
        String sortField = commentPageQueryRequest.getSortField();
        String sortOrder = commentPageQueryRequest.getSortOrder();

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(null != postId, Comment::getPostId, postId);
        queryWrapper.isNull(Comment::getParentId);
        queryWrapper.eq(Comment::getIsDelete, Boolean.FALSE);
        queryWrapper.last(StringUtils.isNotBlank(sortField), "ORDER BY " + sortField + " " + sortOrder);

        return page(Page.of(commentPageQueryRequest.getCurrent(), commentPageQueryRequest.getPageSize()), queryWrapper);
    }

    /**
     * 根据根评论ID获取子评论列表
     *
     * @param rootCommentId 根评论ID
     * @return 子评论列表
     */
    private List<Comment> getCommentByRootCommentId(Long rootCommentId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getRootCommentId, rootCommentId);
        queryWrapper.eq(Comment::getIsDelete, Boolean.FALSE);
        queryWrapper.orderByAsc(Comment::getCreateTime);
        return baseMapper.selectList(queryWrapper);
    }

}
