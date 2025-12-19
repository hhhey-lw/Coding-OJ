package com.longoj.top.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.longoj.top.model.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

}
