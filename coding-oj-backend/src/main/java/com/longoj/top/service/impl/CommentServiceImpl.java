package com.longoj.top.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longoj.top.common.ErrorCode;
import com.longoj.top.constant.CommonConstant;
import com.longoj.top.exception.BusinessException;
import com.longoj.top.mapper.CommentMapper;
import com.longoj.top.model.dto.comment.CommentAddRequest;
import com.longoj.top.model.dto.comment.CommentPageQueryRequest;
import com.longoj.top.model.entity.Comment;
import com.longoj.top.model.vo.CommentVO;
import com.longoj.top.model.vo.UserVO;
import com.longoj.top.service.CommentService;
import com.longoj.top.service.PostService;
import com.longoj.top.service.UserService;
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

    // @Override
    public Wrapper<Comment> getPageQueryFirstLevelWrapper(CommentPageQueryRequest commentPageQueryRequest) {
        Long postId = commentPageQueryRequest.getPostId();
        String sortField = commentPageQueryRequest.getSortField();
        String sortOrder = commentPageQueryRequest.getSortOrder();

        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(postId != 0, "post_id", postId);
        queryWrapper.isNull("parent_id");
        queryWrapper.eq("is_delete", 0);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);

        return queryWrapper;
    }

    public List<Comment> getCommentByRootCommentId(Long rootCommentId) {
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("root_commentId", rootCommentId);
        queryWrapper.eq("is_delete", 0);
        queryWrapper.orderByAsc("create_time");
        List<Comment> commentList = lambdaQuery()
                .eq(Comment::getRootCommentId, rootCommentId)
                .eq(Comment::getIsDelete, 0)
                .orderByAsc(Comment::getCreateTime)
                .list();
        return commentList;
    }

    // @Override
    public Page<CommentVO> getCommentVO(Page<Comment> page) {

        Page<CommentVO> commentVOPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        ArrayList<CommentVO> commentVOList = new ArrayList<>();
        for (Comment comment : page.getRecords()) {
            commentVOList.add(CommentVO.toVO(comment));
        }
        commentVOPage.setRecords(commentVOList);
        return commentVOPage;
    }

    @Override
    public Page<CommentVO> listCommentVOByPage(CommentPageQueryRequest commentPageQueryRequest) {
        Page<Comment> page = page(Page.of(commentPageQueryRequest.getCurrent(), commentPageQueryRequest.getPageSize()),
                getPageQueryFirstLevelWrapper(commentPageQueryRequest));

        List<Comment> rootList = page.getRecords();

        Page<CommentVO> commentVOPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        ArrayList<CommentVO> commentVOS = new ArrayList<>();
        HashMap<Long, UserVO> userMap = new HashMap<>();

        for (Comment comment : rootList) {
            CommentVO commentVO = CommentVO.toVO(comment);
            UserVO userVO = userMap.computeIfAbsent(commentVO.getUserId(), userId -> userService.getUserVOByUserId(userId));
            commentVO.setUserVO(userVO);

            List<Comment> childrenList = getCommentByRootCommentId(comment.getCommentId());
            if (childrenList != null && childrenList.isEmpty()) {
                commentVO.setReplies(Collections.emptyList());
            }else {
                List<CommentVO> collect = childrenList.stream()
                        .map(c -> {
                            CommentVO cVO = CommentVO.toVO(c);
                            UserVO uVO = userMap.computeIfAbsent(cVO.getUserId(), userId -> userService.getUserVOByUserId(userId));
                            cVO.setUserVO(uVO);
                            return cVO;
                        })
                        .collect(Collectors.toList());
                commentVO.setReplies(collect);
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
        // if (commentAddRequest.getParentId() == null || commentAddRequest.getRootCommentId() == null) {
        //     throw new BusinessException(ErrorCode.PARAMS_ERROR);
        // }
        if (commentAddRequest.getPostId() == null || commentAddRequest.getPostId() == 0L) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StrUtil.isBlank(commentAddRequest.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Comment comment = Comment.builder()
                .likeCount(0)
                .isDelete(0)
                .status(commentAddRequest.getStatus())
                .postId(commentAddRequest.getPostId())
                .userId(commentAddRequest.getUserId())
                .parentId(commentAddRequest.getParentId())
                .rootCommentId(commentAddRequest.getRootCommentId())
                .content(commentAddRequest.getContent())
                .build();
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
    public Boolean deleteComment(long commentId) {
        return deleteComment(commentId);
    }
}
