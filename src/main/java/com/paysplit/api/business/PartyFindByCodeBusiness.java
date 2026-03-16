package com.paysplit.api.business;

import com.paysplit.api.converter.PartyFindByCodeConverter;
import com.paysplit.api.dto.party.request.PartyFindByCodeRequest;
import com.paysplit.api.dto.party.response.PartyFindByCodeResponse;
import com.paysplit.api.service.*;
import com.paysplit.common.annotation.Business;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.Subscription;
import com.paysplit.db.domain.SubscriptionPlan;
import com.paysplit.db.domain.User;
import com.paysplit.db.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Business
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartyFindByCodeBusiness {
    private final PartyService partyService;
    private final SubscriptionService subscriptionService;
    private final SubscriptionPlanService subscriptionPlanService;
    private final UserService userService;
    private final PartyMemberService partyMemberService;
    private final PartyFindByCodeConverter partyFindByCodeConverter;

    public PartyFindByCodeResponse get(PartyFindByCodeRequest request) {
        // 초대 코드로 파티 조회 (없으면 -> NOT FOUND)
        // 파티 상태 확인 (RECRUITING이 아니면 NOT FOUND)
        Party party = partyService.getPartyByInviteCode(request.getInviteCode());

        // 구독 플랜 조회 (플랫폼 명, 최대 인원)
        Subscription subscription = subscriptionService.getByPartyId(party);
        SubscriptionPlan plan = subscriptionPlanService.getById(subscription.getPlanId());
        String platformName = plan.getPlatform().getName();
        int maxMembers = plan.getMaxMembers();

        // 파티장 정보 조회 (파티장 이름)
        Long leaderId = party.getLeaderId();
        User user = userService.getById(leaderId);

        // 현재 active 파티 멤버 수 조회
        int currentMembers = partyMemberService.countActiveMembers(party.getId());

        // 응답 반환
        return partyFindByCodeConverter.toResponse(party, platformName, user.getName(), currentMembers, maxMembers);
    }
}
