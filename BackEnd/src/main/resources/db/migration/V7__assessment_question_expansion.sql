INSERT INTO assessment_questions (id, question_text, stage) VALUES
    (101, 'I enjoy debugging complex systems until I find the root cause.', 1),
    (102, 'I am curious about customer behavior and market trends.', 1),
    (103, 'I am motivated by improving digital safety and risk prevention.', 1),
    (104, 'How confident are you with Git collaboration (branching, PRs, merges)?', 2),
    (105, 'How much hands-on cloud or deployment exposure do you have?', 2),
    (106, 'How often do you create reports/presentations from data insights?', 2),
    (107, 'Would you enjoy turning designs into responsive, interactive interfaces?', 3),
    (108, 'Would you enjoy building automated tests and improving release quality?', 3),
    (109, 'Would you enjoy analyzing business KPIs and presenting decision insights?', 3),
    (110, 'Would you enjoy monitoring threats and handling incident response workflows?', 3),
    (111, 'Would you enjoy leading cross-functional teams to deliver product outcomes?', 3),
    (112, 'Would you enjoy user research, wireframing, and design system thinking?', 3);

INSERT INTO assessment_options (id, question_id, option_text, weight_json) VALUES
    (1001, 101, 'Strongly agree', '{"SOFTWARE_ENGINEERING":3,"JAVA_BACKEND_DEVELOPER":3,"FULL_STACK_DEVELOPER":2,"QA_ENGINEER":2,"DEVOPS_ENGINEER":2,"CYBERSECURITY_ANALYST":1}'),
    (1002, 101, 'Agree', '{"SOFTWARE_ENGINEERING":2,"JAVA_BACKEND_DEVELOPER":2,"FULL_STACK_DEVELOPER":2,"QA_ENGINEER":1,"DEVOPS_ENGINEER":1}'),
    (1003, 101, 'Neutral', '{"SOFTWARE_ENGINEERING":1,"QA_ENGINEER":1}'),
    (1004, 101, 'Disagree', '{"DESIGN":1,"MARKETING":1,"PRODUCT_MANAGER":1}'),

    (1005, 102, 'Strongly agree', '{"MARKETING":3,"PRODUCT_MANAGEMENT":3,"PRODUCT_MANAGER":3,"DATA_ANALYST":2,"DATA_SCIENCE":1}'),
    (1006, 102, 'Agree', '{"MARKETING":2,"PRODUCT_MANAGEMENT":2,"PRODUCT_MANAGER":2,"DATA_ANALYST":1}'),
    (1007, 102, 'Neutral', '{"MARKETING":1,"PRODUCT_MANAGER":1}'),
    (1008, 102, 'Disagree', '{"JAVA_BACKEND_DEVELOPER":1,"CYBERSECURITY_ANALYST":1}'),

    (1009, 103, 'Strongly agree', '{"CYBERSECURITY_ANALYST":4,"DEVOPS_ENGINEER":2,"QA_ENGINEER":1,"SOFTWARE_ENGINEERING":1}'),
    (1010, 103, 'Agree', '{"CYBERSECURITY_ANALYST":2,"DEVOPS_ENGINEER":1,"QA_ENGINEER":1}'),
    (1011, 103, 'Neutral', '{"CYBERSECURITY_ANALYST":1}'),
    (1012, 103, 'Disagree', '{"DESIGN":1,"MARKETING":1}'),

    (1013, 104, 'Advanced: regular branching/review workflows', '{"SOFTWARE_ENGINEERING":2,"JAVA_BACKEND_DEVELOPER":2,"FULL_STACK_DEVELOPER":2,"DEVOPS_ENGINEER":1,"QA_ENGINEER":1}'),
    (1014, 104, 'Intermediate: comfortable with day-to-day usage', '{"SOFTWARE_ENGINEERING":1,"FULL_STACK_DEVELOPER":1,"FRONTEND_DEVELOPER":1,"DATA_SCIENTIST":1}'),
    (1015, 104, 'Beginner', '{"DATA_ANALYST":1,"MARKETING":1,"PRODUCT_MANAGER":1}'),
    (1016, 104, 'No exposure', '{"DESIGN":1,"UI_UX_DESIGNER":1}'),

    (1017, 105, 'Built/deployed services in cloud environments', '{"DEVOPS_ENGINEER":4,"FULL_STACK_DEVELOPER":2,"JAVA_BACKEND_DEVELOPER":2,"SOFTWARE_ENGINEERING":1}'),
    (1018, 105, 'Some practical exposure', '{"DEVOPS_ENGINEER":2,"FULL_STACK_DEVELOPER":1,"JAVA_BACKEND_DEVELOPER":1}'),
    (1019, 105, 'Only basic theoretical understanding', '{"SOFTWARE_ENGINEERING":1,"DATA_SCIENCE":1}'),
    (1020, 105, 'No exposure', '{"DESIGN":1,"MARKETING":1}'),

    (1021, 106, 'Frequently, with business recommendations', '{"DATA_ANALYST":3,"DATA_SCIENCE":2,"PRODUCT_MANAGER":2,"MARKETING":2}'),
    (1022, 106, 'Occasionally', '{"DATA_ANALYST":2,"DATA_SCIENCE":1,"PRODUCT_MANAGER":1}'),
    (1023, 106, 'Rarely', '{"SOFTWARE_ENGINEERING":1,"JAVA_BACKEND_DEVELOPER":1}'),
    (1024, 106, 'Never', '{"DESIGN":1}'),

    (1025, 107, 'Very interested', '{"FRONTEND_DEVELOPER":4,"FULL_STACK_DEVELOPER":3,"UI_UX_DESIGNER":2,"DESIGN":1}'),
    (1026, 107, 'Somewhat interested', '{"FRONTEND_DEVELOPER":2,"FULL_STACK_DEVELOPER":2,"UI_UX_DESIGNER":1}'),
    (1027, 107, 'Not sure', '{"FRONTEND_DEVELOPER":1,"FULL_STACK_DEVELOPER":1}'),
    (1028, 107, 'Not interested', '{"JAVA_BACKEND_DEVELOPER":1,"CYBERSECURITY_ANALYST":1}'),

    (1029, 108, 'Very interested', '{"QA_ENGINEER":4,"SOFTWARE_ENGINEERING":2,"JAVA_BACKEND_DEVELOPER":1,"FULL_STACK_DEVELOPER":1}'),
    (1030, 108, 'Somewhat interested', '{"QA_ENGINEER":2,"SOFTWARE_ENGINEERING":1,"FULL_STACK_DEVELOPER":1}'),
    (1031, 108, 'Not sure', '{"QA_ENGINEER":1}'),
    (1032, 108, 'Not interested', '{"DESIGN":1,"MARKETING":1}'),

    (1033, 109, 'Very interested', '{"DATA_ANALYST":4,"DATA_SCIENCE":2,"PRODUCT_MANAGER":1,"MARKETING":1}'),
    (1034, 109, 'Somewhat interested', '{"DATA_ANALYST":2,"DATA_SCIENCE":1,"PRODUCT_MANAGER":1}'),
    (1035, 109, 'Not sure', '{"DATA_ANALYST":1,"DATA_SCIENCE":1}'),
    (1036, 109, 'Not interested', '{"SOFTWARE_ENGINEERING":1,"DESIGN":1}'),

    (1037, 110, 'Very interested', '{"CYBERSECURITY_ANALYST":4,"DEVOPS_ENGINEER":2,"QA_ENGINEER":1}'),
    (1038, 110, 'Somewhat interested', '{"CYBERSECURITY_ANALYST":2,"DEVOPS_ENGINEER":1}'),
    (1039, 110, 'Not sure', '{"CYBERSECURITY_ANALYST":1}'),
    (1040, 110, 'Not interested', '{"MARKETING":1,"DESIGN":1}'),

    (1041, 111, 'Very interested', '{"PRODUCT_MANAGER":4,"PRODUCT_MANAGEMENT":4,"MARKETING":2}'),
    (1042, 111, 'Somewhat interested', '{"PRODUCT_MANAGER":2,"PRODUCT_MANAGEMENT":2,"MARKETING":1}'),
    (1043, 111, 'Not sure', '{"PRODUCT_MANAGER":1,"PRODUCT_MANAGEMENT":1}'),
    (1044, 111, 'Not interested', '{"JAVA_BACKEND_DEVELOPER":1,"DATA_SCIENCE":1}'),

    (1045, 112, 'Very interested', '{"UI_UX_DESIGNER":4,"DESIGN":4,"FRONTEND_DEVELOPER":1}'),
    (1046, 112, 'Somewhat interested', '{"UI_UX_DESIGNER":2,"DESIGN":2,"FRONTEND_DEVELOPER":1}'),
    (1047, 112, 'Not sure', '{"UI_UX_DESIGNER":1,"DESIGN":1}'),
    (1048, 112, 'Not interested', '{"CYBERSECURITY_ANALYST":1,"DEVOPS_ENGINEER":1}');
