CREATE TABLE `USER`
(
    `id`       bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
    `number`   bigint(20)  DEFAULT NULL COMMENT '编号',
    `name`     varchar(30) DEFAULT NULL COMMENT '姓名',
    `age`      int(11)     DEFAULT NULL COMMENT '年龄',
    `sex`      int(11)     DEFAULT NULL COMMENT '性别',
    `join_time` datetime    DEFAULT NULL COMMENT '加入时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;