package com.longoj.top.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.common.BaseResponse;
import com.longoj.top.common.ErrorCode;
import com.longoj.top.common.ResultUtils;
import com.longoj.top.model.dto.comment.CommentAddRequest;
import com.longoj.top.model.dto.comment.CommentPageQueryRequest;
import com.longoj.top.model.vo.CommentVO;
import com.longoj.top.service.CommentService;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<CommentVO>> listCommentVOByPage(@RequestBody CommentPageQueryRequest commentPageQueryRequest) {
        return ResultUtils.success(commentService.listCommentVOByPage(commentPageQueryRequest));
    }

    @PostMapping("/add")
    public BaseResponse<CommentVO> addComment(@RequestBody CommentAddRequest commentAddRequest) {
        CommentVO commentVO = commentService.addComment(commentAddRequest);
        if (ObjectUtil.isEmpty(commentVO)) {
            return ResultUtils.error(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(commentVO);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> delComment(@RequestBody long commentId) {
        Boolean ok = commentService.deleteComment(commentId);
        if (!ok) {
            return ResultUtils.error(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(commentService.deleteComment(commentId));
    }
}
