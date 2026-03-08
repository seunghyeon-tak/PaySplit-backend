package com.paysplit.api.business;

import com.paysplit.api.converter.PartyCreateConverter;
import com.paysplit.api.dto.party.request.PartyCreateRequest;
import com.paysplit.api.dto.party.response.PartyCreateResponse;
import com.paysplit.api.service.PartyMemberService;
import com.paysplit.api.service.PartyService;
import com.paysplit.api.service.UserService;
import com.paysplit.common.annotation.Business;
import com.paysplit.common.error.party.PartyErrorCode;
import com.paysplit.common.error.party.PartyException;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.PartyMember;
import com.paysplit.db.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Business
@RequiredArgsConstructor
@Transactional
public class PartyCreateBusiness {
    private final PartyService partyService;
    private final UserService userService;
    private final PartyMemberService partyMemberService;
    private final PartyCreateConverter partyCreateConverter;

    public PartyCreateResponse create(PartyCreateRequest request) {
        // 요청한 사용자 존재 확인
        Long userId = request.getUserId();
        User user = userService.getById(userId);

        // 탈퇴한 유저인지 확인
        userService.validateNotWithdrawn(user);

        // 초대코드 생성 (UUID 8자리) - 충돌 시 최대 3회 재시도
        String code = generateUniqueInviteCode();

        // Party 생성 (Status = RECRUITING, leaderId = 요청자)
        Party party = partyService.createParty(userId, code);

        // 파티장을 PartyMember로 추가 (status = active)
        partyMemberService.createPartyMember(party, user);

        // 생성된 파티 정보 반환
        return partyCreateConverter.toResponse(party);
    }

    private String generateUniqueInviteCode() {
        for (int attempt = 0; attempt < 3; attempt++) {
            String code = UUID.randomUUID().toString()
                    .replace("-", "")
                    .substring(0, 8)
                    .toUpperCase();

            if (!partyService.existInviteCode(code)) {
                return code;
            }
        }
        throw new PartyException(PartyErrorCode.INVITE_CODE_GENERATE_FAILED);
    }
}
