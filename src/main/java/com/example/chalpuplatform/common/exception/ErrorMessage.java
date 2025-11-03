package com.example.chalpuplatform.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum ErrorMessage {
    // 유저 관련 에러
    USER_NOT_FOUND(NOT_FOUND, "존재하지 않는 회원입니다."),
    USER_EMAIL_ALREADY_EXISTS(BAD_REQUEST, "이미 사용 중인 이메일입니다."),
    USER_USERNAME_ALREADY_EXISTS(BAD_REQUEST, "이미 사용 중인 사용자 이름입니다."),
    USER_INVALID_CREDENTIALS(UNAUTHORIZED, "잘못된 이메일 또는 비밀번호입니다."),
    
    // OAuth 관련 에러
    OAUTH_DUPLICATE_EMAIL(BAD_REQUEST, "이미 다른 소셜 계정으로 가입된 이메일입니다."),
    OAUTH_USER_INFO_NOT_FOUND(BAD_REQUEST, "OAuth 사용자 정보를 가져올 수 없습니다."),
    OAUTH_PROVIDER_NOT_SUPPORTED(BAD_REQUEST, "지원하지 않는 OAuth 제공자입니다."),
    OAUTH_AUTHENTICATION_FAILED(UNAUTHORIZED, "OAuth 인증에 실패했습니다."),
    OAUTH_PROVIDER_CONFLICT(HttpStatus.CONFLICT, "이미 다른 제공자로 가입된 계정입니다."),
    OAUTH_EMAIL_NOT_FOUND(BAD_REQUEST, "OAuth2 제공자로부터 이메일을 찾을 수 없습니다."),
    
    // Apple 관련 에러
    APPLE_IDENTITY_TOKEN_INVALID(UNAUTHORIZED, "Apple Identity Token이 유효하지 않습니다."),
    APPLE_IDENTITY_TOKEN_EXPIRED(UNAUTHORIZED, "Apple Identity Token이 만료되었습니다."),
    APPLE_PUBLIC_KEY_NOT_FOUND(UNAUTHORIZED, "일치하는 Apple 공개키를 찾을 수 없습니다."),
    APPLE_JWT_VERIFICATION_FAILED(UNAUTHORIZED, "Apple JWT 검증에 실패했습니다."),
    APPLE_USER_INFO_MISSING(BAD_REQUEST, "Apple 사용자 정보가 누락되었습니다."),
    
    // 인증 관련 에러
    AUTH_INVALID_TOKEN(UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    AUTH_EXPIRED_TOKEN(UNAUTHORIZED, "만료된 토큰입니다."),
    AUTH_INVALID_REFRESH_TOKEN(UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    AUTH_EXPIRED_REFRESH_TOKEN(UNAUTHORIZED, "만료된 리프레시 토큰입니다."),
    AUTH_REFRESH_TOKEN_NOT_FOUND(UNAUTHORIZED, "리프레시 토큰이 없습니다."),
    AUTH_TOKEN_NOT_FOUND(UNAUTHORIZED, "인증 토큰이 없습니다."),
    AUTH_UNAUTHORIZED(UNAUTHORIZED, "인증되지 않은 사용자입니다."),
    AUTH_ACCESS_DENIED(FORBIDDEN, "접근 권한이 없습니다."),
    AUTH_HEADER_MISSING(UNAUTHORIZED, "Authorization 헤더가 필요합니다."),
    AUTH_INVALID_TOKEN_TYPE(UNAUTHORIZED, "올바르지 않은 토큰 타입입니다."),

    // JWT 관련 에러
    JWT_INVALID_SIGNATURE(UNAUTHORIZED, "JWT 서명이 유효하지 않습니다."),
    JWT_MALFORMED(UNAUTHORIZED, "잘못된 형식의 JWT입니다."),
    JWT_UNSUPPORTED(UNAUTHORIZED, "지원하지 않는 JWT입니다."),
    JWT_CLAIMS_EMPTY(UNAUTHORIZED, "JWT claims가 비어있습니다."),
    JWT_EXPIRED(UNAUTHORIZED, "JWT 토큰이 만료되었습니다."),

    // 리프레쉬 토큰 에러
    REFRESH_TOKEN_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "리프레시 토큰 저장에 실패했습니다."),
    REFRESH_TOKEN_DELETE_ERROR(BAD_REQUEST, "토큰 삭제에 실패했습니다."),

    // 매장 관련 에러
    STORE_NOT_FOUND(NOT_FOUND, "매장을 찾을 수 없습니다."),
    STORE_ACCESS_DENIED(FORBIDDEN, "매장 접근 권한이 없습니다."),
    STORE_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "매장 생성에 실패했습니다."),
    STORE_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "매장 정보 수정에 실패했습니다."),
    STORE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "매장 삭제에 실패했습니다."),
    STORE_OWNER_REQUIRED(FORBIDDEN, "매장 소유자 권한이 필요합니다."),
    STORE_MEMBER_NOT_FOUND(NOT_FOUND, "매장 구성원을 찾을 수 없습니다."),
    STORE_MEMBER_ALREADY_EXISTS(BAD_REQUEST, "이미 매장 구성원으로 등록되어 있습니다."),
    STORE_OWNER_CANNOT_LEAVE(FORBIDDEN, "소유자는 탈퇴할 수 없습니다. 매장을 다른 사람에게 양도하거나 삭제해야 합니다."),
    STORE_NAME_ALREADY_EXISTS(BAD_REQUEST, "이미 존재하는 매장 이름입니다."),
    SITE_LINK_ALREADY_EXISTS(BAD_REQUEST, "이미 사용 중인 사이트 링크입니다."),
    SITE_CANNOT_NULL(BAD_REQUEST, "사이트 링크는 null일 수 없습니다."),
    STORE_NOTICE_NOT_FOUND(NOT_FOUND, "가게 공지사항을 찾을 수 없습니다."),
    STORE_NOTICE_ACCESS_DENIED(FORBIDDEN, "가게 공지사항 접근 권한이 없습니다."),
    UNAUTHORIZED_STORE_NOTICE_ACCESS(FORBIDDEN, "해당 가게의 공지사항이 아닙니다."),

    // 메뉴 관련 에러
    MENU_NOT_FOUND(NOT_FOUND, "메뉴를 찾을 수 없습니다."),
    MENU_ACCESS_DENIED(FORBIDDEN, "메뉴 접근 권한이 없습니다."),
    MENU_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 생성에 실패했습니다."),
    MENU_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 수정에 실패했습니다."),
    MENU_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 삭제에 실패했습니다."),
    MENU_ITEM_NOT_FOUND(NOT_FOUND, "메뉴 항목을 찾을 수 없습니다."),
    MENU_ITEM_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 항목 생성에 실패했습니다."),
    MENU_ITEM_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 항목 수정에 실패했습니다."),
    MENU_ITEM_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 항목 삭제에 실패했습니다."),
    MENU_ITEM_NOT_IN_MENU(BAD_REQUEST, "해당 메뉴에 속한 아이템이 아닙니다."),
    
    // 음식 관련 에러
    FOOD_NOT_FOUND(NOT_FOUND, "음식을 찾을 수 없습니다."),
    FOOD_ACCESS_DENIED(FORBIDDEN, "음식 접근 권한이 없습니다."),
    FOOD_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "음식 등록에 실패했습니다."),
    FOOD_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "음식 정보 수정에 실패했습니다."),
    FOODITEM_NOT_FOUND(NOT_FOUND, "음식 아이템을 찾을 수 없습니다."),
    PHOTO_SET_FEATURED_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "대표 사진 설정에 실패했습니다."),
    
    // 사진 관련 에러
    PRESIGNED_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사진 업로드를 위한 URL 생성에 실패했습니다."),
    PHOTO_REGISTRATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사진 정보 등록에 실패했습니다."),
    PHOTO_NOT_FOUND(NOT_FOUND, "사진을 찾을 수 없습니다."),
    PHOTO_ACCESS_DENIED(FORBIDDEN, "사진 접근 권한이 없습니다."),
    PHOTO_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사진 업로드에 실패했습니다."),
    PHOTO_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사진 삭제에 실패했습니다."),
    PHOTO_INVALID_FORMAT(BAD_REQUEST, "지원하지 않는 사진 형식입니다."),
    PHOTO_SIZE_EXCEEDED(BAD_REQUEST, "사진 크기가 제한을 초과했습니다."),
    PHOTO_FEATURE_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "대표 사진 설정에 실패했습니다."),
    PHOTO_BACKGROUND_REMOVAL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "배경 제거 처리에 실패했습니다."),

    // 캠페인 관련 에러
    CAMPAIGN_NOT_FOUND(NOT_FOUND, "캠페인을 찾을 수 없습니다."),
    CAMPAIGN_ACCESS_DENIED(FORBIDDEN, "캠페인 접근 권한이 없습니다."),
    CAMPAIGN_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "캠페인 생성에 실패했습니다."),
    CAMPAIGN_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "캠페인 수정에 실패했습니다."),
    CAMPAIGN_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "캠페인 삭제에 실패했습니다."),
    CAMPAIGN_ALREADY_ACTIVE(BAD_REQUEST, "이미 활성화된 캠페인입니다."),
    CAMPAIGN_CANNOT_MODIFY(BAD_REQUEST, "활성 또는 완료된 캠페인은 수정할 수 없습니다."),
    CAMPAIGN_OVERLAPPING(BAD_REQUEST, "해당 기간에 이미 진행 중인 캠페인이 있습니다."),
    CAMPAIGN_INVALID_DATE_RANGE(BAD_REQUEST, "잘못된 캠페인 기간입니다."),
    CAMPAIGN_MISMATCH(BAD_REQUEST, "캠페인이 해당 매장 또는 음식과 일치하지 않습니다."),
    CAMPAIGN_TARGET_EXCEEDED(BAD_REQUEST, "목표 피드백 수가 제한을 초과했습니다."),

    // 알림 관련 에러
    NOTIFICATION_SERVICE_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "알림 서비스를 사용할 수 없습니다."),
    NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 전송에 실패했습니다."),
    NOTIFICATION_USER_NO_TOKENS(NOT_FOUND, "사용자의 활성화된 토큰이 없습니다."),
    NOTIFICATION_TOPIC_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토픽 알림 전송에 실패했습니다."),
    NOTIFICATION_TOKEN_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 알림 전송에 실패했습니다."),
    NOTIFICATION_MULTIPLE_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "다중 알림 전송에 실패했습니다."),

    // 앱 버전 관련 에러
    APP_VERSION_NOT_FOUND(NOT_FOUND, "앱 버전 정보를 찾을 수 없습니다."),
    APP_VERSION_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "앱 버전 생성에 실패했습니다."),
    APP_VERSION_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "앱 버전 수정에 실패했습니다."),
    APP_VERSION_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "앱 버전 삭제에 실패했습니다."),

    // 공지사항 관련 에러
    NOTICE_NOT_FOUND(NOT_FOUND, "공지사항을 찾을 수 없습니다."),
    NOTICE_INACTIVE(BAD_REQUEST, "비활성화된 공지사항입니다."),
    NOTICE_CATEGORY_INVALID(BAD_REQUEST, "잘못된 공지사항 카테고리입니다."),

    // 문의 관련 에러
    INQUIRY_NOT_FOUND(NOT_FOUND, "문의 정보를 찾을 수 없습니다."),
    INQUIRY_ACCESS_DENIED(FORBIDDEN, "본인의 문의가 아닙니다."),
    INQUIRY_CATEGORY_INVALID(BAD_REQUEST, "잘못된 문의 카테고리입니다."),
    INQUIRY_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "문의 등록에 실패했습니다."),
    INQUIRY_REPLY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "문의 답변 등록에 실패했습니다."),

    // FCM 관련 에러
    FCM_TOKEN_INVALID_REQUEST(BAD_REQUEST, "FCM 토큰 요청 데이터가 유효하지 않습니다."),
    FCM_TOKEN_NOT_FOUND(NOT_FOUND, "FCM 토큰을 찾을 수 없습니다."),
    FCM_TOKEN_REGISTRATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 토큰 등록에 실패했습니다."),
    FCM_TOKEN_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 토큰 업데이트에 실패했습니다."),
    FCM_TOKEN_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 토큰 삭제에 실패했습니다."),
    FCM_DEVICE_TYPE_INVALID(BAD_REQUEST, "지원하지 않는 디바이스 타입입니다."),
    FCM_TOKEN_ACCESS_DENIED(FORBIDDEN, "FCM 토큰에 대한 접근 권한이 없습니다."),
    FCM_SERVICE_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 서비스를 사용할 수 없습니다."),
    FCM_NOTIFICATION_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "알림 전송에 실패했습니다."),
    FCM_NOTIFICATION_INVALID_REQUEST(BAD_REQUEST, "알림 요청 데이터가 유효하지 않습니다."),
    FCM_USER_NO_ACTIVE_TOKENS(NOT_FOUND, "사용자의 활성화된 FCM 토큰이 없습니다."),

    // 알림 관련 에러
    NOTIFICATION_FIREBASE_INIT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Firebase 초기화에 실패했습니다."),

    // 공통 에러
    INVALID_REQUEST(BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_PARAMETER(BAD_REQUEST, "잘못된 파라미터입니다."),
    RESOURCE_NOT_FOUND(NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 오류가 발생했습니다."),

    // Guide
    GUIDE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 가이드를 찾을 수 없습니다."),
    SUB_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 하위 카테고리를 찾을 수 없습니다."),

    // S3
    S3_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 삭제에 실패했습니다."),

    // GuideTag
    TAG_ALREADY_EXISTS(HttpStatus.CONFLICT, "해당 가이드에 이미 존재하는 태그입니다."),
    GUIDE_TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 가이드-태그 관계를 찾을 수 없습니다."),

    // FCM
    FCM_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 메시지 전송에 실패했습니다."),

    // User Store Role
    USER_STORE_ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저의 가게 권한을 찾을 수 없습니다."),
    USER_DEACTIVATED_REJOIN_UNAVAILABLE(HttpStatus.FORBIDDEN, "탈퇴 후 30일 동안 재가입할 수 없습니다."),

    // Feedback
    FEEDBACK_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "피드백 등록에 실패했습니다."),
    FEEDBACK_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "피드백 내용이 비어있습니다."),
    FEEDBACK_NOT_FOUND(HttpStatus.NOT_FOUND, "피드백을 찾을 수 없습니다."),
    FEEDBACK_ACCESS_DENIED(HttpStatus.FORBIDDEN, "피드백에 대한 접근 권한이 없습니다."),

    // 리워드 관련 에러 
    INSUFFICIENT_REWARD_COUNT(HttpStatus.BAD_REQUEST, "보유 리워드 횟수가 부족합니다."),

    // Survey 관련 에러
    SURVEY_NOT_FOUND(HttpStatus.NOT_FOUND, "설문지를 찾을 수 없습니다."),
    SURVEY_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "설문 질문을 찾을 수 없습니다."),
    SURVEY_ANSWER_INVALID(HttpStatus.BAD_REQUEST, "설문 답변이 유효하지 않습니다."),

    // Reward 관련 에러
    REWARD_NOT_FOUND(HttpStatus.NOT_FOUND, "리워드를 찾을 수 없습니다."),
    REWARD_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "사용할 수 없는 리워드입니다."),
    REWARD_INSUFFICIENT_FEEDBACK(HttpStatus.BAD_REQUEST, "리워드 조건을 만족하지 않습니다."),
    REWARD_REDEMPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "리워드 교환에 실패했습니다."),
    REWARD_REDEMPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "리워드 교환 내역을 찾을 수 없습니다."),
    
    // JAR 관련 에러
    JAR_INSUFFICIENT_DATA(HttpStatus.BAD_REQUEST, "JAR 분석을 위한 데이터가 부족합니다."),
    JAR_INVALID_QUESTION(HttpStatus.BAD_REQUEST, "JAR 분석 대상이 아닌 질문입니다."),
    JAR_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JAR 분석 중 오류가 발생했습니다."),
    JAR_INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "유효하지 않은 날짜 범위입니다."),

    // 메뉴 추출 관련 에러
    MENU_EXTRACTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 추출에 실패했습니다."),
    MENU_EXTRACTION_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "이미 메뉴 추출이 진행 중입니다."),
    EXTRACTION_PROGRESS_NOT_FOUND(NOT_FOUND, "추출 진행 상태를 찾을 수 없습니다."),
    MENU_PARSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메뉴 텍스트 파싱에 실패했습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),

    // 쿠폰 관련 에러
    COUPON_PIN_NOT_FOUND(NOT_FOUND, "유효하지 않은 PIN입니다."),
    COUPON_PIN_EXPIRED(BAD_REQUEST, "만료된 PIN입니다."),
    COUPON_PIN_ALREADY_USED(BAD_REQUEST, "이미 사용된 PIN입니다."),
    COUPON_INSUFFICIENT_STAMPS(BAD_REQUEST, "스탬프가 부족합니다."),
    COUPON_MEMBERSHIP_NOT_FOUND(NOT_FOUND, "쿠폰 멤버십을 찾을 수 없습니다."),
    COUPON_INVALID_PHONE_NUMBER(BAD_REQUEST, "유효하지 않은 전화번호입니다."),
    COUPON_INVALID_STAMPS_COUNT(BAD_REQUEST, "스탬프 수는 1개 이상이어야 합니다."),
    COUPON_PIN_NOT_USED(BAD_REQUEST, "아직 사용되지 않은 pin입니다."),

    // 구독 관련 에러
    SUBSCRIPTION_ALREADY_UNSUBSCRIBED(BAD_REQUEST, "이미 구독 취소된 상태입니다."),
    SUBSCRIPTION_ALREADY_ACTIVE(BAD_REQUEST, "이미 구독 중입니다."),
    SUBSCRIPTION_NOT_ACTIVE(BAD_REQUEST, "활성화된 구독이 아닙니다."),
    SUBSCRIPTION_NOT_FOUND(NOT_FOUND, "구독 정보를 찾을 수 없습니다."),
    NOTIFICATION_ALREADY_ENABLED(BAD_REQUEST, "알림이 이미 활성화되어 있습니다."),
    NOTIFICATION_ALREADY_DISABLED(BAD_REQUEST, "알림이 이미 비활성화되어 있습니다."),
    NO_SUBSCRIBERS(BAD_REQUEST, "구독자가 없습니다."),
    NO_FCM_TOKENS(BAD_REQUEST, "발송 가능한 FCM 토큰이 없습니다."),
    NOTIFICATION_INVALID_STATUS(BAD_REQUEST, "유효하지 않은 알림 상태입니다."),
    STORE_PERMISSION_DENIED(FORBIDDEN, "매장 권한이 없습니다."),
    INVALID_INPUT_VALUE(BAD_REQUEST, "커스텀 링크에는 라벨이 있어야 합니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
