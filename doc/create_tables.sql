DROP TABLE IF EXISTS RESOURCE_LOCK;
CREATE TABLE RESOURCE_LOCK(
    ID                       BIGINT         AUTO_INCREMENT                                              COMMENT 'ID',
    LOCK_KEY                 VARCHAR(64)    NOT NULL                                                    COMMENT '锁KEY',
    LOCK_NAME                VARCHAR(64)    NOT NULL                                                    COMMENT '锁名称',
    UPDATE_TIME              TIMESTAMP      DEFAULT CURRENT_TIMESTAMP                                   COMMENT '修改时间',
    EXPIRED_TIME             TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP       COMMENT '锁到期时间',
    CONSTRAINT UIDX_LOCK_KEY UNIQUE (LOCK_KEY),
    PRIMARY KEY(ID)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8 COLLATE=utf8_general_ci;