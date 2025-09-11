package com.example.chalpuplatform.store.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DeliveryPlatformLinks {
    
    @Column(name = "baemin_link", columnDefinition = "TEXT", nullable = true)
    private String baeminLink;
    
    @Column(name = "yogiyo_link", columnDefinition = "TEXT", nullable = true)
    private String yogiyoLink;
    
    @Column(name = "coupangeats_link", columnDefinition = "TEXT", nullable = true)
    private String coupangeatsLink;
}