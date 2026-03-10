-- 小程序用户表
CREATE TABLE IF NOT EXISTS wx_user (
    id VARCHAR(32) PRIMARY KEY,
    openid VARCHAR(64) NOT NULL UNIQUE,
    unionid VARCHAR(64),
    session_key VARCHAR(128),
    nick_name VARCHAR(128),
    avatar_url VARCHAR(512),
    gender INT DEFAULT 0,
    country VARCHAR(64),
    province VARCHAR(64),
    city VARCHAR(64),
    language VARCHAR(32),
    phone VARCHAR(32),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    last_login_time TIMESTAMP
);

CREATE INDEX idx_wx_user_openid ON wx_user(openid);

COMMENT ON TABLE wx_user IS '小程序用户表';
COMMENT ON COLUMN wx_user.openid IS '微信小程序用户唯一标识';
COMMENT ON COLUMN wx_user.unionid IS '微信开放平台统一标识';
COMMENT ON COLUMN wx_user.session_key IS '微信登录会话密钥';
