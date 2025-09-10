package com.example.chalpuplatform.user.dto;

import com.example.chalpuplatform.user.domain.User;
import lombok.Getter;

@Getter
public class UserDto {
    private Long id;
    private String email;
    private String name;
    private String profileImageUrl;
    private String provider;

    public UserDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.profileImageUrl = user.getPicture();
        this.provider = user.getProvider().name();
    }
}
