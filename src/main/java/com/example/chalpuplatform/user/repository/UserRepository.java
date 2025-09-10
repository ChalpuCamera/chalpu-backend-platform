package com.example.chalpuplatform.user.repository;

import com.example.chalpuplatform.oauth.model.AuthProvider;
import com.example.chalpuplatform.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // --- 기본 조회: 활성 사용자(deletedAt IS NULL)만 조회 ---

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);
    
    List<User> findAllByDeletedAtIsNull();

    boolean existsByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByProviderAndSocialIdAndDeletedAtIsNull(AuthProvider provider, String socialId);

    // --- 상태 무시 조회: deletedAt 값과 상관없이 모든 사용자를 조회 ---
    
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithDeleted(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailWithDeleted(@Param("email") String email);

    // --- 기존 코드와의 호환성을 위한 유지 (isActive 필드 사용) ---
    // 만약 isActive 필드를 제거한다면 이 메서드들도 함께 제거/수정해야 합니다.

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isActive = true")
    Optional<User> findActiveById(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.provider = :provider AND u.socialId = :socialId AND u.isActive = true")
    Optional<User> findActiveByProviderAndSocialId(@Param("provider") AuthProvider provider, @Param("socialId") String socialId);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.isActive = true")
    boolean existsActiveByEmail(@Param("email") String email);
    
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.provider != :provider AND u.isActive = true")
    boolean existsActiveByEmailAndProviderNot(@Param("email") String email, @Param("provider") AuthProvider provider);
    
    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findAllActive();
}
