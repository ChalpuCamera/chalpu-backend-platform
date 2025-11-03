package com.example.chalpuplatform.jar.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JARAttribute {
    SPICINESS("매운맛"),
    SWEETNESS("단맛"),
    SALTINESS("간"),
    SOURNESS("신맛"),
    BITTERNESS("쓴맛"),
    UMAMI("감칠맛"),
    CRISPINESS("바삭함"),
    CHEWINESS("쫄깃함"),
    TENDERNESS("부드러움"),
    PORTION_SIZE("양"),
    FRESHNESS("재료 신선도"),
    TEMPERATURE("온도"),
    DONENESS("익힘정도"),
    OILINESS("기름짐"),
    PRICE("가격"),
    OWNER_MESSAGE("사장님께 한마디");

    private final String koreanName;
}