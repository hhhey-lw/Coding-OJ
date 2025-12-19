package com.longoj.top.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.longoj.top.model.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 韦龙
* @description 针对表【question(题目)】的数据库操作Mapper
* @createDate 2025-05-15 00:13:26
* @Entity com.longcoding.springbootinit.model.entity.Question
*/
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    int updateSubmitNum(Long questionId);

    int updateAcceptedNum(Long questionId);

}
