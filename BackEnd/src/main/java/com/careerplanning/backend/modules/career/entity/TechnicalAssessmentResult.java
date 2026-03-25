package com.careerplanning.backend.modules.career.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "technical_assessment_results")
public class TechnicalAssessmentResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String careerTrack;

    @Column(nullable = false, length = 50)
    private String proficiencyLevel;

    @Column(nullable = false)
    private Integer totalScore;

    @Column(nullable = false)
    private Integer maxScore;

    @Column(nullable = false)
    private Integer percentageScore;

    @Column(nullable = false, columnDefinition = "json")
    private String skillAreaJson;

    @Column(nullable = false, length = 2000)
    private String summary;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCareerTrack() {
        return careerTrack;
    }

    public void setCareerTrack(String careerTrack) {
        this.careerTrack = careerTrack;
    }

    public String getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(String proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public Integer getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Integer totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(Integer maxScore) {
        this.maxScore = maxScore;
    }

    public Integer getPercentageScore() {
        return percentageScore;
    }

    public void setPercentageScore(Integer percentageScore) {
        this.percentageScore = percentageScore;
    }

    public String getSkillAreaJson() {
        return skillAreaJson;
    }

    public void setSkillAreaJson(String skillAreaJson) {
        this.skillAreaJson = skillAreaJson;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
