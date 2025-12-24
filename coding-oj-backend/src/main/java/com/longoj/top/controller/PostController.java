package com.longoj.top.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.controller.dto.PageRequest;
import com.longoj.top.controller.dto.post.*;
import com.longoj.top.controller.dto.user.UserVO;
import com.longoj.top.domain.service.*;
import com.longoj.top.infrastructure.aop.annotation.AuthCheck;
import com.longoj.top.controller.dto.BaseResponse;
import com.longoj.top.controller.dto.DeleteRequest;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.utils.PageUtil;
import com.longoj.top.infrastructure.utils.ResultUtils;
import com.longoj.top.domain.entity.constant.UserConstant;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.domain.entity.Post;
import com.longoj.top.domain.entity.User;

import javax.annotation.Resource;

import com.longoj.top.infrastructure.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 帖子接口
 */
@Slf4j
@Api("帖子接口")
@RestController
@RequestMapping("/post")
public class PostController {

    @Resource
    private PostService postService;

    @Resource
    private CommentService commentService;
    @Autowired
    private UserService userService;

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

    /**
     * 分页获取列表（封装类）
     */
    @ApiOperation("分页获取列表（封装类）")
    @PostMapping("/page/vo")
    public BaseResponse<Page<PostVO>> listPostVOByPage(@RequestBody PostQueryRequest postQueryRequest) {
        int current = postQueryRequest.getCurrent();
        int size = postQueryRequest.getPageSize();
        Page<Post> page = postService.page(postQueryRequest.getSearchKey(), postQueryRequest.getSortField(), postQueryRequest.getSortOrder(), current, size);
        Map<Long, User> userMap = userService.listByIds(page.getRecords().stream()
                        .map(Post::getUserId)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return ResultUtils.success(PageUtil.convertToVO(page, post -> {
            PostVO postVO = PostVO.convertToVo(post);
            postVO.setUser(UserVO.toVO(userMap.get(post.getUserId())));
            return postVO;
        }));
    }

    /**
     * 分页获取当前用户创建的帖子列表
     */
    @ApiOperation("分页我的帖子列表")
    @PostMapping("/my/page/vo")
    public BaseResponse<Page<PostVO>> listMyPostVOByPage(@RequestBody PageRequest pageRequest) {
        int current = pageRequest.getCurrent();
        int size = pageRequest.getPageSize();
        return ResultUtils.success(postService.pageMy(current, size));
    }

    // endregion

    /**
     * 点赞 / 取消点赞
     */
    @ApiOperation("点赞 / 取消点赞")
    @PostMapping("/thumb/toggle")
    public BaseResponse<PostThumbVO> doThumb(@RequestBody PostThumbAddRequest postThumbAddRequest) {
        if (postThumbAddRequest == null || postThumbAddRequest.getPostId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(PostThumbVO.of(postThumbAddRequest.getPostId(), postService.doThumb(postThumbAddRequest.getPostId())));
    }

    /**
     * 收藏 / 取消收藏
     */
    @ApiOperation("收藏/取消收藏")
    @PostMapping("/favour/toggle")
    public BaseResponse<PostFavourVO> doPostFavour(@RequestBody PostFavourAddRequest postFavourAddRequest) {
        if (postFavourAddRequest == null || postFavourAddRequest.getPostId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(PostFavourVO.of(postFavourAddRequest.getPostId(), postService.doFavour(postFavourAddRequest.getPostId())));
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
    @ApiOperation("评论分页")
    @PostMapping("/comment/list/page")
    public BaseResponse<Page<CommentVO>> listCommentVOByPage(@RequestBody CommentPageQueryRequest commentPageQueryRequest) {
        return ResultUtils.success(commentService.listCommentVOByPage(commentPageQueryRequest));
    }

    /**
     * 添加评论
     */
    @ApiOperation("添加评论")
    @PostMapping("/comment/add")
    public BaseResponse<CommentVO> addComment(@RequestBody CommentAddRequest commentAddRequest) {
        return ResultUtils.success(commentService.addComment(commentAddRequest));
    }

    /**
     * 删除评论
     */
    @ApiOperation("删除评论")
    @PostMapping("/comment/delete")
    public BaseResponse<Boolean> delComment(@RequestBody DeleteRequest deleteRequest) {
        commentService.deleteComment(deleteRequest.getId());
        return ResultUtils.success(Boolean.TRUE);
    }


}
