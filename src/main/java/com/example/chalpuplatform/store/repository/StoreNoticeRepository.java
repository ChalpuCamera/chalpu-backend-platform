package com.example.chalpuplatform.store.repository;

import com.example.chalpuplatform.store.domain.StoreNotice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreNoticeRepository extends JpaRepository<StoreNotice, Long> {

    Page<StoreNotice> findByStoreId(Long storeId, Pageable pageable);

    Optional<StoreNotice> findByStoreIdAndIsRepresentativeTrue(Long storeId);
}
