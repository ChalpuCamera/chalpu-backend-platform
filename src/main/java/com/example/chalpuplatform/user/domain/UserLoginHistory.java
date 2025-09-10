package com.example.chalpuplatform.user.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_login_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class UserLoginHistory extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_history_id")
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    public static UserLoginHistory createLoginHistory(Long userId) {
        return UserLoginHistory.builder()
                .userId(userId)
                .build();
    }
}