package com.example.usercontent.repository;

import com.example.usercontent.model.UserContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserContentRepository extends JpaRepository<UserContent, Long> {
}
