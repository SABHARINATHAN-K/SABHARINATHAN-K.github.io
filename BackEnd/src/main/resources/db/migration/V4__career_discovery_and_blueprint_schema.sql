CREATE TABLE IF NOT EXISTS assessment_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_text VARCHAR(1000) NOT NULL,
    stage INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_assessment_questions_stage CHECK (stage IN (1, 2, 3))
);

CREATE TABLE IF NOT EXISTS assessment_options (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    question_id BIGINT NOT NULL,
    option_text VARCHAR(1000) NOT NULL,
    weight_json JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assessment_options_question
        FOREIGN KEY (question_id) REFERENCES assessment_questions(id) ON DELETE CASCADE
);

CREATE INDEX idx_assessment_questions_stage ON assessment_questions(stage);
CREATE INDEX idx_assessment_options_question_id ON assessment_options(question_id);

CREATE TABLE IF NOT EXISTS assessment_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    score_json JSON NOT NULL,
    recommended_track VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assessment_results_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_assessment_results_user_id ON assessment_results(user_id);

CREATE TABLE IF NOT EXISTS career_phases (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    career_track VARCHAR(100) NOT NULL,
    phase_order INT NOT NULL,
    phase_title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    CONSTRAINT uq_career_phase_track_order UNIQUE (career_track, phase_order)
);

CREATE TABLE IF NOT EXISTS career_goal_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    career_track VARCHAR(100) NOT NULL,
    phase_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    category VARCHAR(50) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    default_order INT NOT NULL,
    CONSTRAINT fk_career_goal_templates_phase
        FOREIGN KEY (phase_id) REFERENCES career_phases(id) ON DELETE CASCADE
);

CREATE INDEX idx_career_goal_templates_track ON career_goal_templates(career_track);
CREATE INDEX idx_career_goal_templates_phase_id ON career_goal_templates(phase_id);

ALTER TABLE goals
    ADD COLUMN is_blueprint_goal BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN blueprint_template_id BIGINT NULL,
    ADD COLUMN blueprint_phase_order INT NULL,
    ADD COLUMN blueprint_default_order INT NULL,
    ADD COLUMN blueprint_phase_title VARCHAR(255) NULL,
    ADD CONSTRAINT fk_goals_blueprint_template
        FOREIGN KEY (blueprint_template_id) REFERENCES career_goal_templates(id) ON DELETE SET NULL;

CREATE INDEX idx_goals_blueprint_flags
    ON goals(user_id, is_blueprint_goal, status, blueprint_phase_order, blueprint_default_order);
