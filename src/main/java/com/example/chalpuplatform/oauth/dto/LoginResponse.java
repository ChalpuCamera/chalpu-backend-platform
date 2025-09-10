package com.example.chalpuplatform.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private TokenDTO tokens;
    private Long userId;
}