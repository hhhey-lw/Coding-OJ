package com.longoj.top.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.longoj.top.domain.entity.QuestionSubmit;
import com.longoj.top.domain.entity.dto.UserPassedCountDTO;
import com.longoj.top.controller.dto.user.UserSubmitSummaryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * question_submit(题目提交)
 *
 * @author 韦龙
 * @createDate 2025-05-15 00:13:26
 */
@Mapper
public interface QuestionSubmitMapper extends BaseMapper<QuestionSubmit> {

    /**
     * 统计用户通过题目数和提交数排行榜（从 question_submit 表直接统计）
     *
     * @param topNumber 排行榜前N名
     * @return 用户通过数统计列表
     */
    List<UserPassedCountDTO> selectUserPassedCountsByTopNumber(@Param("topNumber") Integer topNumber);

    /**
     * 查询用户某月每天的提交统计（用于 Redis 降级）
     *
     * @param userId    用户ID
     * @param yearMonth 年月，格式 yyyy-MM
     * @return 每日提交统计列表
     */
    List<UserSubmitSummaryVO> selectDailySubmitSummary(@Param("userId") Long userId, @Param("yearMonth") String yearMonth);

    /**
     * 查询用户某月有提交记录的日期列表（用于生成签到 bitmap）
     *
     * @param userId    用户ID
     * @param yearMonth 年月，格式 yyyy-MM
     * @return 有提交的日期列表（1-31）
     */
    List<Integer> selectSubmitDays(@Param("userId") Long userId, @Param("yearMonth") String yearMonth);

}
