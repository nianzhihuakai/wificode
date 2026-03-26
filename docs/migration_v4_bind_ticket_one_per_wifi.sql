-- 绑定票据改为「一 WiFi 码一行」：去重后唯一约束 wifi_code_id

DELETE FROM wifi_code_bind_ticket
WHERE id NOT IN (
    SELECT id FROM (
        SELECT DISTINCT ON (wifi_code_id) id
        FROM wifi_code_bind_ticket
        ORDER BY wifi_code_id, create_time DESC NULLS LAST, id DESC
    ) sub
);

DROP INDEX IF EXISTS idx_wifi_bind_ticket_wifi_code;
CREATE UNIQUE INDEX IF NOT EXISTS uq_wifi_bind_ticket_wifi_code_id ON wifi_code_bind_ticket(wifi_code_id);

COMMENT ON TABLE wifi_code_bind_ticket IS '店主绑定邀请码：每个 WiFi 码仅一行，重复生成则更新 code/expire_at 并清空核销状态';
