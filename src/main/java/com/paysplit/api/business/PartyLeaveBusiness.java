package com.paysplit.api.business;

import com.paysplit.api.converter.PartyLeaveConverter;
import com.paysplit.api.dto.party.response.PartyLeaveResponse;
import com.paysplit.api.service.PartyMemberService;
import com.paysplit.api.service.PartyService;
import com.paysplit.api.service.SubscriptionService;
import com.paysplit.api.service.UserService;
import com.paysplit.common.annotation.Business;
import com.paysplit.common.error.party.PartyErrorCode;
import com.paysplit.common.error.party.PartyException;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.PartyMember;
import com.paysplit.db.domain.Subscription;
import com.paysplit.db.domain.User;
import com.paysplit.db.enums.LeaveStatus;
import com.paysplit.db.enums.SubscriptionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Business
@RequiredArgsConstructor
@Transactional
public class PartyLeaveBusiness {
    private final UserService userService;
    private final PartyMemberService partyMemberService;
    private final PartyService partyService;
    private final SubscriptionService subscriptionService;
    private final PartyLeaveConverter partyLeaveConverter;

    public PartyLeaveResponse leave(Long partyId, Long userId) {
        // 사용자 확인
        User user = userService.getById(userId);

        // 파티 존재 확인
        Party party = partyService.getById(partyId);

        // 파티 멤버인지 확인
        PartyMember partyMember = partyMemberService.getByPartyAndUser(party, user);

        // 파티장이면 탈퇴 불가 (파티장은 해산으로 처리 해야함)
        if (party.getLeaderId().equals(user.getId())) {
            throw new PartyException(PartyErrorCode.PARTY_LEADER_LEAVE_FAILED);
        }

        // 구독 상태 확인
        Subscription subscription = subscriptionService.getByPartyId(party);

        LeaveStatus leaveStatus;

        if (subscription.getStatus() == SubscriptionStatus.PENDING) {
            // pending -> 즉시 탈퇴
            partyMemberService.leavePartyMember(user, party);
            leaveStatus = LeaveStatus.IMMEDIATE;
        } else if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            // active -> 탈퇴 예약
            partyMemberService.requestLeavePartyMember(user, party);
            leaveStatus = LeaveStatus.RESERVED;
        } else {
            throw new PartyException(PartyErrorCode.PARTY_LEAVE_FAILED);
        }

        // 응답 반환
        return partyLeaveConverter.toResponse(user, party, subscription, partyMember, leaveStatus);
    }
}
