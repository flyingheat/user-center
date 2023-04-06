-- auto-generated definition
create table user
(
    id           bigint auto_increment
        primary key,
    username     varchar(256)                       null comment '用户昵称
',
    avatarUrl    varchar(256)                       null comment '头像',
    userAccount  varchar(256)                       null comment '账号',
    gender       tinyint                            null comment '性别',
    userPassword varchar(256)                       not null comment '密码',
    phone        varchar(128)                       null comment '电话',
    email        varchar(512)                       null comment '邮箱',
    userStatus   int      default 0                 null comment '状态 0- 正常',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '修改时间',
    isDelete     tinyint  default 0                 null comment '是否删除  0 - 没有
',
    userRole     int      default 0                 not null comment '用户角色
- 普通用户 0
- 管理员 1',
    planetCode   varchar(512)                       null comment '星球编号'
)
    comment '用户';

