package com.example.chalpuplatform.landing.repository;

import com.example.chalpuplatform.landing.domain.ContactInquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactInquiryRepository extends JpaRepository<ContactInquiry, Long> {
}
