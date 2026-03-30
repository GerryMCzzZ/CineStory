-- CineStory Database Schema
-- Version 1.0

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    avatar_url VARCHAR(500),
    bio TEXT,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    quota_total INT DEFAULT 100,
    quota_used INT DEFAULT 0,
    quota_reset_date DATETIME,
    api_key VARCHAR(100) UNIQUE,
    api_key_enabled BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME ON UPDATE CURRENT_TIMESTAMP,
    last_login_at DATETIME,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_api_key (api_key),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================
-- 风格模板表
-- ============================================
CREATE TABLE IF NOT EXISTS style_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '风格模板ID',
    name VARCHAR(100) NOT NULL COMMENT '风格名称',
    name_en VARCHAR(100) COMMENT '英文名称',
    description TEXT COMMENT '风格描述',
    is_system BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否系统预设',
    is_public BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否公开',

    -- 视觉风格配置
    visual_style TEXT COMMENT '视觉风格描述',
    character_style TEXT COMMENT '角色风格描述',
    background_style TEXT COMMENT '背景风格描述',
    negative_prompts TEXT COMMENT '负面提示词',

    -- 镜头和运动偏好
    default_camera VARCHAR(255) COMMENT '默认镜头设置',
    default_motion VARCHAR(255) COMMENT '默认运动描述',

    -- 其他配置
    preview_image_url VARCHAR(512) COMMENT '预览图片URL',
    config_json JSON COMMENT '额外配置(JSON格式)',

    user_id BIGINT COMMENT '创建者ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    INDEX idx_user_id (user_id),
    INDEX idx_is_system (is_system),
    INDEX idx_is_public (is_public),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风格模板表';

