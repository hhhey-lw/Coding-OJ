package com.longoj.top.judge.codesandbox;

import com.longoj.top.judge.codesandbox.service.CodeSandBox;
import com.longoj.top.judge.codesandbox.service.impl.JavaDockerCodeSandBox;
import com.longoj.top.judge.codesandbox.service.impl.JavaNativeCodeSandBox;
import com.longoj.top.judge.codesandbox.service.impl.SampleCodeSandBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


@Component
public class CodeSandboxFactory {

    @Autowired
    private ApplicationContext applicationContext;  // 注入Spring上下文

    public CodeSandBox getCodeSandBox(String type) {
        switch (type) {
            case "native":
                return applicationContext.getBean(JavaNativeCodeSandBox.class);
            case "docker":
                return applicationContext.getBean(JavaDockerCodeSandBox.class);
            default:
                return applicationContext.getBean(SampleCodeSandBox.class);
        }
    }
}
