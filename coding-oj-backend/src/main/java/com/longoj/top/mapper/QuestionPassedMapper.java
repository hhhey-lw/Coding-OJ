package com.longoj.top.mapper;

import com.longoj.top.model.dto.questionsubmit.UserPassedCountDTO;
import com.longoj.top.model.entity.QuestionPassed;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 韦龙
* @description 针对表【question_passed(用户通过题目记录表)】的数据库操作Mapper
* @createDate 2025-06-18 19:27:18
* @Entity com.longoj.top.model.entity.QuestionPassed
*/
public interface QuestionPassedMapper extends BaseMapper<QuestionPassed> {

}




