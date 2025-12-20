package com.longoj.top.domain.service;

import com.longoj.top.domain.entity.dto.ExecuteCodeRequest;
import com.longoj.top.domain.entity.dto.ExecuteCodeResponse;

public interface CodeSandBox {

    /**
     * 代码执行
     *
     * @param executeCodeRequest 代码执行请求
     * @return 代码执行响应
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);

}
