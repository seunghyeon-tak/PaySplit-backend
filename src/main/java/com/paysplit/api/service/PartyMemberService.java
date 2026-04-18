package com.paysplit.api.service;

import com.paysplit.common.error.party.PartyErrorCode;
import com.paysplit.common.error.party.PartyException;
import com.paysplit.common.error.party_member.PartyMemberErrorCode;
import com.paysplit.common.error.party_member.PartyMemberException;
import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.PartyMember;
import com.paysplit.db.domain.User;
import com.paysplit.db.enums.PartyMemberStatus;
import com.paysplit.db.repository.PartyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartyMemberService {
    private final PartyMemberRepository partyMemberRepository;

    public PartyMember getByPartyAndUser(Party party, User user) {
        return partyMemberRepository.findByPartyAndUser(party, user)
                .orElseThrow(() -> new PartyMemberException(PartyMemberErrorCode.PARTY_MEMBER_NOT_FOUND));
    }

    public PartyMember createPartyMember(Party party, User user) {
        PartyMember partyMember = PartyMember.builder()
                .party(party)
                .user(user)
                .status(PartyMemberStatus.ACTIVE)
                .build();

        return partyMemberRepository.save(partyMember);
    }

    public int countActiveMembers(Long partyId) {
        return partyMemberRepository.countByPartyIdAndStatus(partyId, PartyMemberStatus.ACTIVE);
    }

    public void validatePartyNotFull(int currentMember, int partyMax) {
        if (currentMember == partyMax) {
            throw new PartyException(PartyErrorCode.PARTY_MEMBER_FULL);
        }
    }

    public void validateNotAlreadyJoined(Long userId, Long planId) {
        if (partyMemberRepository.existsActiveByUserIdAndPlanId(userId, planId, PartyMemberStatus.ACTIVE)) {
            throw new PartyException(PartyErrorCode.ALREADY_JOINED);
        }
    }

    public void leavePartyMember(User user, Party party) {
        PartyMember partyMember = partyMemberRepository.findByPartyAndUser(party, user)
                .orElseThrow(() -> new PartyMemberException(PartyMemberErrorCode.PARTY_MEMBER_NOT_FOUND));

        partyMember.leave();
    }

    public void requestLeavePartyMember(User user, Party party) {
        PartyMember partyMember = partyMemberRepository.findByPartyAndUser(party, user)
                .orElseThrow(() -> new PartyMemberException(PartyMemberErrorCode.PARTY_MEMBER_NOT_FOUND));

        partyMember.requestLeave();
    }

    public void leaveAllPartyMembers(Long partyId) {
        List<PartyMember> partyMemberList = partyMemberRepository.findByPartyIdAndStatus(partyId, PartyMemberStatus.ACTIVE);

        for (PartyMember member : partyMemberList) {
            member.leave();
        }
    }
}
