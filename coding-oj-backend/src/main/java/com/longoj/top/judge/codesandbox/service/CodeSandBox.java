package com.longoj.top.judge.codesandbox.service;

import com.longoj.top.judge.codesandbox.model.ExecuteCodeRequest;
import com.longoj.top.judge.codesandbox.model.ExecuteCodeResponse;

public interface CodeSandBox {

    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);

}
