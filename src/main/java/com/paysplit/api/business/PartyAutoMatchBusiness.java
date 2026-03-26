package com.paysplit.api.business;

import com.paysplit.api.converter.PartyAutoMatchConverter;
import com.paysplit.api.dto.party.request.PartyAutoMatchRequest;
import com.paysplit.api.dto.party.response.PartyAutoMatchResponse;
import com.paysplit.api.service.*;
import com.paysplit.common.annotation.Business;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.SubscriptionPlan;
import com.paysplit.db.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Business
@RequiredArgsConstructor
@Transactional
public class PartyAutoMatchBusiness {
    private final UserService userService;
    private final PartyMemberService partyMemberService;
    private final SubscriptionPlanService subscriptionPlanService;
    private final PartyService partyService;
    private final PartyAutoMatchConverter partyAutoMatchConverter;
    private final WaitingQueueService waitingQueueService;

    public PartyAutoMatchResponse auto(PartyAutoMatchRequest request) {
        Long userId = request.getUserId();
        Long planId = request.getPlanId();

        // 사용자 확인
        User user = userService.getById(userId);

        // 탈퇴 유저 확인
        userService.validateNotWithdrawn(user);

        // 이미 같은 플랜에 참여 중인지 확인
        partyMemberService.validateNotAlreadyJoined(userId, planId);

        SubscriptionPlan plan = subscriptionPlanService.getById(planId);

        // planID로 recruiting 상태이고, 자리 있는 파티 조회
        Optional<Party> availableParty = partyService.findAvailableParty(planId);
        if (availableParty.isPresent()) {
            // - 있으면 파티 멤버 추가 JOINED
            partyMemberService.createPartyMember(availableParty.get(), user);
            log.info("자동 완료 - partyId : {}, userId : {}", availableParty.get().getId(), userId);
            return partyAutoMatchConverter.toResponse("JOINED", availableParty.get().getId(), plan.getId(), plan.getName());
        } else {
            // - 없으면 Redis 대기 큐 추가 WAITING
            waitingQueueService.addToWaitingQueue(planId, userId);
            log.info("자동 매칭 대기 큐 추가 - planId : {}, userId : {}", planId, userId);
            return partyAutoMatchConverter.toResponse("WAITING", null, plan.getId(), plan.getName());
        }
    }
}
