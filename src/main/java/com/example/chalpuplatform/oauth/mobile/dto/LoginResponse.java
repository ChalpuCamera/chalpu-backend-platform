package com.example.chalpuplatform.oauth.mobile.dto;

import com.example.chalpuplatform.oauth.dto.TokenDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private TokenDTO tokens;
    private Long userId;
}