package com.example.template.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.template.entity.LearningContentEntity;
import com.example.template.entity.UserEntity;
import com.example.template.learningCurveStrategy.LearningCurveStrategy;
import com.example.template.repository.LearningContentRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
public class LearningContentService {
    
    @Autowired
    private LearningContentRepository learningContentRepository;
    
    public List<LearningContentEntity> getAllContents() {
        return learningContentRepository.findAll();
    }
    
    public Optional<LearningContentEntity> getContentById(Long id) {
        return learningContentRepository.findById(id);
    }
    
    public List<LearningContentEntity> getContentsByCategory(String category) {
        return learningContentRepository.findByCategory(category);
    }
    
    public List<LearningContentEntity> getContentsByUser(UserEntity user) {
        return learningContentRepository.findByUser(user);
    }
    
    public LearningContentEntity saveContent(LearningContentEntity content) {
        if (content.getCreatedDate() == null) {
            content.setCreatedDate(LocalDate.now());
        }

        if (content.getNextReviewDate() == null) {
            content.setNextReviewDate(LocalDate.now());
        }
        return learningContentRepository.save(content);
    }
    
    public void deleteContent(Long id) {
        learningContentRepository.deleteById(id);
    }

    public List<LearningContentEntity> getContentsByLearningCurve(UserEntity user, String category, LearningCurveStrategy strategy) {
        List<LearningContentEntity> allContents;
        if (category == null || category.isEmpty()) {
            allContents = learningContentRepository.findByUser(user);
        } else {
            List<String> categories = List.of(category.split(","));
            allContents = learningContentRepository.findByUserAndCategoryIn(user, categories);
        }
        // ドラフトでないコンテンツのみをフィルタリング
        allContents = allContents.stream()
                                 .filter(content -> !content.isDraft())
                                 .collect(Collectors.toList());
        return allContents.isEmpty() ? allContents : strategy.filterByLearningCurve(allContents);
    }
    
    public List<String> getAllCategories() {
        List<String> categories = learningContentRepository.findDistinctCategories();
        return categories.stream()
                         .flatMap(category -> Arrays.stream(category.split(","))) // カンマで分割
                         .distinct() // 重複を排除
                         .collect(Collectors.toList());
    }
    
    public List<LearningContentEntity> searchContents(String searchTerm) {
        List<LearningContentEntity> allContents = learningContentRepository.findAll();
        return allContents.stream()
            .filter(content -> content.getTitle().toLowerCase().contains(searchTerm.toLowerCase()) ||
                              content.getContent().toLowerCase().contains(searchTerm.toLowerCase()))
            .collect(Collectors.toList());
    }
    
    public List<LearningContentEntity> getDraftsByUser(UserEntity user) {
        return learningContentRepository.findByUserAndIsDraft(user, true);
    }
    
    public List<LearningContentEntity> getContentsByUserAndCategories(UserEntity user, List<String> categories) {
        return learningContentRepository.findByUserAndCategoryIn(user, categories);
    }
    
}