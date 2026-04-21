package com.paysplit.api.service;

import com.paysplit.common.client.dto.TossBillingKeyResponse;
import com.paysplit.common.error.user.UserErrorCode;
import com.paysplit.common.error.user.UserException;
import com.paysplit.db.domain.BillingKey;
import com.paysplit.db.domain.User;
import com.paysplit.db.repository.BillingKeyRepository;
import com.paysplit.db.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BillingKeyService {
    private final BillingKeyRepository billingKeyRepository;
    private final UserRepository userRepository;

    public BillingKey save(Long userId, TossBillingKeyResponse tossResponse) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        BillingKey billingKey = BillingKey.builder()
                .user(user)
                .billingKey(tossResponse.getBillingKey())
                .customerKey(tossResponse.getCustomerKey())
                .mid(tossResponse.getMid())
                .method(tossResponse.getMethod())
                .authenticatedAt(LocalDateTime.parse(tossResponse.getAuthenticatedAt(),
                        DateTimeFormatter.ISO_OFFSET_TIME))
                .cardIssuerCode(tossResponse.getCard().getIssuerCode())
                .cardAcquirerCode(tossResponse.getCard().getAcquirerCode())
                .cardNumber(tossResponse.getCard().getNumber())
                .cardType(tossResponse.getCard().getCardType())
                .cardOwnerType(tossResponse.getCard().getOwnerType())
                .build();

        return billingKeyRepository.save(billingKey);
    }
}
