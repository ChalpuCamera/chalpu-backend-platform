package com.example.chalpuplatform.user.repository;

import com.example.chalpuplatform.user.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    @Query("SELECT up FROM UserProfile up WHERE up.user.id = :userId AND up.user.deletedAt IS NULL")
    Optional<UserProfile> findByUserId(@Param("userId") Long userId);
}