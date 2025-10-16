package com.example.chalpuplatform.landing.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contact_inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ContactInquiry extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id")
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "유저 아이디")
    private Long userId;

    public static ContactInquiry createInquiry(String content, Long userId) {
        return ContactInquiry.builder()
                .content(content)
                .userId(userId)
                .build();
    }
}
