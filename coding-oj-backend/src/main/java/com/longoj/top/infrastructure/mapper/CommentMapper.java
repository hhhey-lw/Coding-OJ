package com.longoj.top.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.longoj.top.domain.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * comment(评论)
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

}
