package com.example.chalpuplatform.store.repository;

import com.example.chalpuplatform.store.domain.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    
    Optional<Store> findByIdAndIsActiveTrue(Long id);
    
    List<Store> findByIsActiveTrue();

    Page<Store> findByIsActiveTrue(Pageable pageable);
} 