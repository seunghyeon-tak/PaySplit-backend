package com.paysplit.support;

import com.paysplit.db.domain.Party;
import com.paysplit.db.enums.PartyStatus;

public class PartyFixture {
    public static Party recuitingParty(Long leaderId) {
        return Party.builder()
                .leaderId(leaderId)
                .status(PartyStatus.RECRUITING)
                .inviteCode("TESTCODE999")
                .build();
    }

    public static Party activeParty(Long leaderId) {
        return Party.builder()
                .leaderId(leaderId)
                .status(PartyStatus.ACTIVE)
                .inviteCode("TESTCODE123")
                .build();
    }
}
