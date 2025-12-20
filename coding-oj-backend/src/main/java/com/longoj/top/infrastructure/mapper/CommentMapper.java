package com.longoj.top.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.longoj.top.domain.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

}
