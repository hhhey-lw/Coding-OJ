package com.longoj.top.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.controller.dto.post.PostAddRequest;
import com.longoj.top.controller.dto.post.PostUpdateRequest;
import com.longoj.top.controller.dto.question.QuestionVO;
import com.longoj.top.domain.entity.Post;
import com.longoj.top.controller.dto.post.PostVO;
import com.longoj.top.domain.entity.Tag;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子服务
 */
public interface PostService extends IService<Post> {

    /**
     * 创建帖子
     */
    Long addPost(PostAddRequest postAddRequest);

    /**
     * 获取帖子VO
     */
    PostVO getPostVO(Long postId);

    /**
     * 分页查询帖子
     */
    Page<Post> page(String searchKey, String sortField, String sortOrder, int current, int size);

    /**
     * 分页查询我的帖子
     */
    Page<PostVO> pageMy(int current, int size);

    /**
     * 分页获取我收藏的帖子列表
     */
    Page<PostVO> pageMyFavour(int current, int pageSize);

    /**
     * 增加评论数
     */
    boolean incrementCommentCount(Long postId);

    /**
     * 增加浏览数
     */
    boolean incrementPageView(Long postId);

    /**
     * 增加点赞数
     */
    boolean incrementThumbCount(long postId);

    /**
     * 减少点赞数
     */
    boolean decrementThumbCount(long postId);

    /**
     * 根据id删除帖子
     */
    Boolean deleteById(Long id);

    /**
     * 更新帖子
     */
    Boolean update(PostUpdateRequest postUpdateRequest);

    /**
     * 增加收藏数
     */
    boolean incrementFavourNum(long postId);

    /**
     * 减少收藏数
     */
    boolean decrementFavourNum(long postId);

    /**
     * 帖子收藏
     */
    boolean doFavour(long postId);

    /**
     * 点赞
     */
    boolean doThumb(long postId);

}
