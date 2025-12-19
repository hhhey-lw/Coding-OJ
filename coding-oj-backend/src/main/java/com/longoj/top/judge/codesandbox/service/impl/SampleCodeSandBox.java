package com.longoj.top.judge.codesandbox.service.impl;

import cn.hutool.core.collection.ListUtil;
import com.longoj.top.judge.codesandbox.service.CodeSandBox;
import com.longoj.top.judge.codesandbox.model.ExecuteCodeRequest;
import com.longoj.top.judge.codesandbox.model.ExecuteCodeResponse;
import com.longoj.top.model.dto.questionsubmit.JudgeInfo;
import org.springframework.stereotype.Component;


@Component
public class SampleCodeSandBox implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return ExecuteCodeResponse.builder()
                .outputList(ListUtil.toList("9", "6", "6"))
                .judgeInfo(JudgeInfo.builder()
                        .memory(1000L)
                        .message("success")
                        .time(1000L)
                        .build())
                .build();
    }
}
