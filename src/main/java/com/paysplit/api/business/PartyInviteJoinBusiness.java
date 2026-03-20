package com.paysplit.api.business;

import com.paysplit.api.converter.PartyInviteJoinConverter;
import com.paysplit.api.dto.party.request.PartyJoinRequest;
import com.paysplit.api.dto.party.response.PartyJoinResponse;
import com.paysplit.api.service.*;
import com.paysplit.common.annotation.Business;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.Subscription;
import com.paysplit.db.domain.SubscriptionPlan;
import com.paysplit.db.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Business
@RequiredArgsConstructor
@Transactional
public class PartyInviteJoinBusiness {
    private final UserService userService;
    private final PartyService partyService;
    private final SubscriptionService subscriptionService;
    private final SubscriptionPlanService subscriptionPlanService;
    private final PartyMemberService partyMemberService;
    private final PartyInviteJoinConverter partyInviteJoinConverter;

    public PartyJoinResponse join(String inviteCode, PartyJoinRequest request) {
        Long userId = request.getUserId();

        // 사용자 확인
        User user = userService.getById(userId);

        // 탈퇴 유저 확인
        userService.validateNotWithdrawn(user);

        // 초대코드로 파티조회 & 파티 상태 모집중 체크
        Party party = partyService.getPartyByInviteCode(inviteCode);

        // 파티 꽉찼는지 확인
        Subscription subscription = subscriptionService.getByPartyId(party);
        SubscriptionPlan plan = subscriptionPlanService.getById(subscription.getPlanId());
        int currentNumber = partyMemberService.countActiveMembers(party.getId());
        partyMemberService.validatePartyNotFull(currentNumber, plan.getMaxMembers());

        // 같은 플랜에 파티 참여중인지 확인
        partyMemberService.validateNotAlreadyJoined(userId, plan.getId());

        // 파티 멤버 추가
        partyMemberService.createPartyMember(party, user);

        return partyInviteJoinConverter.toResponse(party.getId(), plan.getId(), plan.getName());
    }
}
