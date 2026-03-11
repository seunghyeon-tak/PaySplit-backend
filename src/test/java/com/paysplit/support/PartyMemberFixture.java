package com.paysplit.support;

import com.paysplit.db.domain.Party;
import com.paysplit.db.domain.PartyMember;
import com.paysplit.db.domain.User;
import com.paysplit.db.enums.PartyMemberStatus;

public class PartyMemberFixture {
    public static PartyMember createPartyMember(Party party, User user) {
        return PartyMember.builder()
                .party(party)
                .user(user)
                .status(PartyMemberStatus.ACTIVE)
                .build();
    }
}
