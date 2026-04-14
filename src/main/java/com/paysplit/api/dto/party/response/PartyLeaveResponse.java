package com.paysplit.api.dto.party.response;

import com.paysplit.db.enums.LeaveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PartyLeaveResponse {
    @Schema(name = "user_id", description = "유저 ID", example = "1")
    private Long userId;

    @Schema(name = "party_id", description = "파티 ID", example = "4")
    private Long partyId;

    @Schema(name = "plan_name", description = "구독 플랫폼 이름", example = "넷플릭스")
    private String planName;

    @Schema(name = "leave_date", description = "탈퇴 처리 날짜 (즉시 탈퇴 시 현재 시간, 예약 탈퇴 시 구독 만료일)", example = "2026-01-25T00:00:00")
    private LocalDateTime leaveDate;

    @Schema(description = "탈퇴 상태 (IMMEDIATE: 즉시 탈퇴, RESERVED: 탈퇴 예약)", example = "IMMEDIATE")
    private LeaveStatus status;
}
