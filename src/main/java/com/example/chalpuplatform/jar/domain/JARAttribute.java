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
    OWNER_MESSAGE("사장님께 한마디"),
    DESIGN_SATISFACTION("디자인 만족도"),
    MOISTNESS("촉촉함"),
    CREAMINESS("크림 질감"),
    FLAVOR_BALANCE("맛 조화"),
    VALUE_FOR_MONEY("가격 대비 만족도"),
    REPURCHASE_INTENT("재구매 의향");

    private final String koreanName;
}