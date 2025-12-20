package com.longoj.top.infrastructure.mapper;

import com.longoj.top.domain.entity.dto.UserPassedCountDTO;
import com.longoj.top.domain.entity.UserSubmitSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserSubmitSummaryMapper {

    UserSubmitSummary querySubmitSummary(Long userId, String yyyyMMDD);

    boolean updateSubmitSummary(Long userId, String yyyyMMDD, Integer submitCount, Integer acceptedCount);

    boolean addSubmitSummary(Long userId, String yyyyMMDD);

    List<UserPassedCountDTO> selectUserPassedCountsByTopNumber(@Param("topNumber") Integer topNumber);
}
