package com.example.chalpuplatform.oauth.repository;

import com.example.chalpuplatform.oauth.model.AuthCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AuthCodeRepository extends JpaRepository<AuthCode, String> {

    Optional<AuthCode> findByCode(String code);
}