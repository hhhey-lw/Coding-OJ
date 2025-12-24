package com.longoj.top.domain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longoj.top.controller.dto.post.PostAddRequest;
import com.longoj.top.controller.dto.post.PostUpdateRequest;
import com.longoj.top.domain.entity.constant.CommonConstant;
import com.longoj.top.domain.repository.PostFavourRepository;
import com.longoj.top.domain.repository.PostThumbRepository;
import com.longoj.top.infrastructure.exception.BusinessException;
import com.longoj.top.infrastructure.exception.ErrorCode;
import com.longoj.top.infrastructure.mapper.PostFavourMapper;
import com.longoj.top.infrastructure.mapper.PostMapper;
import com.longoj.top.infrastructure.mapper.PostThumbMapper;
import com.longoj.top.controller.dto.post.PostQueryRequest;
import com.longoj.top.domain.entity.Post;
import com.longoj.top.domain.entity.PostFavour;
import com.longoj.top.domain.entity.PostThumb;
import com.longoj.top.domain.entity.User;
import com.longoj.top.controller.dto.post.PostVO;
import com.longoj.top.controller.dto.user.UserVO;
import com.longoj.top.domain.service.PostService;
import com.longoj.top.domain.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.longoj.top.infrastructure.utils.PageUtil;
import com.longoj.top.infrastructure.utils.ResultUtils;
import com.longoj.top.infrastructure.utils.ThrowUtils;
import com.longoj.top.infrastructure.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.collection.CollUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 帖子服务实现
 *
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Resource
    private UserService userService;

    @Resource
    private PostFavourRepository postFavourRepository;

    @Resource
    private PostThumbRepository postThumbRepository;

    @Override
    public Long addPost(PostAddRequest postAddRequest) {
        Post post = PostAddRequest.toEntity(postAddRequest);
        this.save(post);
        return post.getId();
    }

    @Override
    public PostVO getPostVO(Long postId) {
        Post post = getById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        PostVO postVO = PostVO.convertToVo(post);

        // 1. 关联查询用户信息
        UserVO userVO = UserVO.toVO(userService.getById(post.getUserId()));
        postVO.setUser(userVO);

        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = UserContext.getUser();
        if (loginUser != null) {
            // 获取点赞
            PostThumb postThumb = postThumbRepository.getThumbPostByUserId(postId, loginUser.getId());
            postVO.setIsThumb(postThumb != null);
            // 获取收藏
            PostFavour postFavour = postFavourRepository.getFavourRecord(postId, loginUser.getId());
            postVO.setIsFavour(postFavour != null);
        }
        // 浏览 + 1
        incrementPageView(postId);
        return postVO;
    }

    @Override
    public boolean incrementCommentCount(Long postId) {
        return lambdaUpdate()
                .setSql("comment_num = comment_num + 1")
                .eq(Post::getId, postId)
                .update();
    }

    @Override
    public boolean incrementPageView(Long postId) {
        return lambdaUpdate()
                .setSql("view_num = view_num + 1")
                .eq(Post::getId, postId)
                .update();
    }

    @Override
    public boolean incrementThumbCount(long postId) {
        return lambdaUpdate()
                .setSql("thumb_num = thumb_num + 1")
                .eq(Post::getId, postId)
                .update();
    }

    @Override
    public boolean decrementThumbCount(long postId) {
        return lambdaUpdate()
                .setSql("thumb_num = thumb_num - 1")
                .eq(Post::getId, postId)
                .gt(Post::getThumbNum, 0)
                .update();
    }

    @Override
    public boolean incrementFavourNum(long postId) {
        return lambdaUpdate()
                .setSql("favour_num = favour_num - 1")
                .eq(Post::getId, postId)
                .gt(Post::getThumbNum, 0)
                .update();
    }

    @Override
    public boolean decrementFavourNum(long postId) {
        return lambdaUpdate()
                .setSql("favour_num = favour_num - 1")
                .eq(Post::getId, postId)
                .gt(Post::getThumbNum, 0)
                .update();
    }

    @Override
    public Boolean deleteById(Long id) {
        Post post = getById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return removeById(id);
    }

    @Override
    public Boolean update(PostUpdateRequest postUpdateRequest) {
        Post post = postUpdateRequest.toEntity(postUpdateRequest);

        // 参数校验
        validPost(post);

        // 判断是否存在
        Post oldPost = getById(postUpdateRequest.getId());
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);

        return updateById(post);
    }

    @Override
    public Page<Post> page(String searchKey, String sortField, String sortOrder, int current, int size) {
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(searchKey)) {
            queryWrapper.like(Post::getTitle, searchKey);
        }
        if (StringUtils.isNotBlank(sortField)) {
            queryWrapper.last(" order by " + sortField + " " + sortOrder);
        }
         return page(new Page<>(current, size), queryWrapper);
    }

    @Override
    public Page<PostVO> pageMy(int current, int size) {
        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Post::getUserId, UserContext.getUser().getId());
        queryWrapper.eq(Post::getIsDelete, Boolean.FALSE);
        queryWrapper.orderByDesc(Post::getCreateTime);
        return PageUtil.convertToVO(page(new Page<>(current, size), queryWrapper), post -> {
            PostVO postVO = PostVO.convertToVo(post);
            postVO.setUser(UserVO.toVO(UserContext.getUser()));
            return postVO;
        });
    }

    @Override
    public Page<PostVO> pageMyFavour(int current, int pageSize) {
        Long loginUserId = UserContext.getUser().getId();
        Page<PostFavour> postFavourPage = postFavourRepository.page(loginUserId, current, pageSize);
        if (postFavourPage == null || CollUtil.isEmpty(postFavourPage.getRecords())) {
            return PageUtil.emptyPage(current, pageSize);
        }
        // 获取帖子列表(注意帖子可能会被删除)
        List<Post> postList = listByIds(postFavourPage.getRecords().stream()
                .map(PostFavour::getPostId)
                .collect(Collectors.toSet()));
        if (CollUtil.isEmpty(postList)) {
            return PageUtil.emptyPage(current, pageSize);
        }
        Map<Long, Post> postMap = postList.stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));
        Map<Long, User> userMap = userService.listByIds(postList.stream()
                        .map(Post::getUserId)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return PageUtil.convertToVO(postFavourPage, postFavour -> {
            Post post = postMap.get(postFavour.getPostId());
            if (post == null) {
                return null;
            }
            PostVO postVO = PostVO.convertToVo(post);
            postVO.setUser(UserVO.toVO(userMap.get(post.getUserId())));
            return postVO;
        });
    }

    /**
     * 帖子收藏
     */
    @Override
    public boolean doFavour(long postId) {
        // 判断是否存在
        Post post = getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已帖子收藏
        boolean isFavour = false;
        Long loginUserId = UserContext.getUser().getId();
        PostFavour postFavour = postFavourRepository.getFavourRecord(postId, loginUserId);
        if (null == postFavour) {
            // 未帖子收藏，进行收藏
            postFavourRepository.addFavourRecord(postId, loginUserId);
            // 帖子收藏数 + 1
            incrementFavourNum(postId);
            isFavour = true;
        } else {
            // 已帖子收藏，进行取消收藏
            postFavourRepository.removeFavourRecord(postId, loginUserId);
            // 帖子收藏数 - 1
            decrementFavourNum(postId);
        }
        return isFavour;
    }

    /**
     * 点赞
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doThumb(long postId) {
        long userId = UserContext.getUser().getId();
        boolean isThumb = false;
        // 判断帖子是否存在
        Post post = getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 判断是否点赞过
        PostThumb postThumb = postThumbRepository.getThumbPostByUserId(postId, userId);
        if (postThumb == null) {
            // ==> 未点赞，执行点赞
            postThumbRepository.addPostThumb(userId, postId);
            // 点赞数 + 1
            incrementThumbCount(postId);
            isThumb = true;
        }
        else {
            // ==> 已点赞，执行取消点赞
            postThumbRepository.removePostThumb(userId, postId);
            // 点赞数 - 1
            decrementThumbCount(postId);
        }
        return isThumb;
    }

    /**
     * 校验帖子参数
     */
    private void validPost(Post post) {
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (post.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子ID不能为空");
        }
        String title = post.getTitle();
        String content = post.getContent();
        ThrowUtils.throwIf(StringUtils.isBlank(title) || title.length() > 100, ErrorCode.PARAMS_ERROR, "标题不能为空且长度不能超过100");
        ThrowUtils.throwIf(StringUtils.isBlank(content) || content.length() > 2048, ErrorCode.PARAMS_ERROR, "内容不能为空且长度不能超过2048");
    }

}




