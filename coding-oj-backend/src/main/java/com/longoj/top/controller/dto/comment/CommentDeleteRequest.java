package com.longoj.top.controller.dto.comment;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommentDeleteRequest implements Serializable {
    private Long commentId;
}
