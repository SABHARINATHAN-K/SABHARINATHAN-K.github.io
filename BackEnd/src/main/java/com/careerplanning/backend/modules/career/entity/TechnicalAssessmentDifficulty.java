package com.careerplanning.backend.modules.career.entity;

public enum TechnicalAssessmentDifficulty {
    FOUNDATION(10),
    APPLIED(15),
    ARCHITECTURE(20);

    private final int points;

    TechnicalAssessmentDifficulty(int points) {
        this.points = points;
    }

    public int points() {
        return points;
    }
}
