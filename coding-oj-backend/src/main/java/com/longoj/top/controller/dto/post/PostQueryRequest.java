package com.longoj.top.controller.dto.post;

import com.longoj.top.controller.dto.PageRequest;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostQueryRequest extends PageRequest implements Serializable {
    /**
     * 搜索词
     */
    private String searchKey;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 收藏用户 id
     */
    private Long favourUserId;

    @Serial
    private static final long serialVersionUID = 1L;
}