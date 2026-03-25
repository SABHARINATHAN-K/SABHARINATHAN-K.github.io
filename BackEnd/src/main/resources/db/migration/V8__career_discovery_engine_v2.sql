ALTER TABLE assessment_questions
    ADD COLUMN question_key VARCHAR(120) NULL,
    ADD COLUMN cluster_key VARCHAR(120) NULL,
    ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_assessment_questions_active_stage ON assessment_questions(is_active, stage);
CREATE INDEX idx_assessment_questions_cluster_key ON assessment_questions(cluster_key);

UPDATE assessment_questions
SET is_active = FALSE;

INSERT INTO assessment_questions (id, question_text, stage, question_key, cluster_key, is_active) VALUES
    (201, 'Q1 - Problem Solving Style: Which approach sounds most like you?', 1, 'STAGE1_Q1', NULL, TRUE),
    (202, 'Q2 - Preferred Work Nature: What type of work energizes you most?', 1, 'STAGE1_Q2', NULL, TRUE),
    (203, 'Q3 - Motivation Type: What motivates your best work?', 1, 'STAGE1_Q3', NULL, TRUE),
    (204, 'Q4 - Team Role: Which role do you naturally take in a team?', 1, 'STAGE1_Q4', NULL, TRUE),
    (205, 'Q5 - Long-Term Vision: Which future sounds most meaningful to you?', 1, 'STAGE1_Q5', NULL, TRUE),
    (206, 'Q6 - Decision Approach: What is your default decision style?', 1, 'STAGE1_Q6', NULL, TRUE),

    (207, 'Math/Logic Comfort (1 to 5)', 2, 'STAGE2_Q1', NULL, TRUE),
    (208, 'Coding Comfort (1 to 5)', 2, 'STAGE2_Q2', NULL, TRUE),
    (209, 'Debug Tolerance (1 to 5)', 2, 'STAGE2_Q3', NULL, TRUE),
    (210, 'Communication Confidence (1 to 5)', 2, 'STAGE2_Q4', NULL, TRUE),
    (211, 'Creative Confidence (1 to 5)', 2, 'STAGE2_Q5', NULL, TRUE),
    (212, 'Risk & Security Awareness (1 to 5)', 2, 'STAGE2_Q6', NULL, TRUE),

    (213, 'Scenario: A key feature is failing right before release. What would you most likely do first?', 3, 'STAGE3_ENGINEERING', 'ENGINEERING_CLUSTER', TRUE),
    (214, 'Scenario: Leadership needs guidance from messy business data within 24 hours. Your preferred approach?', 3, 'STAGE3_DATA', 'DATA_CLUSTER', TRUE),
    (215, 'Scenario: A product metric is dropping every week. Where do you jump in?', 3, 'STAGE3_PRODUCT_BUSINESS', 'PRODUCT_BUSINESS_CLUSTER', TRUE),
    (216, 'Scenario: User feedback says the app feels confusing. What do you do?', 3, 'STAGE3_DESIGN', 'DESIGN_CLUSTER', TRUE),
    (217, 'Scenario: A suspicious security alert appears in production logs. What action sounds right?', 3, 'STAGE3_INFRA_SECURITY', 'INFRA_SECURITY_CLUSTER', TRUE);

