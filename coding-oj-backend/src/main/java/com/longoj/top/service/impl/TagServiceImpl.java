package com.longoj.top.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.longoj.top.model.entity.Tag;
import com.longoj.top.model.vo.QuestionVO;
import com.longoj.top.model.vo.TagVO;
import com.longoj.top.service.TagService;
import com.longoj.top.mapper.TagMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @author 韦龙
* @description 针对表【tag(标签表)】的数据库操作Service实现
* @createDate 2025-06-16 14:50:42
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

    @Override
    @Transactional(propagation =  Propagation.MANDATORY)
    public List<Long> queryTagIdByTagNameBatch(List<String> tagList) {
        List<Long> tagIds = new ArrayList<>();
        for (String tagName : tagList) {
            Tag tag = lambdaQuery().select(Tag::getId)
                    .eq(Tag::getTagName, tagName)
                    .eq(Tag::getIsDelete, 0) // 确保只获取未删除的标签
                    .one();
            if (tag == null) {
                tag = new Tag();
                tag.setTagName(tagName);
                tag.setCreateTime(new Date());
                save(tag);
            }
            tagIds.add(tag.getId());
        }
        return tagIds;
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




