# 数据库初始化

-- 创建库
create database if not exists my_db;

-- 切换库
use my_db;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 题目表
create table if not exists question
(
    id          bigint auto_increment comment 'id' primary key,
    title       varchar(512)                       null comment '标题',
    content     text                               null comment '内容',
    tags        varchar(1024)                      null comment '标签列表（json 数组）',
    answer      text null comment '题目答案',
    submitNum   int default 0 comment '题目提交数',
    acceptedNum int default 0 comment '题目通过数',
    judgeCase   text null comment '题目判题用例 (json 数组)',
    judgeConfig text null comment '判题配置 (json 对象)',
    thumbNum   int      default 0                 not null comment '点赞数',
    favourNum  int      default 0                 not null comment '收藏数',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '题目' collate = utf8mb4_unicode_ci;


-- 题目提交表
create table if not exists question_submit
(
    id         bigint auto_increment comment 'id' primary key,
    language varchar(128) not null comment '编程语言',
    code       text                               not null comment '用户代码',
    judgeInfo  text                               null comment '判题信息（json 对象）',
    status     int      default 0                 not null comment '判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）',
    questionId bigint                             not null comment '题目 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_questionId (questionId),
    index idx_userId (userId)
) comment '题目提交' collate = utf8mb4_unicode_ci;


-- 帖子表
create table if not exists post
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(512)                       null comment '标题',
    content    text                               null comment '内容',
    tags       varchar(1024)                      null comment '标签列表（json 数组）',
    thumbNum   int      default 0                 not null comment '点赞数',
    favourNum  int      default 0                 not null comment '收藏数',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '帖子' collate = utf8mb4_unicode_ci;

-- 帖子点赞表（硬删除）
create table if not exists post_thumb
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子点赞';

-- 帖子收藏表（硬删除）
create table if not exists post_favour
(
    id         bigint auto_increment comment 'id' primary key,
    postId     bigint                             not null comment '帖子 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    index idx_postId (postId),
    index idx_userId (userId)
) comment '帖子收藏';


CREATE TABLE comments (
comment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
content TEXT NOT NULL COMMENT '评论内容',
post_id BIGINT NOT NULL COMMENT '评论文章ID',
user_id BIGINT NOT NULL COMMENT '评论用户ID',
parent_id BIGINT DEFAULT NULL COMMENT '父评论ID(回复评论时使用)',
like_count INT DEFAULT 0 COMMENT '点赞数',
status TINYINT DEFAULT 0 COMMENT '状态(0-正常,1-审核中,2-删除)',
create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
INDEX idx_user (user_id),
INDEX idx_post (post_id),
INDEX idx_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';


CREATE TABLE user_check_in (
userId BIGINT NOT NULL COMMENT '用户Id',
yearMonth VARCHAR(7) NOT NULL COMMENT '年月(YYYY-MM)',
bitmap INT NOT NULL COMMENT '签到位图',
createTime DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
updateTime DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
PRIMARY KEY (userId, yearMonth)
) ENGINE=InnoDB COMMENT='用户签到表';

CREATE TABLE `user_submit_summary`  (
`userId` bigint(0) NOT NULL COMMENT '用户Id',
`yearMonthDay` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '年月(YYYY-MM-DD)',
`submitCount` int(0) NOT NULL COMMENT '用户当天的提交的数量',
`acceptCount` int(0) NOT NULL COMMENT '用户当天的提交通过的数量',
`updateTime` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
PRIMARY KEY (`userId`, `yearMonthDay`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户提交汇总表';


CREATE TABLE `mq_local_message` (
`message_id` VARCHAR(128) NOT NULL COMMENT '消息唯一ID',
`exchange_name` VARCHAR(255) NOT NULL COMMENT '目标交换机名称',
`routing_key` VARCHAR(255) NOT NULL COMMENT '目标路由键',
`payload` TEXT COMMENT '消息体内容 (根据实际情况选择 TEXT, JSON, BLOB 等)',
`status` TINYINT NOT NULL DEFAULT 0 COMMENT '消息状态: 0-待发送, 1-发送成功, 2-发送失败待重试, 3-最终失败',
`retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
`error_cause` TEXT COMMENT '上次发送失败原因',
`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
PRIMARY KEY (`message_id`),
INDEX `idx_status_retry_updated` (`status`, `retry_count`, `updated_at`) COMMENT '用于补偿任务查询的索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RabbitMQ 本地消息表 (兜底方案)';


-- 用户通过题目记录表
CREATE TABLE `question_passed`
(
    `id`          bigint                             NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `userId`      bigint                             NOT NULL COMMENT '用户ID',
    `questionId`  bigint                             NOT NULL COMMENT '题目ID',
    `createTime`  datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    `updateTime`  datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`    tinyint  DEFAULT 0                 NOT NULL COMMENT '是否删除(0-未删, 1-已删)',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_userId_questionId` (`userId`, `questionId`),
    KEY `idx_userId` (`userId`)
) ENGINE = InnoDB COMMENT ='用户通过题目记录表';

CREATE TABLE meal (
meal_id INT AUTO_INCREMENT PRIMARY KEY, -- 套餐唯一标识
name VARCHAR(100) NOT NULL,               -- 套餐名称，例如 "Free Plan", "Premium Plan"
description TEXT,                         -- 套餐描述
price DECIMAL(10, 2) NOT NULL DEFAULT 0.00, -- 套餐价格，例如 0.00 表示免费
max_interviews_per_day INT DEFAULT NULL,  -- 每日最大面试次数，例如免费用户每天2次
max_duration_per_interview INT DEFAULT NULL, -- 单次面试最大时长（分钟），例如免费用户每次30分钟
create_time DATETIME DEFAULT CURRENT_TIMESTAMP, -- 创建时间
update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- 更新时间
);

CREATE TABLE user_meal (
user_meal_id INT AUTO_INCREMENT PRIMARY KEY, -- 用户套餐唯一标识
user_id BIGINT NOT NULL,                  -- 用户唯一标识
meal_id INT NOT NULL,                        -- 套餐ID，关联到Package表
start_date DATETIME NOT NULL,                   -- 订阅开始日期
end_date DATETIME,                              -- 订阅结束日期（如果为NULL，表示无限期）
`status` INT NOT NULL DEFAULT 0,   -- 订阅状态，例如 'active':0, 'expired':1, 'canceled':2
create_time DATETIME DEFAULT CURRENT_TIMESTAMP,  -- 创建时间
update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- 更新时间
);

CREATE TABLE user_resume_info (
user_resume_id INT AUTO_INCREMENT PRIMARY KEY, -- 用户简历唯一标识
user_id BIGINT NOT NULL,                       -- 用户唯一标识
position_name VARCHAR(255) NOT NULL,           -- 用户面试岗位名称
responsibilities TEXT,                         -- 用户面试岗位要求（文本）
resume_information TEXT,                        -- 用户简历信息（文本）
create_time DATETIME DEFAULT CURRENT_TIMESTAMP, -- 创建时间
update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP -- 更新时间
);



