package com.paysplit.api.service;

import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.PartyMember;
import com.paysplit.db.domain.User;
import com.paysplit.db.enums.PartyMemberStatus;
import com.paysplit.db.repository.PartyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PartyMemberService {
    private final PartyMemberRepository partyMemberRepository;

    public PartyMember createPartyMember(Party party, User user) {
        PartyMember partyMember = PartyMember.builder()
                .party(party)
                .user(user)
                .status(PartyMemberStatus.ACTIVE)
                .build();

        return partyMemberRepository.save(partyMember);
    }
}
