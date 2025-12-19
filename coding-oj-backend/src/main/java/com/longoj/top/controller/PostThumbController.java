package com.longoj.top.controller;

import com.longoj.top.common.BaseResponse;
import com.longoj.top.common.ErrorCode;
import com.longoj.top.common.ResultUtils;
import com.longoj.top.exception.BusinessException;
import com.longoj.top.model.dto.postthumb.PostThumbAddRequest;
import com.longoj.top.model.entity.User;
import com.longoj.top.service.PostThumbService;
import com.longoj.top.service.UserService;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.longoj.top.utils.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子点赞接口
 *
 */
@RestController
@RequestMapping("/post_thumb")
@Slf4j
// @Api(tags = "帖子点赞接口")
public class PostThumbController {

    @Resource
    private PostThumbService postThumbService;

    @Resource
    private UserService userService;

    /**
     * 点赞 / 取消点赞
     *
     * @param postThumbAddRequest
     * @param request
     * @return resultNum 本次点赞变化数
     */
    @ApiOperation("点赞 / 取消点赞")
    @PostMapping("/")
    public BaseResponse<Integer> doThumb(@RequestBody PostThumbAddRequest postThumbAddRequest,
            HttpServletRequest request) {
        if (postThumbAddRequest == null || postThumbAddRequest.getPostId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        // final User loginUser = UserContext.getUser();
        long postId = postThumbAddRequest.getPostId();
        int result = postThumbService.doPostThumb(postId, loginUser);
        return ResultUtils.success(result);
    }

}
