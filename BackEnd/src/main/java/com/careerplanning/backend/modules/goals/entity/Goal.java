package com.careerplanning.backend.modules.goals.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "goals")
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalStatus status = GoalStatus.PLANNED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalPriority priority = GoalPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GoalCategory category = GoalCategory.LEARNING;

    private LocalDate targetDate;

    private Instant completedDate;

    @Column(nullable = false)
    private Integer progress = 0;

    @Column(length = 4000)
    private String notes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "goal_tags", joinColumns = @JoinColumn(name = "goal_id"))
    @OrderColumn(name = "tag_order")
    @Column(name = "tag", nullable = false)
    private List<String> tags = new ArrayList<>();

    @Column(name = "is_blueprint_goal", nullable = false)
    private boolean blueprintGoal = false;

    private Long blueprintTemplateId;

    private Integer blueprintPhaseOrder;

    private Integer blueprintDefaultOrder;

    private String blueprintPhaseTitle;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public GoalPriority getPriority() {
        return priority;
    }

    public void setPriority(GoalPriority priority) {
        this.priority = priority;
    }

    public GoalCategory getCategory() {
        return category;
    }

    public void setCategory(GoalCategory category) {
        this.category = category;
    }

    public Instant getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(Instant completedDate) {
        this.completedDate = completedDate;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isBlueprintGoal() {
        return blueprintGoal;
    }

    public void setBlueprintGoal(boolean blueprintGoal) {
        this.blueprintGoal = blueprintGoal;
    }

    public Long getBlueprintTemplateId() {
        return blueprintTemplateId;
    }

    public void setBlueprintTemplateId(Long blueprintTemplateId) {
        this.blueprintTemplateId = blueprintTemplateId;
    }

    public Integer getBlueprintPhaseOrder() {
        return blueprintPhaseOrder;
    }

    public void setBlueprintPhaseOrder(Integer blueprintPhaseOrder) {
        this.blueprintPhaseOrder = blueprintPhaseOrder;
    }

    public Integer getBlueprintDefaultOrder() {
        return blueprintDefaultOrder;
    }

    public void setBlueprintDefaultOrder(Integer blueprintDefaultOrder) {
        this.blueprintDefaultOrder = blueprintDefaultOrder;
    }

    public String getBlueprintPhaseTitle() {
        return blueprintPhaseTitle;
    }

    public void setBlueprintPhaseTitle(String blueprintPhaseTitle) {
        this.blueprintPhaseTitle = blueprintPhaseTitle;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
