package com.example.chalpuplatform.landing.repository;

import com.example.chalpuplatform.landing.domain.LandingPageButtonLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LandingPageButtonLogRepository extends JpaRepository<LandingPageButtonLog, Long> {
}
