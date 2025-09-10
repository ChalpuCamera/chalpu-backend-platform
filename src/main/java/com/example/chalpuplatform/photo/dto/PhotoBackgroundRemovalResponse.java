package com.example.chalpuplatform.photo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PhotoBackgroundRemovalResponse {
    private String processedImageBase64;
    private String originalFileName;
}