package com.example.chalpuplatform.store.repository;

import com.example.chalpuplatform.store.domain.StoreLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreLinkRepository extends JpaRepository<StoreLink, Long> {
}
