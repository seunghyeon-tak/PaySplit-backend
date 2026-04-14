package com.paysplit.api.service;

import com.paysplit.common.error.party.PartyErrorCode;
import com.paysplit.common.error.party.PartyException;
import com.paysplit.db.domain.Party;
import com.paysplit.db.enums.PartyStatus;
import com.paysplit.db.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PartyService {
    private final PartyRepository partyRepository;

    public Party getById(Long partyId) {
        return partyRepository.findById(partyId)
                .orElseThrow(() -> new PartyException(PartyErrorCode.PARTY_NOT_FOUND));
    }

    public Party createParty(Long userId, String code) {
        Party party = Party.builder()
                .leaderId(userId)
                .status(PartyStatus.RECRUITING)
                .inviteCode(code)
                .build();

        return partyRepository.save(party);
    }

    public boolean existInviteCode(String code) {
        return partyRepository.existsByInviteCode(code);
    }

    public Party getPartyByInviteCode(String inviteCode) {
        return partyRepository.findByInviteCodeAndStatus(inviteCode, PartyStatus.RECRUITING)
                .orElseThrow(() -> new PartyException(PartyErrorCode.PARTY_NOT_FOUND));
    }

    public Optional<Party> findAvailableParty(Long planId) {
        return partyRepository.findAvailableParty(planId);
    }
}
