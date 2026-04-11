package com.paysplit.common.scheduler;

import com.paysplit.db.domain.PartyMember;
import com.paysplit.db.repository.PartyMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PartyLeaveScheduler {
    private final PartyMemberRepository partyMemberRepository;

    @Scheduled(cron = "0 0 0 * * *")  // 매일 자정 실행
    public void processExpiredLeaveRequests() {
        log.info("#### 파티 탈퇴 예약 처리 스케줄러 실행 ####");

        // leaveRequestAt이 있고 구독 만료된 멤버 조회 후 LEFT 처리
        List<PartyMember> members = partyMemberRepository.findExpiredLeaveRequests(LocalDateTime.now());

        for (PartyMember partyMember : members) {
            partyMember.leave();
        }
    }
}
