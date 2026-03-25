CREATE TABLE IF NOT EXISTS technical_assessment_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    career_track VARCHAR(100) NOT NULL,
    proficiency_level VARCHAR(50) NOT NULL,
    total_score INT NOT NULL,
    max_score INT NOT NULL,
    percentage_score INT NOT NULL,
    skill_area_json JSON NOT NULL,
    summary VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_technical_assessment_results_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_technical_assessment_results_user_track_created
    ON technical_assessment_results(user_id, career_track, created_at DESC);
