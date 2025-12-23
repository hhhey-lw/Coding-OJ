package com.longoj.top.controller.dto.post;

import cn.hutool.json.JSONUtil;
import com.longoj.top.domain.entity.Post;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.longoj.top.controller.dto.user.UserVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * 帖子视图
 *
 */
@Data
public class PostVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 评论数
     */
    private Integer commentNum;

    /**
     * 浏览数
     */
    private Integer viewNum;

    /**
     * 点赞数
     */
    private Integer thumbNum;

    /**
     * 收藏数
     */
    private Integer favourNum;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 创建人信息
     */
    private UserVO user;

    /**
     * 是否已点赞
     */
    private Boolean isThumb;

    /**
     * 是否已收藏
     */
    private Boolean isFavour;

    /**
     * 对象转换为VO对象
     */
    public static PostVO convertToVo(Post post) {
        if (post == null) {
            return null;
        }
        PostVO postVO = new PostVO();
        postVO.setId(post.getId());
        postVO.setTitle(post.getTitle());
        postVO.setContent(post.getContent());
        postVO.setCommentNum(post.getCommentNum());
        postVO.setViewNum(post.getViewNum());
        postVO.setThumbNum(post.getThumbNum());
        postVO.setFavourNum(post.getFavourNum());
        postVO.setUserId(post.getUserId());
        postVO.setCreateTime(post.getCreateTime());
        postVO.setUpdateTime(post.getUpdateTime());
        // TODO 是否点赞和收藏单独处理
        postVO.setTagList(JSONUtil.toList(post.getTags(), String.class));
        return postVO;
    }
}
