package com.example.chalpuplatform.common.config;

import io.sentry.Sentry;
import io.sentry.SentryLevel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@Profile({"dev", "prod"})
public class SentryConfig {

    @Value("${sentry.dsn:}")
    private String sentryDsn;

    @Value("${sentry.environment:dev}")
    private String environment;

    @Value("${sentry.traces-sample-rate:1.0}")
    private Double tracesSampleRate;

    @Value("${spring.application.name:chalpu-platform}")
    private String appName;

    @PostConstruct
    public void init() {
        if (sentryDsn == null || sentryDsn.isEmpty()) {
            log.warn("Sentry DSN이 설정되지 않았습니다. Sentry가 비활성화됩니다.");
            return;
        }

        try {
            Sentry.init(options -> {
                options.setDsn(sentryDsn);
                options.setEnvironment(environment);
                options.setTracesSampleRate(tracesSampleRate);
                options.setAttachThreads(true);
                options.setAttachStacktrace(true);
                options.setSendDefaultPii(false);
                
                // 에러 필터링 - 특정 예외는 Sentry에 보고하지 않음
                options.setBeforeSend((event, hint) -> {
                    // 404 에러는 제외
                    if (event.getLevel() == SentryLevel.ERROR) {
                        String message = event.getMessage() != null ? event.getMessage().getMessage() : "";
                        if (message.contains("404") || message.contains("Not Found")) {
                            return null;
                        }
                    }
                    return event;
                });
                
                // 태그 설정
                options.setTag("application", appName);
                options.setTag("environment", environment);
                
                // 릴리즈 정보 (선택사항)
                // options.setRelease("chalpu-platform@" + version);
            });

            log.info("Sentry 초기화 완료 - Environment: {}, Sample Rate: {}", environment, tracesSampleRate);
        } catch (Exception e) {
            log.error("Sentry 초기화 실패", e);
        }
    }
}