package com.longoj.top.judge.codesandbox;

import com.longoj.top.judge.codesandbox.model.JudgeContext;
import com.longoj.top.judge.codesandbox.strategy.JudgeStrategy;
import com.longoj.top.judge.codesandbox.strategy.impl.DefaultJudgeStrategy;
import com.longoj.top.model.dto.questionsubmit.JudgeInfo;
import com.longoj.top.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

@Service
public class JudgeManager {

    public JudgeInfo doJudge(JudgeContext judgeContext) {

        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        // if ("java".equals(language)) {
        //     judgeStrategy = new JavaLanguageJudgeStrategy();
        // }
        return judgeStrategy.doJudge(judgeContext);

    }
}
