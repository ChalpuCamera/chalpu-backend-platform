package com.example.chalpuplatform.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private Integer code;
    private String message;
    private T result;
    
    /**
     * 성공 응답 - 데이터와 함께
     */
    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(200, "API 요청이 성공했습니다.", result);
    }
    
    /**
     * 성공 응답 - 데이터 없음
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "API 요청이 성공했습니다.", null);
    }
    
    /**
     * 성공 응답 - 커스텀 메시지
     */
    public static <T> ApiResponse<T> success(String message, T result) {
        return new ApiResponse<>(200, message, result);
    }
    /**
     * 서버 오류 응답 - 커스텀 메시지
     */
    public static <T> ApiResponse<T> serverError(String message) {
        return new ApiResponse<>(500, message, null);
    }
    
    /**
     * 인증 오류 응답
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
