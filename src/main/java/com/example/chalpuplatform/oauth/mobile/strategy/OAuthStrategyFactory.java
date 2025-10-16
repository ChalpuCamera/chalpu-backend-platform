package com.example.chalpuplatform.oauth.mobile.strategy;

import com.example.chalpuplatform.common.exception.ErrorMessage;
import com.example.chalpuplatform.common.exception.OAuthException;
import com.example.chalpuplatform.oauth.model.AuthProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OAuthStrategyFactory {

    private final Map<AuthProvider, OAuthStrategy> strategies;

    public OAuthStrategyFactory(List<OAuthStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(
                        OAuthStrategy::getProvider,
                        Function.identity()
                ));

        log.info("OAuthStrategyFactory initialized with {} strategies: {}",
                strategies.size(),
                strategies.keySet());
    }

    public OAuthStrategy getStrategy(AuthProvider provider) {
        OAuthStrategy strategy = strategies.get(provider);

        if (strategy == null) {
            log.error("Unsupported OAuth provider: {}", provider);
            throw new OAuthException(ErrorMessage.OAUTH_PROVIDER_NOT_SUPPORTED);
        }

        return strategy;
    }
}
