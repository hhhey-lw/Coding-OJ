package com.longoj.top.job.cycle;

import com.longoj.top.judge.codesandbox.service.JudgeService;
import com.longoj.top.model.dto.questionsubmit.JudgeInfo;
import com.longoj.top.model.entity.QuestionSubmit;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.concurrent.*;

@Slf4j
// @Component
public class JudgeExecutor {

    private static final Integer QUEUE_CAPACITY = 128;

    private ThreadPoolExecutor THREAD_POOL_EXECUTOR;

    @Resource
    private JudgeService judgeService;

    public JudgeExecutor() {
        THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
                1, 1, 0L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(QUEUE_CAPACITY, true), new DefaultThreadFactory("HandleSubmitFactory"), new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public void pushTaskQueue(QuestionSubmit questionSubmit) {
        try {
            // log.debug("提交任务: {}", questionSubmit.getId());
            THREAD_POOL_EXECUTOR.submit(() -> submitQuestion(questionSubmit));
        } catch (RejectedExecutionException e) {
            log.error("任务被拒绝", e);
        }
    }

    public void submitQuestion(QuestionSubmit questionSubmit) {
        try {
            // log.debug("执行开始！questionId={}", questionSubmit.getId());
            JudgeInfo judgeInfo = judgeService.doJudge(questionSubmit);
            // log.debug("执行完成: {}", JSONUtil.toJsonStr(judgeInfo));
        } catch (Exception e) {
            log.error("判题异常", e);
        }
    }
}
