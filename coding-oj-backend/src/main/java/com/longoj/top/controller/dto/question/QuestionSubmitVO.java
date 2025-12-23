package com.longoj.top.controller.dto.question;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.longoj.top.controller.dto.user.UserVO;
import com.longoj.top.domain.entity.dto.JudgeInfo;
import com.longoj.top.domain.entity.QuestionSubmit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 题目提交
 * @TableName question_submit
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionSubmitVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 判题信息（json 对象）
     */
    private JudgeInfo judgeInfo;

    /**
     * 判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）
     */
    private Integer status;

    /**
     * 提交用户信息
     */
    private UserVO userVO;

    /**
     * 提交题目信息
     */
    private QuestionVO questionVO;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 实体对象转换为 VO 对象
     */
    public static QuestionSubmitVO convertToVO(QuestionSubmit questionSubmit) {
        if (questionSubmit == null) {
            return null;
        }
        QuestionSubmitVO questionSubmitVO = new QuestionSubmitVO();
        questionSubmitVO.setId(questionSubmit.getId());
        questionSubmitVO.setLanguage(questionSubmit.getLanguage());
        questionSubmitVO.setCode(questionSubmit.getCode());
        questionSubmitVO.setStatus(questionSubmit.getStatus());
        questionSubmitVO.setCreateTime(questionSubmit.getCreateTime());
        questionSubmitVO.setUpdateTime(questionSubmit.getUpdateTime());

        questionSubmitVO.setJudgeInfo(JSONUtil.toBean(questionSubmit.getJudgeInfo(), JudgeInfo.class));
        return  questionSubmitVO;
    }
}