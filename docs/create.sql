-- ============================================================
-- WiFi 二维码小程序 - PostgreSQL 数据库表结构
-- 数据库：PostgreSQL
-- ============================================================

-- ============================================================
-- 1. 用户表（引用 base-project 已有设计，此处保持一致）
-- ============================================================
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

CREATE INDEX IF NOT EXISTS idx_wx_user_openid ON wx_user(openid);

COMMENT ON TABLE wx_user IS '小程序用户表';
COMMENT ON COLUMN wx_user.id IS '用户主键';
COMMENT ON COLUMN wx_user.openid IS '微信小程序用户唯一标识';
COMMENT ON COLUMN wx_user.unionid IS '微信开放平台统一标识';
COMMENT ON COLUMN wx_user.session_key IS '微信登录会话密钥';
COMMENT ON COLUMN wx_user.nick_name IS '用户昵称';
COMMENT ON COLUMN wx_user.avatar_url IS '用户头像URL';

-- ============================================================
-- 2. WiFi 码表
-- ============================================================
CREATE TABLE IF NOT EXISTS wifi_code (
                                         id VARCHAR(32) PRIMARY KEY,
    user_id VARCHAR(32) NOT NULL REFERENCES wx_user(id),
    brand_name VARCHAR(128) DEFAULT '',
    ssid VARCHAR(128) NOT NULL,
    password VARCHAR(256) DEFAULT '',
    auth_type VARCHAR(16) DEFAULT 'WPA',
    yesterday_count INT DEFAULT 0,
    total_count INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_wifi_code_user_id ON wifi_code(user_id);
CREATE INDEX IF NOT EXISTS idx_wifi_code_ssid ON wifi_code(ssid);
CREATE INDEX IF NOT EXISTS idx_wifi_code_brand_name ON wifi_code(brand_name);
CREATE INDEX IF NOT EXISTS idx_wifi_code_create_time ON wifi_code(create_time);

COMMENT ON TABLE wifi_code IS 'WiFi 二维码信息表';
COMMENT ON COLUMN wifi_code.id IS '主键，由应用层生成（如 nanoid/uuid）';
COMMENT ON COLUMN wifi_code.user_id IS '创建用户ID，关联 wx_user.id';
COMMENT ON COLUMN wifi_code.brand_name IS '品牌名称';
COMMENT ON COLUMN wifi_code.ssid IS '网络名称';
COMMENT ON COLUMN wifi_code.password IS 'WiFi 密码';
COMMENT ON COLUMN wifi_code.auth_type IS '认证类型：WPA/WPA2/WEP/nopass';
COMMENT ON COLUMN wifi_code.yesterday_count IS '昨日有效连接次数';
COMMENT ON COLUMN wifi_code.total_count IS '累计有效连接次数';
COMMENT ON COLUMN wifi_code.status IS '状态：1 正常，0 已删除';

-- update_time 自动更新触发器
CREATE OR REPLACE FUNCTION update_wifi_code_modified()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_wifi_code_update ON wifi_code;
CREATE TRIGGER trg_wifi_code_update
    BEFORE UPDATE ON wifi_code
    FOR EACH ROW
    EXECUTE PROCEDURE update_wifi_code_modified();

-- ============================================================
-- 3. 扫码记录表（可选，用于统计有效连接）
-- ============================================================
CREATE TABLE IF NOT EXISTS wifi_scan_log (
                                             id VARCHAR(32) PRIMARY KEY,
    wifi_code_id VARCHAR(32) NOT NULL REFERENCES wifi_code(id),
    user_id VARCHAR(32),
    scan_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip VARCHAR(64),
    device_info TEXT
    );

CREATE INDEX IF NOT EXISTS idx_wifi_scan_log_code_id ON wifi_scan_log(wifi_code_id);
CREATE INDEX IF NOT EXISTS idx_wifi_scan_log_scan_time ON wifi_scan_log(scan_time);

COMMENT ON TABLE wifi_scan_log IS 'WiFi 码扫码记录，用于统计有效连接';
COMMENT ON COLUMN wifi_scan_log.id IS '主键，由应用层生成';
COMMENT ON COLUMN wifi_scan_log.wifi_code_id IS 'WiFi 码ID';
COMMENT ON COLUMN wifi_scan_log.scan_time IS '扫码时间';

-- 上传文件记录表（头像等）
CREATE TABLE IF NOT EXISTS uploaded_file (
                                             id VARCHAR(32) PRIMARY KEY,
    file_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(32),
    biz_type VARCHAR(32),
    original_name VARCHAR(256),
    storage_path VARCHAR(512),
    url VARCHAR(512),
    mime_type VARCHAR(64),
    file_size BIGINT,
    width INT,
    height INT,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE UNIQUE INDEX idx_uploaded_file_file_id ON uploaded_file(file_id);

COMMENT ON TABLE uploaded_file IS '上传文件记录表';
