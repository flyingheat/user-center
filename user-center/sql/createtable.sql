-- auto-generated definition
create table user
(
    id           bigint auto_increment
        primary key,
    username     varchar(256)                       null comment '用户昵称',
    avatarUrl    varchar(256)                       null comment '头像',
    userAccount  varchar(256)                       null comment '账号',
    gender       tinyint                            null comment '性别',
    userPassword varchar(256)                       not null comment '密码',
    phone        varchar(128)                       null comment '电话',
    email        varchar(512)                       null comment '邮箱',
    userStatus   int      default 0                 null comment '状态 0- 正常',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '修改时间',
    isDelete     tinyint  default 0                 null comment '是否删除  0 - 没有',
    userRole     int      default 0                 not null comment '用户角色- 普通用户 0 - 管理员 1',
    planetCode   varchar(512)                       null comment '星球编号',
    tags         varchar(1024)                      null comment '标签列表 json'
)
    comment '用户';


alter table user add  COLUMN tags varchar(1024)null comment'标签列表';

-- auto-generated definition
create table tag
(
    id         bigint auto_increment comment 'id'
        primary key,
    tagName    varchar(256)                       null comment '标签名称',
    userId     bigint                             null comment '用户id',
    parentId   bigint                             null comment '父标签id',
    isParent   tinyint                            null comment '0 - 不是  1 - 父标签',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '修改时间',
    isDelete   tinyint  default 0                 null comment '是否删除  0 - 没有',
    constraint unique_tagName
        unique (tagName)
)
    comment '标签';

create index idx_userId
    on tag (userId);



-- auto-generated definition
create table team
(
    id           bigint auto_increment primary key,
    name     varchar(256)                 not  null comment '队伍名称',
    description    varchar(1024)                       null comment '描述',
    maxNum  			int    default 1                 null comment '最大人数',
    userId       bigint                           not null comment '创建人id',
    expireTime   datetime                         null comment '过期时间',
    password    varchar(256)                        null comment '密码',
    status      int      default 0                 null comment '状态  0-公开 ， 1-私有，2-加密',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '修改时间',
    isDelete     tinyint  default 0                 null comment '是否删除  0 - 没有'

)
    comment '队伍';

-- auto-generated definition
create table user_team
(
    id           bigint auto_increment
        primary key,
    userId       bigint                           not null comment '用户Id',
    teamId       bigint                           not null comment '队伍Id',
    joinTime   datetime                         null comment '加入时间',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '修改时间',
    isDelete     tinyint  default 0                 null comment '是否删除  0 - 没有'

)
    comment '用户队伍关系';




