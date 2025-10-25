package com.example.chalpuplatform.store.dto;

import com.example.chalpuplatform.store.domain.LinkType;
import com.example.chalpuplatform.store.domain.Store;
import com.example.chalpuplatform.store.domain.StoreLink;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매장 응답")
public class StoreResponse {
    
    @Schema(description = "매장 ID", example = "1")
    private Long storeId;
    
    @Schema(description = "매장명", example = "맛있는 식당")
    private String storeName;

    @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
    private String address;

    @Schema(description = "가게 설명", example = "저희 가게는 신선한 재료로 음식을 만듭니다.")
    private String description;

    @Schema(description = "피드백 개수", example = "42")
    private Long feedbackCount;

    @Schema(description = "메뉴 개수", example = "15")
    private Long menuCount;

    @Schema(description = "썸네일 URL", example = "https://chalpu.s3.ap-northeast-2.amazonaws.com/stores/1/thumbnail.jpg")
    private String thumbnailUrl;

    @Schema(description = "쿠폰 사용 개수", example = "10", required = false)
    private Integer requiredStampsForCoupon;

    @Schema(description = "템플릿 유형", example = "1", required = false)
    private Integer displayTemplate;

    @Schema(description = "사장님의 메뉴가 자동으로 자신의 가게에 등록되는 기능 on/off")
    private Boolean autoCreateMenus;

    @Schema(description = "사이트 링크 (매장 고유 식별자)", example = "우리집냉면")
    private String siteLink;

    @Schema(description = "매장 링크 목록")
    private List<StoreLinkResponse> links;

    public static StoreResponse from(Store store) {
        List<StoreLinkResponse> linkResponses = store.getLinks().stream()
                .sorted(Comparator.comparing(StoreLink::getDisplayOrder))
                .map(StoreLinkResponse::from)
                .collect(Collectors.toList());

        return StoreResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .address(store.getAddress())
                .description(store.getDescription())
                .feedbackCount(store.getFeedbackCount())
                .menuCount(store.getMenuCount())
                .thumbnailUrl(store.getThumbnailUrl())
                .requiredStampsForCoupon(store.getRequiredStampsForCoupon())
                .displayTemplate(store.getDisplayTemplate() != null ? store.getDisplayTemplate() : 1)
                .autoCreateMenus(store.getAutoCreateMenus() != null ? store.getAutoCreateMenus() : false)
                .siteLink(store.getSiteLink())
                .links(linkResponses)
                .build();
    }
} 