package com.longoj.top.controller.dto.post;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import cn.hutool.json.JSONUtil;
import com.longoj.top.domain.entity.Post;
import lombok.Data;

/**
 * 更新请求
 */
@Data
public class PostUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

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
     * 转换为实体对象
     *
     * @param postUpdateRequest 更新请求
     * @return 实体对象
     */
    public Post toEntity(PostUpdateRequest postUpdateRequest) {
        Post post = new Post();
        post.setId(postUpdateRequest.getId());
        post.setTitle(postUpdateRequest.getTitle());
        post.setContent(postUpdateRequest.getContent());
        post.setTags(JSONUtil.toJsonStr(postUpdateRequest.getTags()));
        return post;
    }
}