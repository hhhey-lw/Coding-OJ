package com.longoj.top.domain.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longoj.top.domain.entity.Tag;
import com.longoj.top.controller.dto.post.TagVO;
import com.longoj.top.domain.service.TagService;
import com.longoj.top.infrastructure.mapper.TagMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 标签服务
 *
 * @author 韦龙
 * @createDate 2025-06-16 14:50:42
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> batchQueryTagIdByTagName(List<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询已存在的标签
        List<Tag> existingTags = lambdaQuery()
                .select(Tag::getId, Tag::getTagName)
                .in(Tag::getTagName, tagList)
                .eq(Tag::getIsDelete, Boolean.FALSE)
                .list();

        // 构建标签名到ID的映射
        Map<String, Long> tagNameToIdMap = existingTags.stream()
                .collect(Collectors.toMap(Tag::getTagName, Tag::getId));

        // 找出需要新建的标签
        List<Tag> newTags = tagList.stream()
                .filter(tagName -> !tagNameToIdMap.containsKey(tagName))
                .distinct()
                .map(tagName -> {
                    Tag tag = new Tag();
                    tag.setTagName(tagName);
                    tag.setCreateTime(new Date());
                    return tag;
                })
                .toList();

        // 批量保存新标签
        if (!newTags.isEmpty()) {
            saveBatch(newTags);
            // 将新标签加入映射
            newTags.forEach(tag -> tagNameToIdMap.put(tag.getTagName(), tag.getId()));
        }

        // 按原始顺序返回标签ID
        return tagList.stream()
                .map(tagNameToIdMap::get)
                .toList();
    }

    @Override
    public Page<TagVO> getTagBypage(Long current, Long pageSize) {
        Page<Tag> tagPage = page(new Page<>(current, pageSize), lambdaQuery()
                .eq(Tag::getIsDelete, 0)
                .orderByDesc(Tag::getCreateTime)
                .getWrapper());

        Page<TagVO> tagVOPage = new Page<>(tagPage.getCurrent(), tagPage.getSize(), tagPage.getTotal());

        List<TagVO> tagVOList = tagPage.getRecords().stream()
                .map(tag -> {
                    TagVO tagVO = new TagVO();
                    tagVO.setId(tag.getId());
                    tagVO.setTagName(tag.getTagName());
                    return tagVO;
                })
                .toList();
        tagVOPage.setRecords(tagVOList);

        return tagVOPage;
    }
}




