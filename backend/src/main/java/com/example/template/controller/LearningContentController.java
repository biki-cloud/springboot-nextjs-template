package com.example.template.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.template.entity.LearningContentEntity;
import com.example.template.entity.UserEntity;
import com.example.template.learningCurveStrategy.DefaultLearningCurveStrategy;
import com.example.template.learningCurveStrategy.LearningCurveStrategy;
import com.example.template.service.LearningContentService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/learning")
public class LearningContentController {

    @Autowired
    private LearningContentService learningContentService;

    private static final Logger logger = LoggerFactory.getLogger(LearningContentController.class);

    @GetMapping
    public List<LearningContentEntity> getAllContents() {
        return learningContentService.getAllContents();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LearningContentEntity> getContentById(@PathVariable Long id) {
        Optional<LearningContentEntity> content = learningContentService.getContentById(id);
        return content.map(ResponseEntity::ok)
                      .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/user/{userId}")
    public List<LearningContentEntity> getContentsByUser(@PathVariable Long userId) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        return learningContentService.getContentsByUser(user);
    }

    @GetMapping("/user/{userId}/learning-curve")
    public List<LearningContentEntity> getContentsByLearningCurve(
            @PathVariable Long userId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "random") String strategyType) { // デフルト戦略を指定
        UserEntity user = new UserEntity();
        user.setId(userId);
        
        LearningCurveStrategy strategy;
        // 戦略の選択
        switch (strategyType) {
            case "DefaultLearningCurveStrategy":
                strategy = new DefaultLearningCurveStrategy();
                break;
            default:
                strategy = new DefaultLearningCurveStrategy();
                break;
        }
        
        List<LearningContentEntity> allContents = learningContentService.getContentsByLearningCurve(user, category, strategy);
        return  allContents;
    }

    @GetMapping("/user/{userId}/categories")
    public List<LearningContentEntity> getContentsByUserAndCategories(
            @PathVariable Long userId,
            @RequestParam List<String> categories) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        return learningContentService.getContentsByUserAndCategories(user, categories);
    }

    @PostMapping
    public ResponseEntity<LearningContentEntity> createContent(@RequestBody LearningContentEntity content) {
        logger.info("Received request body: {}", content); // リクエストボディをログに出力
        LearningContentEntity savedContent = learningContentService.saveContent(content);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedContent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        learningContentService.deleteContent(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<LearningContentEntity> updateLearningContent(
            @PathVariable Long id, @RequestBody LearningContentEntity newContent) {
        Optional<LearningContentEntity> optionalContent = learningContentService.getContentById(id);

        if (optionalContent.isPresent()) {
            LearningContentEntity existingContent = optionalContent.get();
            existingContent.setDraft(newContent.isDraft());
            existingContent.setTitle(newContent.getTitle());
            existingContent.setContent(newContent.getContent());
            existingContent.setCategory(newContent.getCategory());
            existingContent.setUser(newContent.getUser());
            existingContent.setLastReviewedDate(newContent.getLastReviewedDate());
            existingContent.setReviewCount(newContent.getReviewCount());
            learningContentService.saveContent(existingContent);
            return ResponseEntity.ok(existingContent);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = learningContentService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/strategies")
    public ResponseEntity<List<String>> getLearningCurveStrategies() {
        List<String> strategies = List.of(
            new DefaultLearningCurveStrategy().getClass().getSimpleName()
        );
        return ResponseEntity.ok(strategies);
    }

    @PostMapping("/{id}/correct")
    public ResponseEntity<LearningContentEntity> markCorrect(@PathVariable Long id) {
        Optional<LearningContentEntity> contentOpt = learningContentService.getContentById(id);
        if (contentOpt.isPresent()) {
            LearningContentEntity content = contentOpt.get();
            content.correctAnswer(); // 正解処理
            learningContentService.saveContent(content); // 更新を保存
            return ResponseEntity.ok(content);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/incorrect")
    public ResponseEntity<LearningContentEntity> markIncorrect(@PathVariable Long id) {
        Optional<LearningContentEntity> contentOpt = learningContentService.getContentById(id);
        if (contentOpt.isPresent()) {
            LearningContentEntity content = contentOpt.get();
            content.incorrectAnswer(); // 不正解処理
            learningContentService.saveContent(content); // 更新を保存
            return ResponseEntity.ok(content);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public List<LearningContentEntity> searchContents(@RequestParam String term) {
        return learningContentService.searchContents(term);
    }

    @GetMapping("/drafts/user/{userId}")
    public List<LearningContentEntity> getDraftsByUser(@PathVariable Long userId) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        return learningContentService.getDraftsByUser(user);
    }
}