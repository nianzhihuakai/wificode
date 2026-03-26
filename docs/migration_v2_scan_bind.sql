-- 扫码统计、店主绑定、远期分润表结构升级（在已有库上执行）
-- PostgreSQL

-- wifi_code：店主（绑定后）
ALTER TABLE wifi_code ADD COLUMN IF NOT EXISTS store_owner_id VARCHAR(32);
ALTER TABLE wifi_code DROP CONSTRAINT IF EXISTS fk_wifi_code_store_owner;
ALTER TABLE wifi_code
    ADD CONSTRAINT fk_wifi_code_store_owner
    FOREIGN KEY (store_owner_id) REFERENCES wx_user (id);
CREATE INDEX IF NOT EXISTS idx_wifi_code_store_owner ON wifi_code (store_owner_id);

-- wifi_scan_log：按日去重
ALTER TABLE wifi_scan_log ADD COLUMN IF NOT EXISTS stat_date DATE;
UPDATE wifi_scan_log SET stat_date = scan_time::date WHERE stat_date IS NULL;
ALTER TABLE wifi_scan_log ALTER COLUMN stat_date SET DEFAULT CURRENT_DATE;
COMMENT ON COLUMN wifi_scan_log.user_id IS '访客用户 wx_user.id，未登录为空';
COMMENT ON COLUMN wifi_scan_log.stat_date IS '统计日，用于去重与昨日汇总';

CREATE UNIQUE INDEX IF NOT EXISTS uq_wifi_scan_visitor_day
    ON wifi_scan_log (wifi_code_id, user_id, stat_date)
    WHERE user_id IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_wifi_scan_anon_day
    ON wifi_scan_log (wifi_code_id, ip, stat_date)
    WHERE user_id IS NULL AND ip IS NOT NULL AND ip <> '';

-- 店主绑定票据（邀请码）
CREATE TABLE IF NOT EXISTS wifi_code_bind_ticket (
    id VARCHAR(32) PRIMARY KEY,
    wifi_code_id VARCHAR(32) NOT NULL REFERENCES wifi_code (id),
    code VARCHAR(16) NOT NULL,
    expire_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    used_by_user_id VARCHAR(32) REFERENCES wx_user (id),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_wifi_bind_ticket_code ON wifi_code_bind_ticket (code);
CREATE INDEX IF NOT EXISTS idx_wifi_bind_ticket_wifi_code ON wifi_code_bind_ticket (wifi_code_id);

COMMENT ON TABLE wifi_code_bind_ticket IS 'WiFi 码店主绑定邀请码（一次性核销）';

-- 远期：广告分成规则（占位）
CREATE TABLE IF NOT EXISTS revenue_share_rule (
    id VARCHAR(32) PRIMARY KEY,
    wifi_code_id VARCHAR(32) REFERENCES wifi_code (id),
    platform_rate_bp INT NOT NULL DEFAULT 4000,
    sales_rate_bp INT NOT NULL DEFAULT 3000,
    store_rate_bp INT NOT NULL DEFAULT 3000,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP
);
COMMENT ON TABLE revenue_share_rule IS '广告分成比例（万分比 bp，和为 10000）；wifi_code_id 空表示全局默认';

-- 远期：广告收益台账（占位）
CREATE TABLE IF NOT EXISTS ad_revenue_ledger (
    id VARCHAR(32) PRIMARY KEY,
    wifi_code_id VARCHAR(32) NOT NULL REFERENCES wifi_code (id),
    period VARCHAR(16) NOT NULL,
    source_ref VARCHAR(128),
    amount_cent BIGINT NOT NULL DEFAULT 0,
    platform_cent BIGINT NOT NULL DEFAULT 0,
    sales_cent BIGINT NOT NULL DEFAULT 0,
    store_cent BIGINT NOT NULL DEFAULT 0,
    status INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_ad_revenue_ledger_code ON ad_revenue_ledger (wifi_code_id);
COMMENT ON TABLE ad_revenue_ledger IS '广告收益拆分流水，对接微信流量主结算后写入';