INSERT INTO assessment_options (id, question_id, option_text, weight_json) VALUES
    (2001, 201, 'Break down technical problems step by step and solve with logic.', '{"LOGICAL":4,"ANALYTICAL":3,"DETAIL_ORIENTED":2}'),
    (2002, 201, 'Evaluate patterns from data before choosing a direction.', '{"ANALYTICAL":4,"LOGICAL":3,"DETAIL_ORIENTED":2}'),
    (2003, 201, 'Explore multiple creative directions and iterate quickly.', '{"CREATIVE":4,"STRATEGIC":1,"COMMUNICATION":1}'),
    (2004, 201, 'Align people around priorities and decision trade-offs.', '{"COMMUNICATION":3,"STRATEGIC":3}'),

    (2005, 202, 'Building applications, systems, and features end-to-end.', '{"LOGICAL":3,"DETAIL_ORIENTED":2,"OPERATIONS_ORIENTED":2}'),
    (2006, 202, 'Analyzing data, trends, and root causes for decisions.', '{"ANALYTICAL":4,"DETAIL_ORIENTED":2,"STRATEGIC":1}'),
    (2007, 202, 'Designing interfaces, flows, and user experiences.', '{"CREATIVE":4,"COMMUNICATION":2,"DETAIL_ORIENTED":1}'),
    (2008, 202, 'Improving reliability, deployment, and security posture.', '{"OPERATIONS_ORIENTED":3,"SECURITY_ORIENTED":3,"DETAIL_ORIENTED":2}'),

    (2009, 203, 'Solving hard technical challenges with measurable outcomes.', '{"LOGICAL":3,"DETAIL_ORIENTED":2,"OPERATIONS_ORIENTED":1}'),
    (2010, 203, 'Turning data into insight that changes decisions.', '{"ANALYTICAL":4,"STRATEGIC":2,"COMMUNICATION":1}'),
    (2011, 203, 'Driving product direction and influencing stakeholders.', '{"COMMUNICATION":3,"STRATEGIC":4,"ANALYTICAL":1}'),
    (2012, 203, 'Crafting intuitive experiences that delight users.', '{"CREATIVE":4,"COMMUNICATION":2}'),

    (2013, 204, 'Hands-on implementer who executes complex tasks deeply.', '{"LOGICAL":3,"DETAIL_ORIENTED":3}'),
    (2014, 204, 'Insight translator who explains findings clearly.', '{"ANALYTICAL":4,"COMMUNICATION":2}'),
    (2015, 204, 'Coordinator who aligns teams and plans strategy.', '{"COMMUNICATION":3,"STRATEGIC":4}'),
    (2016, 204, 'Quality/risk guardian who spots flaws early.', '{"DETAIL_ORIENTED":3,"SECURITY_ORIENTED":3,"OPERATIONS_ORIENTED":2}'),

    (2017, 205, 'Building scalable software and technical systems.', '{"LOGICAL":4,"OPERATIONS_ORIENTED":2,"DETAIL_ORIENTED":2}'),
    (2018, 205, 'Building data-driven or intelligent solutions.', '{"ANALYTICAL":4,"LOGICAL":2,"STRATEGIC":1}'),
    (2019, 205, 'Leading product or business outcomes across teams.', '{"STRATEGIC":4,"COMMUNICATION":3,"ANALYTICAL":1}'),
    (2020, 205, 'Designing meaningful, human-centered experiences.', '{"CREATIVE":4,"COMMUNICATION":2,"STRATEGIC":1}'),

    (2021, 206, 'Evidence-first: compare options through data and experiments.', '{"ANALYTICAL":3,"DETAIL_ORIENTED":2,"STRATEGIC":1}'),
    (2022, 206, 'Feasibility-first: optimize architecture and implementation.', '{"LOGICAL":3,"OPERATIONS_ORIENTED":2,"DETAIL_ORIENTED":2}'),
    (2023, 206, 'User-first: prioritize empathy and usability impact.', '{"CREATIVE":3,"COMMUNICATION":2,"STRATEGIC":1}'),
    (2024, 206, 'Risk-first: secure and stabilize before scaling.', '{"SECURITY_ORIENTED":3,"OPERATIONS_ORIENTED":3,"DETAIL_ORIENTED":2}'),

    (2025, 207, '1 - Very low', '{"LOGICAL":1,"ANALYTICAL":1}'),
    (2026, 207, '2 - Low', '{"LOGICAL":2,"ANALYTICAL":2}'),
    (2027, 207, '3 - Moderate', '{"LOGICAL":3,"ANALYTICAL":3}'),
    (2028, 207, '4 - High', '{"LOGICAL":4,"ANALYTICAL":4}'),
    (2029, 207, '5 - Very high', '{"LOGICAL":5,"ANALYTICAL":5}'),

    (2030, 208, '1 - Very low', '{"LOGICAL":1,"DETAIL_ORIENTED":1,"OPERATIONS_ORIENTED":1}'),
    (2031, 208, '2 - Low', '{"LOGICAL":2,"DETAIL_ORIENTED":1,"OPERATIONS_ORIENTED":1}'),
    (2032, 208, '3 - Moderate', '{"LOGICAL":3,"DETAIL_ORIENTED":2,"OPERATIONS_ORIENTED":2}'),
    (2033, 208, '4 - High', '{"LOGICAL":4,"DETAIL_ORIENTED":3,"OPERATIONS_ORIENTED":3}'),
    (2034, 208, '5 - Very high', '{"LOGICAL":5,"DETAIL_ORIENTED":4,"OPERATIONS_ORIENTED":4}'),

    (2035, 209, '1 - Very low', '{"DETAIL_ORIENTED":1,"LOGICAL":1}'),
    (2036, 209, '2 - Low', '{"DETAIL_ORIENTED":2,"LOGICAL":1,"OPERATIONS_ORIENTED":1}'),
    (2037, 209, '3 - Moderate', '{"DETAIL_ORIENTED":3,"LOGICAL":2,"OPERATIONS_ORIENTED":2}'),
    (2038, 209, '4 - High', '{"DETAIL_ORIENTED":4,"LOGICAL":3,"OPERATIONS_ORIENTED":3}'),
    (2039, 209, '5 - Very high', '{"DETAIL_ORIENTED":5,"LOGICAL":4,"OPERATIONS_ORIENTED":4}'),

    (2040, 210, '1 - Very low', '{"COMMUNICATION":1,"STRATEGIC":1}'),
    (2041, 210, '2 - Low', '{"COMMUNICATION":2,"STRATEGIC":1}'),
    (2042, 210, '3 - Moderate', '{"COMMUNICATION":3,"STRATEGIC":2}'),
    (2043, 210, '4 - High', '{"COMMUNICATION":4,"STRATEGIC":3}'),
    (2044, 210, '5 - Very high', '{"COMMUNICATION":5,"STRATEGIC":4}'),

    (2045, 211, '1 - Very low', '{"CREATIVE":1,"COMMUNICATION":1}'),
    (2046, 211, '2 - Low', '{"CREATIVE":2,"COMMUNICATION":1}'),
    (2047, 211, '3 - Moderate', '{"CREATIVE":3,"COMMUNICATION":2}'),
    (2048, 211, '4 - High', '{"CREATIVE":4,"COMMUNICATION":2}'),
    (2049, 211, '5 - Very high', '{"CREATIVE":5,"COMMUNICATION":3}'),

    (2050, 212, '1 - Very low', '{"SECURITY_ORIENTED":1,"OPERATIONS_ORIENTED":1,"DETAIL_ORIENTED":1}'),
    (2051, 212, '2 - Low', '{"SECURITY_ORIENTED":2,"OPERATIONS_ORIENTED":1,"DETAIL_ORIENTED":1}'),
    (2052, 212, '3 - Moderate', '{"SECURITY_ORIENTED":3,"OPERATIONS_ORIENTED":2,"DETAIL_ORIENTED":2}'),
    (2053, 212, '4 - High', '{"SECURITY_ORIENTED":4,"OPERATIONS_ORIENTED":3,"DETAIL_ORIENTED":3}'),
    (2054, 212, '5 - Very high', '{"SECURITY_ORIENTED":5,"OPERATIONS_ORIENTED":4,"DETAIL_ORIENTED":4}'),

    (2055, 213, 'Investigate the issue, isolate root cause, and ship a safe fix.', '{"LOGICAL":4,"DETAIL_ORIENTED":3,"OPERATIONS_ORIENTED":2}'),
    (2056, 213, 'Improve service architecture to prevent this class of issue.', '{"LOGICAL":4,"OPERATIONS_ORIENTED":3,"DETAIL_ORIENTED":2}'),
    (2057, 213, 'Resolve user-facing flow issues and communicate impact clearly.', '{"CREATIVE":3,"COMMUNICATION":2,"LOGICAL":2}'),
    (2058, 213, 'Coordinate testing and release quality checkpoints across teams.', '{"DETAIL_ORIENTED":3,"OPERATIONS_ORIENTED":3,"COMMUNICATION":2}'),

    (2059, 214, 'Build a predictive model and evaluate it with robust metrics.', '{"ANALYTICAL":5,"LOGICAL":3,"DETAIL_ORIENTED":2}'),
    (2060, 214, 'Create a clean dashboard and actionable KPI narrative.', '{"ANALYTICAL":4,"COMMUNICATION":3,"STRATEGIC":2}'),
    (2061, 214, 'Translate data uncertainty into clear business recommendations.', '{"ANALYTICAL":4,"STRATEGIC":3,"COMMUNICATION":3}'),
    (2062, 214, 'Automate data quality and feature pipelines for reuse.', '{"ANALYTICAL":4,"OPERATIONS_ORIENTED":3,"DETAIL_ORIENTED":2}'),

    (2063, 215, 'Lead problem framing, prioritization, and roadmap decisions.', '{"STRATEGIC":5,"COMMUNICATION":4,"ANALYTICAL":2}'),
    (2064, 215, 'Run market/channel experiments and improve growth conversion.', '{"COMMUNICATION":4,"STRATEGIC":3,"CREATIVE":2}'),
    (2065, 215, 'Analyze process and requirement gaps for business stakeholders.', '{"ANALYTICAL":4,"COMMUNICATION":3,"DETAIL_ORIENTED":2}'),
    (2066, 215, 'Coordinate teams to deliver an outcome with measurable impact.', '{"STRATEGIC":4,"COMMUNICATION":4,"DETAIL_ORIENTED":2}'),

    (2067, 216, 'Run user research and convert findings into clearer flows.', '{"CREATIVE":5,"COMMUNICATION":3,"DETAIL_ORIENTED":2}'),
    (2068, 216, 'Prototype multiple interaction patterns and test usability.', '{"CREATIVE":5,"DETAIL_ORIENTED":3,"ANALYTICAL":2}'),
    (2069, 216, 'Implement frontend improvements with accessibility in mind.', '{"CREATIVE":3,"LOGICAL":3,"DETAIL_ORIENTED":2}'),
    (2070, 216, 'Align design changes with product strategy and user goals.', '{"CREATIVE":4,"STRATEGIC":3,"COMMUNICATION":2}'),

    (2071, 217, 'Perform threat triage, containment, and incident response.', '{"SECURITY_ORIENTED":5,"OPERATIONS_ORIENTED":3,"DETAIL_ORIENTED":2}'),
    (2072, 217, 'Harden infrastructure and improve deployment safeguards.', '{"OPERATIONS_ORIENTED":4,"SECURITY_ORIENTED":4,"LOGICAL":2}'),
    (2073, 217, 'Design stronger automated tests and release quality gates.', '{"DETAIL_ORIENTED":4,"OPERATIONS_ORIENTED":3,"SECURITY_ORIENTED":2}'),
    (2074, 217, 'Audit systems and document long-term risk mitigation plan.', '{"SECURITY_ORIENTED":4,"STRATEGIC":2,"DETAIL_ORIENTED":3}');
