package com.longoj.top.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.controller.dto.PageRequest;
import com.longoj.top.controller.dto.post.CommentAddRequest;
import com.longoj.top.controller.dto.post.CommentPageQueryRequest;
import com.longoj.top.controller.dto.post.CommentVO;
import com.longoj.top.controller.dto.post.PostFavourAddRequest;
import com.longoj.top.controller.dto.post.PostThumbAddRequest;
import com.longoj.top.domain.service.*;
import com.longoj.top.infrastructure.aop.annotation.AuthCheck;
import com.longoj.top.controller.dto.BaseResponse;
import com.longoj.top.controller.dto.DeleteRequest;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.utils.PageUtil;
import com.longoj.top.infrastructure.utils.ResultUtils;
import com.longoj.top.domain.entity.constant.UserConstant;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.controller.dto.post.PostAddRequest;
import com.longoj.top.controller.dto.post.PostQueryRequest;
import com.longoj.top.controller.dto.post.PostUpdateRequest;
import com.longoj.top.domain.entity.Post;
import com.longoj.top.domain.entity.User;
import com.longoj.top.controller.dto.post.PostVO;

import javax.annotation.Resource;

import com.longoj.top.infrastructure.utils.UserContext;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子接口
 */
@Slf4j
@RestController
@RequestMapping("/post")
public class PostController {

    @Resource
    private PostService postService;

    @Resource
    private CommentService commentService;

    // region 增删改查

    /**
     * 创建
     *
     * @param postAddRequest
     * @return
     */
    @ApiOperation("创建帖子")
    @PostMapping("/add")
    public BaseResponse<Long> addPost(@RequestBody PostAddRequest postAddRequest) {
        if (postAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(postService.addPost(postAddRequest));
    }

    /**
     * 删除帖子
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePost(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(postService.deleteById(deleteRequest.getId()));
    }

    /**
     * 更新
     */
    @ApiOperation("更新")
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePost(@RequestBody PostUpdateRequest postUpdateRequest) {
        if (postUpdateRequest == null || postUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(postService.update(postUpdateRequest));
    }

    /**
     * 根据 id 获取VO对象
     */
    @ApiOperation("根据 id 获取")
    @GetMapping("/get/vo")
    public BaseResponse<PostVO> getPostVOById(@RequestParam Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(postService.getPostVO(id));
    }

    // FIXME: 这个请求类的问题
    /**
     * 分页获取列表（仅管理员）
     */
    @ApiOperation("分页获取列表（仅管理员）")
    @PostMapping("/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Post>> listPostByPage(@RequestBody PostQueryRequest postQueryRequest) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        return ResultUtils.success(postService.page(postQueryRequest.getSearchKey(), postQueryRequest.getTags(), postQueryRequest.getUserId(), current, size));
    }

    /**
     * 分页获取列表（封装类）
     */
    @ApiOperation("分页获取列表（封装类）")
    @PostMapping("/page/vo")
    public BaseResponse<Page<PostVO>> listPostVOByPage(@RequestBody PostQueryRequest postQueryRequest) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        Page<Post> page = postService.page(postQueryRequest.getSearchKey(), postQueryRequest.getTags(), postQueryRequest.getUserId(), current, size);
        return ResultUtils.success(PageUtil.convertToVO(page, PostVO::convertToVo));
    }

    /**
     * 分页获取当前用户创建的帖子列表
     */
    @ApiOperation("分页获取当前用户创建的帖子列表")
    @PostMapping("/my/page/vo")
    public BaseResponse<Page<PostVO>> listMyPostVOByPage(@RequestBody PostQueryRequest postQueryRequest) {
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        User loginUser = UserContext.getUser();
        Page<Post> page = postService.page(postQueryRequest.getSearchKey(), postQueryRequest.getTags(), loginUser.getId(), current, size);
        return ResultUtils.success(PageUtil.convertToVO(page, PostVO::convertToVo));
    }

    // endregion

    /**
     * 点赞 / 取消点赞
     */
    @ApiOperation("点赞 / 取消点赞")
    @PostMapping("/thumb/toggle")
    public BaseResponse<Boolean> doThumb(@RequestBody PostThumbAddRequest postThumbAddRequest) {
        if (postThumbAddRequest == null || postThumbAddRequest.getPostId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(postService.doThumb(postThumbAddRequest.getPostId()));
    }

    /**
     * 收藏 / 取消收藏
     */
    @ApiOperation("收藏/取消收藏")
    @PostMapping("/favour/toggle")
    public BaseResponse<Boolean> doPostFavour(@RequestBody PostFavourAddRequest postFavourAddRequest) {
        if (postFavourAddRequest == null || postFavourAddRequest.getPostId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(postService.doFavour(postFavourAddRequest.getPostId()));
    }

    /**
     * 获取我收藏的帖子列表
     */
    @ApiOperation("我的收藏分页")
    @PostMapping("/favour/my/page")
    public BaseResponse<Page<PostVO>> listMyFavourPostByPage(@RequestBody PageRequest pageRequest) {
        return ResultUtils.success(postService.pageMyFavour(pageRequest.getCurrent(), pageRequest.getPageSize()));
    }


    // =================== 评论相关接口 ====================

    /**
     * 分页获取评论列表
     */
    @PostMapping("/comment/list/page")
    public BaseResponse<Page<CommentVO>> listCommentVOByPage(@RequestBody CommentPageQueryRequest commentPageQueryRequest) {
        return ResultUtils.success(commentService.listCommentVOByPage(commentPageQueryRequest));
    }

    /**
     * 添加评论
     */
    @PostMapping("/comment/add")
    public BaseResponse<CommentVO> addComment(@RequestBody CommentAddRequest commentAddRequest) {
        return ResultUtils.success(commentService.addComment(commentAddRequest));
    }

    /**
     * 删除评论
     */
    @PostMapping("/comment/delete")
    public BaseResponse<Boolean> delComment(@RequestBody long commentId) {
        commentService.deleteComment(commentId);
        return ResultUtils.success(Boolean.TRUE);
    }


}
