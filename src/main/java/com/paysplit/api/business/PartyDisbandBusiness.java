package com.paysplit.api.business;

import com.paysplit.api.converter.PartyDisbandConverter;
import com.paysplit.api.dto.party.response.PartyDisbandResponse;
import com.paysplit.api.service.PartyMemberService;
import com.paysplit.api.service.PartyService;
import com.paysplit.api.service.SubscriptionService;
import com.paysplit.api.service.UserService;
import com.paysplit.common.annotation.Business;
import com.paysplit.common.error.subscription.SubscriptionErrorCode;
import com.paysplit.common.error.subscription.SubscriptionException;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.Subscription;
import com.paysplit.db.enums.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Business
@RequiredArgsConstructor
@Transactional
public class PartyDisbandBusiness {
    private final UserService userService;
    private final PartyService partyService;
    private final SubscriptionService subscriptionService;
    private final PartyMemberService partyMemberService;
    private final PartyDisbandConverter partyDisbandConverter;

    public PartyDisbandResponse disband(Long partyId, Long userId) {
        // 사용자 확인 (파티 해체는 파티장만 가능)
        userService.getById(userId);

        // 파티 존재 확인
        Party party = partyService.getById(partyId);

        // 파티장인지 확인
        partyService.validatePartyLeader(partyId, userId);

        // 구독상태 확인 - subscription
        Subscription subscription = subscriptionService.getByPartyId(party);

        // pending 즉시 해산
        if (subscription.getStatus() == SubscriptionStatus.PENDING) {
            // 파티 멤버 전원 left
            partyMemberService.leaveAllPartyMembers(partyId);
            // 구독 canceled
            subscriptionService.subscriptionCancel(subscription.getId());
            // 파티 disbanded, 초대 코드 null
            partyService.partyDisband(partyId);

            return partyDisbandConverter.toResponse(partyId, subscription.getPlan().getName(), LocalDateTime.now());
        } else if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {  // active 해산 예약
            // 스케줄러가 구독 만료일에 자동 처리
            partyService.requestDisband(partyId);

            return partyDisbandConverter.toResponse(partyId, subscription.getPlan().getName(), subscription.getEndedAt());
        } else {
            throw new SubscriptionException(SubscriptionErrorCode.SUBSCRIPTION_CANCEL_FAILED);
        }
    }
}
