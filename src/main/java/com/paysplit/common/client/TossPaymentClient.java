package com.paysplit.common.client;

import com.paysplit.common.client.dto.TossBillingKeyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    @Value("${toss.secret-key}")
    private String secretKey;

    @Value("${toss.base-url}")
    private String baseUrl;

    private final WebClient webClient;

    public TossBillingKeyResponse issueBillingKey(String customerKey, String authKey) {
        String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());

        return webClient.post()
                .uri(baseUrl + "/v1/billing/authorizations/issue")
                .header("Authorization", "Basic" + encodedKey)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "customerKey", customerKey,
                        "authKey", authKey
                ))
                .retrieve()
                .bodyToMono(TossBillingKeyResponse.class)
                .block();
    }
}
