package com.example.chalpuplatform.store.domain;

import com.example.chalpuplatform.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "store_links",
    indexes = @Index(name = "idx_store_display_order", columnList = "store_id, display_order")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class StoreLink extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "link_type", nullable = false, length = 50)
    private LinkType linkType;

    @Column(name = "custom_label", length = 50)
    private String Label;

    @Column(name = "url", length = 500, nullable = false)
    private String url;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public static StoreLink create(Store store, LinkType linkType, String label, String url, Integer displayOrder) {
        return StoreLink.builder()
                .store(store)
                .linkType(linkType)
                .Label(label)
                .url(url)
                .displayOrder(displayOrder)
                .build();
    }
}
