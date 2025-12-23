package com.longoj.top.domain.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.domain.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.controller.dto.post.TagVO;

import java.util.List;

/**
 * 标签表服务
 *
* @author 韦龙
* @createDate 2025-06-16 14:50:42
*/
public interface TagService extends IService<Tag> {

    /**
     * 根据标签名称批量查询标签ID列表
     *
     * @param tagList 标签名称列表
     * @return 标签ID列表
     */
    List<Long> batchQueryTagIdByTagName(List<String> tagList);

    /**
     * 分页获取标签列表
     *
     * @param current  当前页
     * @param pageSize 每页大小
     * @return 标签分页列表
     */
    Page<TagVO> getTagBypage(Long current, Long pageSize);
}
