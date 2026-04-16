package com.paysplit.common.scheduler;

import com.paysplit.db.domain.Party;
import com.paysplit.db.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PartyDisbandScheduler {
    private final PartyRepository partyRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void processDisbandRequests() {
        log.info("파티 해산 예약 처리 스케줄러 실행");

        // disbandRequestAt이 있고 구독 만료된 파티 조회 후 disbanded 처리
        List<Party> parties = partyRepository.findPartyDisbandRequests(LocalDateTime.now());
        parties.forEach(party -> {
            party.disband();
            log.info("파티 해산 처리 완료 - partyId: {}", party.getId());
        });
    }
}
