package com.example.usercontent.service;

import com.example.usercontent.model.UserContent;
import com.example.usercontent.repository.UserContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserContentService {

    @Autowired
    private UserContentRepository repository;

    public UserContent saveContent(String content) {
        UserContent uc = new UserContent();
        uc.setContent(content);
        uc.setCreatedAt(LocalDateTime.now());
        return repository.save(uc);
    }

    public Optional<UserContent> getContent(Long id) {
        return repository.findById(id);
    }

    public void deleteContent(Long id) {
        repository.deleteById(id);
    }
}
