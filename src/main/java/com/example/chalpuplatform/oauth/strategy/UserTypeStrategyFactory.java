package com.example.chalpuplatform.oauth.strategy;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 사용자 타입별 전략 팩토리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserTypeStrategyFactory {
    
    private final List<UserTypeStrategy> strategies;
    private final Map<String, UserTypeStrategy> strategyMap = new HashMap<>();
    
    @PostConstruct
    public void init() {
        strategies.forEach(strategy -> {
            strategyMap.put(strategy.getUserType(), strategy);
            log.info("UserTypeStrategy 등록: {}", strategy.getUserType());
        });
    }
    
    /**
     * 사용자 타입에 따른 전략 반환
     * @param userType 사용자 타입
     * @return 해당하는 전략 (없으면 customer 기본값)
     */
    public UserTypeStrategy getStrategy(String userType) {
        if (userType == null) {
            log.debug("userType이 null이므로 기본값 customer 사용");
            return strategyMap.get("customer");
        }
        
        UserTypeStrategy strategy = strategyMap.get(userType.toLowerCase());
        if (strategy == null) {
            log.debug("알 수 없는 userType: {}, 기본값 customer 사용", userType);
            return strategyMap.get("customer");
        }
        
        return strategy;
    }
}