-- ============================================
-- API密钥配置表
-- ============================================
CREATE TABLE IF NOT EXISTS api_credentials (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    provider VARCHAR(50) NOT NULL COMMENT 'API提供商: runway, pika, luma, openai等',
    api_key_encrypted TEXT NOT NULL COMMENT '加密的API密钥',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_user_provider (user_id, provider),
    INDEX idx_user_id (user_id),
    INDEX idx_provider (provider),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='API密钥配置表';

-- ============================================
-- 项目表
-- ============================================
CREATE TABLE IF NOT EXISTS projects (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(255) NOT NULL COMMENT '项目名称',
    description TEXT COMMENT '项目描述',
    style_template_id BIGINT COMMENT '使用的风格模板ID',

    -- 小说文本信息
    novel_title VARCHAR(255) COMMENT '小说标题',
    novel_author VARCHAR(255) COMMENT '小说作者',
    novel_content TEXT COMMENT '小说内容',
    total_characters INT COMMENT '总字数',

    -- 项目配置
    config_json JSON COMMENT '项目配置(JSON格式)',

    -- 状态管理
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT, PROCESSING, COMPLETED, FAILED',
    current_step VARCHAR(50) COMMENT '当前步骤',
    progress INT DEFAULT 0 COMMENT '进度(0-100)',

    -- 输出信息
    output_video_url VARCHAR(512) COMMENT '最终视频URL',
    output_video_path VARCHAR(512) COMMENT '最终视频路径',
    total_duration INT COMMENT '总时长(秒)',

    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    started_at TIMESTAMP NULL COMMENT '开始时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',

    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (style_template_id) REFERENCES style_templates(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目表';

-- ============================================
-- 任务表
-- ============================================
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    type VARCHAR(50) NOT NULL COMMENT '任务类型: TEXT_SPLITTING, PROMPT_GENERATION, VIDEO_GENERATION, VIDEO_COMPOSITION',
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING, RUNNING, COMPLETED, FAILED, CANCELLED',

    -- 进度统计
    total_items INT DEFAULT 0 COMMENT '总项目数',
    completed_items INT DEFAULT 0 COMMENT '已完成数',
    failed_items INT DEFAULT 0 COMMENT '失败数',

    -- 错误信息
    error_message TEXT COMMENT '错误消息',
    error_stack TEXT COMMENT '错误堆栈',

    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    started_at TIMESTAMP NULL COMMENT '开始时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',

    INDEX idx_project_id (project_id),
    INDEX idx_status (status),
    INDEX idx_type (type),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务表';

-- ============================================
-- 文本切片表
-- ============================================
CREATE TABLE IF NOT EXISTS text_slices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '切片ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    content TEXT NOT NULL COMMENT '切片内容',
    order_index INT NOT NULL COMMENT '顺序索引',

    -- 切片元数据
    scene_type VARCHAR(50) COMMENT '场景类型: DIALOGUE, DESCRIPTION, ACTION, TRANSITION',
    characters JSON COMMENT '出场人物列表(JSON数组)',
    mood VARCHAR(100) COMMENT '情绪基调',
    location VARCHAR(255) COMMENT '场景地点',
    time_of_day VARCHAR(50) COMMENT '时间段',

    -- 上下文引用
    context_before JSON COMMENT '前置上下文引用(JSON数组)',
    context_after JSON COMMENT '后置上下文引用(JSON数组)',

    -- 统计信息
    character_count INT COMMENT '字符数',
    estimated_duration INT COMMENT '预估时长(秒)',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_project_order (project_id, order_index),
    INDEX idx_project_id (project_id),
    INDEX idx_scene_type (scene_type),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文本切片表';

-- ============================================
-- 提示词表
-- ============================================
CREATE TABLE IF NOT EXISTS video_prompts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提示词ID',
    slice_id BIGINT NOT NULL COMMENT '切片ID',

    -- 提示词内容
    visual_prompt TEXT NOT NULL COMMENT '视觉提示词',
    motion_prompt TEXT COMMENT '运动提示词',
    camera_prompt TEXT COMMENT '镜头提示词',
    atmosphere VARCHAR(255) COMMENT '氛围描述',

    -- 生成参数
    duration INT DEFAULT 5 COMMENT '期望时长(秒)',
    aspect_ratio VARCHAR(20) DEFAULT '16:9' COMMENT '宽高比',

    -- 修改记录
    is_manual BOOLEAN DEFAULT FALSE COMMENT '是否用户手动修改',
    original_prompt TEXT COMMENT '原始生成的提示词',

    -- LLM 调用记录
    llm_provider VARCHAR(50) COMMENT '使用的LLM提供商',
    llm_model VARCHAR(100) COMMENT '使用的LLM模型',
    llm_tokens_used INT COMMENT '消耗的token数',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    UNIQUE KEY uk_slice_id (slice_id),
    INDEX idx_slice_id (slice_id),
    FOREIGN KEY (slice_id) REFERENCES text_slices(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频生成提示词表';

-- ============================================
-- 视频生成记录表
-- ============================================
CREATE TABLE IF NOT EXISTS video_generations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '生成记录ID',
    slice_id BIGINT NOT NULL COMMENT '切片ID',
    prompt_id BIGINT COMMENT '提示词ID',

    -- API 信息
    provider VARCHAR(50) NOT NULL COMMENT '视频生成服务商',
    provider_model VARCHAR(100) COMMENT '使用的模型',
    provider_task_id VARCHAR(255) COMMENT '第三方任务ID',

    -- 状态管理
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING, PROCESSING, COMPLETED, FAILED, TIMEOUT',
    error_message TEXT COMMENT '错误消息',

    -- 视频信息
    video_url VARCHAR(512) COMMENT '视频URL',
    local_path VARCHAR(512) COMMENT '本地存储路径',
    minio_path VARCHAR(512) COMMENT 'MinIO存储路径',
    duration INT COMMENT '视频时长(秒)',
    width INT COMMENT '视频宽度',
    height INT COMMENT '视频高度',
    file_size BIGINT COMMENT '文件大小(字节)',

    -- 重试记录
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    last_retry_at TIMESTAMP NULL COMMENT '最后重试时间',

    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',

    INDEX idx_slice_id (slice_id),
    INDEX idx_status (status),
    INDEX idx_provider_task (provider_task_id),
    INDEX idx_provider (provider),
    FOREIGN KEY (slice_id) REFERENCES text_slices(id) ON DELETE CASCADE,
    FOREIGN KEY (prompt_id) REFERENCES video_prompts(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频生成记录表';

-- ============================================
-- 视频片段表 (用于拼接)
-- ============================================
CREATE TABLE IF NOT EXISTS video_segments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '片段ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    video_generation_id BIGINT COMMENT '视频生成记录ID',

    -- 片段信息
    order_index INT NOT NULL COMMENT '顺序索引',
    video_path VARCHAR(512) NOT NULL COMMENT '视频文件路径',
    duration FLOAT NOT NULL COMMENT '时长(秒)',
    start_time FLOAT DEFAULT 0 COMMENT '开始时间',
    end_time FLOAT COMMENT '结束时间',

    -- 字幕和转场
    subtitle TEXT COMMENT '字幕文本',
    transition_type VARCHAR(50) DEFAULT 'fade' COMMENT '转场类型: fade, dissolve, wipe, none',
    transition_duration FLOAT DEFAULT 1.0 COMMENT '转场时长(秒)',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    UNIQUE KEY uk_project_order (project_id, order_index),
    INDEX idx_project_id (project_id),
    INDEX idx_video_generation_id (video_generation_id),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (video_generation_id) REFERENCES video_generations(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频片段表';

-- ============================================
-- 插入初始数据 - 系统预设风格模板
-- ============================================
INSERT INTO style_templates (name, name_en, description, is_system, is_public, visual_style, character_style, background_style, negative_prompts, default_camera, default_motion) VALUES
('日式动漫风', 'Japanese Anime', '传统日本动漫风格，色彩鲜艳，线条清晰', TRUE, TRUE,
 'Japanese anime style, vibrant colors, clean line art, cel shaded, 2D animation',
 'anime character, big expressive eyes, stylized hair, manga style',
 'hand-drawn background, cel shaded, detailed anime scenery',
 'realistic, photograph, 3D render, live action, film grain',
 'medium shot, dynamic angle, anime cinematography',
 'smooth camera movement, subtle animation, anime-style motion'),

('3D动漫风', '3D Anime', '现代3D动画风格，类似新海诚电影', TRUE, TRUE,
 '3D anime style, Makoto Shinkai inspired, vibrant colors, detailed environments, high quality 3D render',
 '3D anime character, detailed facial features, expressive animation',
 'detailed 3D background, scenic environment, atmospheric lighting',
 '2D, sketch, low quality, blurry, pixelated',
 'cinematic shot, depth of field, dramatic angle',
 'smooth camera pan, subtle motion, cinematic movement'),

('水墨国风', 'Chinese Ink', '中国传统水墨画风格', TRUE, TRUE,
 'Chinese ink painting style, watercolor, brush strokes, traditional art, elegant',
 'stylized character in ink wash painting style, flowing lines',
 'ink wash background, mountains, mist, traditional Chinese landscape',
 'anime, manga, realistic, photograph, 3D render',
 'wide shot, artistic composition',
 'slow movement, gentle motion, flowing like ink'),

('赛博朋克', 'Cyberpunk', '未来科技感风格，霓虹灯光，暗色调', TRUE, TRUE,
 'cyberpunk style, neon lights, dark atmosphere, futuristic, high contrast, rain, city lights',
 'cyberpunk character, glowing elements, tech-wear style',
 'futuristic city, neon signs, dark alley, rain, holograms',
 'bright, cheerful, daytime, natural lighting',
 'dynamic angle, low angle shot, dutch angle',
 'fast movement, glitch effects, dynamic motion'),

('吉卜力风', 'Ghibli Style', '宫崎骏吉卜力工作室风格', TRUE, TRUE,
 'Studio Ghibli style, hand-drawn, watercolor background, soft colors, peaceful atmosphere',
 'Ghibli character style, soft features, expressive eyes, warm colors',
 'detailed hand-painted background, nature, peaceful scenery, lush greenery',
 '3D render, realistic, photograph, dark, gritty',
 'wide shot, peaceful composition',
 'gentle movement, slow motion, calming animation');

-- ============================================
-- 创建默认管理员用户 (测试用，生产环境应删除)
-- ============================================
-- 插入默认管理员用户（密码: admin123，需要 BCrypt 加密）
-- 密码 admin123 的 BCrypt 加密结果
INSERT INTO users (username, email, password, nickname, role, quota_total) VALUES
('admin', 'admin@cinestory.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '管理员', 'ADMIN', 10000)
ON DUPLICATE KEY UPDATE id = id;
