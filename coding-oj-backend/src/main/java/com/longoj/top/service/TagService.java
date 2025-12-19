package com.longoj.top.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.model.entity.Tag;
import com.baomidou.mybatisplus.extension.service.IService;
import com.longoj.top.model.vo.QuestionVO;
import com.longoj.top.model.vo.TagVO;

import java.util.List;

/**
* @author 韦龙
* @description 针对表【tag(标签表)】的数据库操作Service
* @createDate 2025-06-16 14:50:42
*/
public interface TagService extends IService<Tag> {

    List<Long> queryTagIdByTagNameBatch(List<String> tagList);

    Page<TagVO> getTagBypage(Long current, Long pageSize);
}
