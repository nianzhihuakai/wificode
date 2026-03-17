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
