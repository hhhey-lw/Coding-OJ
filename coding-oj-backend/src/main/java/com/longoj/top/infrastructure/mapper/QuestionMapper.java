package com.longoj.top.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.longoj.top.domain.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * question(题目)
 *
* @author 韦龙
* @createDate 2025-05-15 00:13:26
*/
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 更新提交数
     */
    int updateSubmitNum(Long questionId);

    /**
     * 更新通过数
     */
    int updateAcceptedNum(Long questionId);

    /**
     * 根据标签分页查询
     */
    Page<Question> pageByTagIds(@Param("page") Page<Object> objectPage,
                                @Param("searchKey") String searchKey,
                                @Param("difficulty") Integer difficulty,
                                @Param("tagIds") List<Long> tagIds);
}
