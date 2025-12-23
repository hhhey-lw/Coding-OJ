package com.longoj.top.controller.dto.question;

import com.longoj.top.controller.dto.PageRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 题目
 * @TableName question
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionQueryRequest extends PageRequest implements Serializable {

    /**
     * 搜索关键字：标题
     */
    private String searchKey;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 难度
     */
    private Integer difficulty;

    @Serial
    private static final long serialVersionUID = 1L;

}