package com.longoj.top.controller.dto.post;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import cn.hutool.json.JSONUtil;
import com.longoj.top.domain.entity.Post;
import com.longoj.top.infrastructure.utils.UserContext;
import lombok.Data;

/**
 * 创建请求
 *
 */
@Data
public class PostAddRequest implements Serializable {

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 转换为实体
     */
    public static Post toEntity(PostAddRequest postAddRequest) {
        Post post = new Post();
        post.setTitle(postAddRequest.getTitle());
        post.setContent(postAddRequest.getContent());
        post.setTags(JSONUtil.toJsonStr(postAddRequest.getTags()));
        post.setCommentNum(0);
        post.setFavourNum(0);
        post.setViewNum(0);
        post.setThumbNum(0);
        post.setIsDelete(0);
        post.setUserId(UserContext.getUser().getId());
        Date now = new Date();
        post.setCreateTime(now);
        post.setUpdateTime(now);
        return post;
    }
}