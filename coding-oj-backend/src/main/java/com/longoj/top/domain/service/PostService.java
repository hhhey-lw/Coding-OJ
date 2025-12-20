package com.longoj.top.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.controller.dto.post.PostQueryRequest;
import com.longoj.top.domain.entity.Post;
import com.longoj.top.controller.dto.post.PostVO;
import javax.servlet.http.HttpServletRequest;

/**
 * 帖子服务
 */
public interface PostService extends IService<Post> {

    /**
     * 获取查询条件
     *
     * @param postQueryRequest
     * @return
     */
    QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest);

    /**
     * 获取帖子封装
     *
     * @param post
     * @param request
     * @return
     */
    PostVO getPostVO(Post post, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param postPage
     * @param request
     * @return
     */
    Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request);

    boolean incrementCommentCount(Long postId);
    boolean incrementPageView(Long postId);
}
