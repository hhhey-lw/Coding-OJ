package com.longoj.top.judge.codesandbox.model;

import com.longoj.top.model.dto.question.JudgeCase;
import com.longoj.top.model.dto.questionsubmit.JudgeInfo;
import com.longoj.top.model.entity.Question;
import com.longoj.top.model.entity.QuestionSubmit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 上下文（用于定义在策略中传递的参数）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JudgeContext {

    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCaseList;

    private Question question;

    private QuestionSubmit questionSubmit;

}
