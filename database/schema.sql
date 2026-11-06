-- ====================================================================
-- ZERO TO HUNDRED'S HUNT - DATABASE SCHEMA (MySQL 8)
-- ====================================================================

CREATE DATABASE IF NOT EXISTS zero_to_hundreds_hunt
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE zero_to_hundreds_hunt;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    username VARCHAR(60) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    bio TEXT NULL,
    role VARCHAR(50) DEFAULT 'FILMMAKER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Password Reset Tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reset_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Screenplay Projects
CREATE TABLE IF NOT EXISTS screenplay_projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    genre VARCHAR(100),
    story_concept TEXT,
    logline TEXT,
    synopsis TEXT,
    theme VARCHAR(255),
    central_conflict TEXT,
    structure_type VARCHAR(50) DEFAULT 'THREE_ACT',
    status VARCHAR(50) DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_screenplay_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_screenplay_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Story Structures (Stores stages/beats for a screenplay)
CREATE TABLE IF NOT EXISTS story_structures (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    screenplay_id BIGINT NOT NULL,
    structure_name VARCHAR(100) NOT NULL,
    stage_name VARCHAR(120) NOT NULL,
    stage_order INT NOT NULL,
    description TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_structure_screenplay FOREIGN KEY (screenplay_id) REFERENCES screenplay_projects(id) ON DELETE CASCADE,
    INDEX idx_structure_screenplay (screenplay_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Characters
CREATE TABLE IF NOT EXISTS characters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    screenplay_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    role_type VARCHAR(50) DEFAULT 'MAIN',
    age VARCHAR(50),
    background TEXT,
    personality TEXT,
    motivation TEXT,
    goals TEXT,
    strengths TEXT,
    weaknesses TEXT,
    internal_conflict TEXT,
    external_conflict TEXT,
    character_arc TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_char_screenplay FOREIGN KEY (screenplay_id) REFERENCES screenplay_projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_char_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_char_screenplay (screenplay_id),
    INDEX idx_char_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Scenes
CREATE TABLE IF NOT EXISTS scenes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    screenplay_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    scene_number INT NOT NULL,
    heading_type VARCHAR(10) DEFAULT 'INT.',
    location VARCHAR(150) NOT NULL,
    time_of_day VARCHAR(50) DEFAULT 'DAY',
    scene_heading VARCHAR(255) NOT NULL,
    description TEXT,
    character_appearances TEXT,
    actions TEXT,
    dialogue_content MEDIUMTEXT,
    emotional_purpose TEXT,
    scene_objective TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_scene_screenplay FOREIGN KEY (screenplay_id) REFERENCES screenplay_projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_scene_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_scene_screenplay (screenplay_id),
    INDEX idx_scene_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Screenplay Drafts (Full Script Editor content)
CREATE TABLE IF NOT EXISTS screenplay_drafts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    screenplay_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    raw_content MEDIUMTEXT,
    page_count INT DEFAULT 1,
    last_saved TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_draft_screenplay FOREIGN KEY (screenplay_id) REFERENCES screenplay_projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_draft_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. AI Conversations
CREATE TABLE IF NOT EXISTS ai_conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    screenplay_id BIGINT NULL,
    title VARCHAR(200) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_conv_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_conv_screenplay FOREIGN KEY (screenplay_id) REFERENCES screenplay_projects(id) ON DELETE SET NULL,
    INDEX idx_ai_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. AI Messages
CREATE TABLE IF NOT EXISTS ai_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    sender_type VARCHAR(20) NOT NULL, -- 'USER' or 'ASSISTANT'
    content MEDIUMTEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_msg_conv FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id) ON DELETE CASCADE,
    INDEX idx_ai_msg_conv (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Production Projects
CREATE TABLE IF NOT EXISTS production_projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    screenplay_id BIGINT NULL,
    title VARCHAR(200) NOT NULL,
    stage VARCHAR(50) DEFAULT 'DEVELOPMENT', -- DEVELOPMENT, PRE_PRODUCTION, PRODUCTION, POST_PRODUCTION, COMPLETED
    description TEXT,
    director VARCHAR(100),
    producer VARCHAR(100),
    target_completion_date DATE,
    progress_percentage INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_prod_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_prod_screenplay FOREIGN KEY (screenplay_id) REFERENCES screenplay_projects(id) ON DELETE SET NULL,
    INDEX idx_prod_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Production Tasks
CREATE TABLE IF NOT EXISTS production_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    stage VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED
    due_date DATE,
    priority VARCHAR(30) DEFAULT 'MEDIUM',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES production_projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_task_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_task_proj (project_id),
    INDEX idx_task_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Cinematic Library Entries
CREATE TABLE IF NOT EXISTS cinematic_library_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(80) NOT NULL, -- CINEMATOGRAPHY, EDITING, SOUND_DESIGN, LIGHTING, SHOT_COMPOSITION, DIRECTORS_NOTES, LESSONS
    film_reference VARCHAR(150),
    description TEXT,
    personal_observations MEDIUMTEXT NOT NULL,
    tags VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_lib_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_lib_user (user_id),
    INDEX idx_lib_cat (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Creative Vault Ideas
CREATE TABLE IF NOT EXISTS creative_ideas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(80) NOT NULL, -- RAW_STORY, ONE_LINER, LOGLINE, CHARACTER_IDEA, PLOT_TWIST, SCENE_IDEA, DIALOGUE, TITLE, EXPERIMENTAL
    content MEDIUMTEXT NOT NULL,
    tags VARCHAR(255),
    is_favourite BOOLEAN DEFAULT FALSE,
    converted_to_screenplay_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_vault_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_vault_screenplay FOREIGN KEY (converted_to_screenplay_id) REFERENCES screenplay_projects(id) ON DELETE SET NULL,
    INDEX idx_vault_user (user_id),
    INDEX idx_vault_fav (is_favourite)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
