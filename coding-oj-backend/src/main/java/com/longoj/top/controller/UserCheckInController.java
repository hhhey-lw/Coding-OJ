package com.longoj.top.controller;

import com.longoj.top.common.BaseResponse;
import com.longoj.top.common.ResultUtils;
import com.longoj.top.model.dto.user.UserCheckInQueryRequest;
import com.longoj.top.model.dto.user.UserCheckInUpdateRequest;
import com.longoj.top.model.vo.UserCheckInVO;
import com.longoj.top.service.UserCheckInService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user-check-in")
public class UserCheckInController {

    @Resource
    private UserCheckInService userCheckInService;

    @PostMapping("/update")
    public BaseResponse<Boolean> updateUserCheckInByOneDay(@RequestBody UserCheckInUpdateRequest userCheckInUpdateRequest) {
        return ResultUtils.success(userCheckInService.updateUserCheckInByOneDay(userCheckInUpdateRequest.getUserId(), userCheckInUpdateRequest.getYearMonth(), userCheckInUpdateRequest.getDay()));
    }

    @GetMapping("/info")
    public BaseResponse<UserCheckInVO> getUserCheckInByUserIdAndYearMonth(Long userId, String yearMonth) {
        return ResultUtils.success(userCheckInService.getUserCheckInByUserIdAndYearMonth(userId, yearMonth));
    }
}
