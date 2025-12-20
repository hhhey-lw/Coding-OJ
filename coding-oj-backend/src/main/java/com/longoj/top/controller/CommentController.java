package com.longoj.top.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.controller.dto.BaseResponse;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.utils.ResultUtils;
import com.longoj.top.controller.dto.comment.CommentAddRequest;
import com.longoj.top.controller.dto.comment.CommentPageQueryRequest;
import com.longoj.top.controller.dto.comment.CommentVO;
import com.longoj.top.domain.service.CommentService;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.*;


/**
 * 评论接口
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping("/list/page")
    public BaseResponse<Page<CommentVO>> listCommentVOByPage(@RequestBody CommentPageQueryRequest commentPageQueryRequest) {
        return ResultUtils.success(commentService.listCommentVOByPage(commentPageQueryRequest));
    }

    @PostMapping("/add")
    public BaseResponse<CommentVO> addComment(@RequestBody CommentAddRequest commentAddRequest) {
        return ResultUtils.success(commentService.addComment(commentAddRequest));
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> delComment(@RequestBody long commentId) {
        commentService.deleteComment(commentId);
        return ResultUtils.success(Boolean.TRUE);
    }
}
