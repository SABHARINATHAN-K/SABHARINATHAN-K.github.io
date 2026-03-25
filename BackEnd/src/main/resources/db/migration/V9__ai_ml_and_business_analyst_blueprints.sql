INSERT INTO career_phases (id, career_track, phase_order, phase_title, description) VALUES
    (101, 'AI_ML_ENGINEER', 1, 'Foundation', 'Build math, ML fundamentals, and robust coding practices.'),
    (102, 'AI_ML_ENGINEER', 2, 'Execution', 'Train, evaluate, and deploy end-to-end ML solutions.'),
    (103, 'AI_ML_ENGINEER', 3, 'Acceleration', 'Productionize AI systems and monitor real-world performance.'),
    (104, 'BUSINESS_ANALYST', 1, 'Foundation', 'Build requirement analysis, KPI fluency, and business context.'),
    (105, 'BUSINESS_ANALYST', 2, 'Execution', 'Deliver insights, process improvements, and decision support.'),
    (106, 'BUSINESS_ANALYST', 3, 'Acceleration', 'Scale stakeholder influence with measurable business impact.');

INSERT INTO career_goal_templates (id, career_track, phase_id, title, description, category, priority, default_order) VALUES
    (201, 'AI_ML_ENGINEER', 101, 'Strengthen ML and linear algebra fundamentals', 'Cover model intuition, vectors, optimization, and evaluation basics.', 'SKILL_DEVELOPMENT', 'HIGH', 1),
    (202, 'AI_ML_ENGINEER', 101, 'Build reusable data preprocessing workflows', 'Create robust feature preparation and data validation pipelines.', 'LEARNING', 'MEDIUM', 2),
    (203, 'AI_ML_ENGINEER', 102, 'Ship an end-to-end ML project', 'From data collection to model deployment and metric tracking.', 'PROJECT', 'HIGH', 1),
    (204, 'AI_ML_ENGINEER', 103, 'Add monitoring and drift controls to ML service', 'Track model quality, drift, and retraining thresholds.', 'CAREER_GROWTH', 'HIGH', 1),
    (205, 'BUSINESS_ANALYST', 104, 'Master requirement elicitation and process mapping', 'Practice stakeholder interviews, user stories, and workflow mapping.', 'SKILL_DEVELOPMENT', 'HIGH', 1),
    (206, 'BUSINESS_ANALYST', 104, 'Improve SQL and dashboard storytelling', 'Build clear insight narratives tied to business metrics.', 'LEARNING', 'MEDIUM', 2),
    (207, 'BUSINESS_ANALYST', 105, 'Deliver one KPI-driven business analysis project', 'Analyze performance trends and recommend concrete actions.', 'PROJECT', 'HIGH', 1),
    (208, 'BUSINESS_ANALYST', 106, 'Lead a cross-functional insights review', 'Present impact, risks, and next-step recommendations.', 'CAREER_GROWTH', 'MEDIUM', 1);
