package com.example.chalpuplatform.fooditem.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FoodCategory {
    MAIN("메인요리"),
    SIDE("사이드"),
    DRINK("음료"),
    DESSERT("디저트"),
    APPETIZER("전채요리"),
    SOUP("스프"),
    SALAD("샐러드"),
    ETC("기타");

    private final String description;
}