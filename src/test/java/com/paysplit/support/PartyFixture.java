package com.paysplit.support;

import com.paysplit.db.domain.Party;
import com.paysplit.db.enums.PartyStatus;

public class PartyFixture {
    public static Party recuitingParty() {
        return Party.builder()
                .leaderId(999L)
                .status(PartyStatus.RECRUITING)
                .inviteCode("TESTCODE999")
                .build();
    }
}
