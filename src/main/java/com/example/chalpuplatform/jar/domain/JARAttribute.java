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
    PORTION_SIZE("양"),
    TEMPERATURE("온도"),
    DONENESS("익힘정도"),
    OILINESS("기름짐"),
    PRICE("가격");
    
    private final String koreanName;
}