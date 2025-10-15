package com.example.chalpuplatform.landing.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "landing_page_button_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LandingPageButtonLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "button_log_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ButtonType buttonType;

    public static LandingPageButtonLog createLog(ButtonType buttonType) {
        return LandingPageButtonLog.builder()
                .buttonType(buttonType)
                .build();
    }
}
