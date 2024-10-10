package com.example.template.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.ManyToOne;
import java.time.ZoneId;
import jakarta.persistence.JoinColumn;
import java.time.LocalDate;

@Entity
@Table(name = "learning_content")
@Data
public class LearningContentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = true)
    private LocalDate createdDate;

    @Column(nullable = true)
    private LocalDate lastReviewedDate;

    @Column(nullable = false)
    private int reviewCount = 0;

    @Column(nullable = false)
    private int correctCount = 0; // Correct count

    @Column(nullable = false)
    private int incorrectCount = 0; // Incorrect count

    @Column(nullable = false)
    private int level = 1; // Level

    @Column(nullable = true)
    private LocalDate nextReviewDate; // Next review date

    @Column(nullable = true)
    private boolean isDraft = false;

    // Method to handle correct answer
    public void correctAnswer() {
        this.correctCount++;
        this.reviewCount++;
        
        // レベルアップの条件を厳しくする
        if (correctCount % 5 == 0) { // 5回正解するごとにレベルアップ
            this.level = Math.min(level + 1, 5); // Increase level (max 3)
        }
        
        this.lastReviewedDate = LocalDate.now(ZoneId.of("Asia/Tokyo"));
        this.nextReviewDate = calculateNextReviewDate();
    }

    // Method to handle incorrect answer
    public void incorrectAnswer() {
        this.incorrectCount++;
        this.reviewCount++;
        
        // レベルダウンの条件を厳しくする
        if (incorrectCount % 3 == 0) { // 3回不正解するごとにレベルダウン
            this.level = Math.max(level - 1, 1); // Decrease level (min 1)
        }
        
        this.lastReviewedDate = LocalDate.now(ZoneId.of("Asia/Tokyo"));
        this.nextReviewDate = calculateNextReviewDate();
    }

    // Method to calculate next review date
    private LocalDate calculateNextReviewDate() {
        switch (this.level) {
            case 1:
                return LocalDate.now(ZoneId.of("Asia/Tokyo")).plusDays(1); // Next day
            case 2:
                return LocalDate.now(ZoneId.of("Asia/Tokyo")).plusDays(3); // 3 days later
            case 3:
                return LocalDate.now(ZoneId.of("Asia/Tokyo")).plusWeeks(1); // 1 week later
            case 4:
                return LocalDate.now(ZoneId.of("Asia/Tokyo")).plusWeeks(3); // 3 weeks later
            case 5:
                return LocalDate.now(ZoneId.of("Asia/Tokyo")).plusWeeks(5); // 5 weeks later
            default:
                return LocalDate.now(ZoneId.of("Asia/Tokyo")).plusWeeks(5);
        }
    }

    @Override
    public String toString() {
        return "LearningContentEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", category='" + category + '\'' +
                ", user=" + user +
                ", createdDate=" + createdDate +
                ", lastReviewedDate=" + lastReviewedDate +
                ", reviewCount=" + reviewCount +
                ", correctCount=" + correctCount +
                ", incorrectCount=" + incorrectCount +
                ", level=" + level +
                ", nextReviewDate=" + nextReviewDate +
                '}';
    }
}