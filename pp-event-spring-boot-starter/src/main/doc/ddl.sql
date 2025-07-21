CREATE TABLE `XIMA_EVENT`
(
    `id`                        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `event_no`                  varchar(50)   DEFAULT NULL,
    `status`                    int(11)       DEFAULT NULL,
    `retried_count`             int(11)       DEFAULT NULL,
    `method_invocation_content` varchar(1000) DEFAULT NULL,
    `create_time`               datetime      DEFAULT NULL,
    `update_time`               datetime      DEFAULT NULL,
    `version`                   int(11)       DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;