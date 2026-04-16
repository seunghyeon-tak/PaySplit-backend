package com.paysplit.api.controller;

import com.paysplit.api.business.*;
import com.paysplit.api.dto.party.request.PartyAutoMatchRequest;
import com.paysplit.api.dto.party.request.PartyCreateRequest;
import com.paysplit.api.dto.party.response.*;
import com.paysplit.api.response.ApiResult;
import com.paysplit.common.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Party", description = "파티 관련 API")
@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
public class PartyController {
    private final PartyCreateBusiness partyCreateBusiness;
    private final PartyFindByCodeBusiness partyFindByCodeBusiness;
    private final PartyInviteJoinBusiness partyInviteJoinBusiness;
    private final PartyAutoMatchBusiness partyAutoJoinBusiness;
    private final PartyLeaveBusiness partyLeaveBusiness;

    @Operation(
            summary = "파티 생성",
            description = "새로운 파티를 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공"),
                    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "USER_001",
                                                    summary = "이미 가입 했습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "USER_001",
                                                                "messsage": "이미 가입 했습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "PLAN_001",
                                                    summary = "구독 플랜 정보를 찾을 수 없습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "PLAN_001",
                                                                "messsage": "구독 플랜 정보를 찾을 수 없습니다",
                                                                "data": null
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "BAD_REQUEST",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "USER_002",
                                                    summary = "이미 탈퇴한 사용자 입니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "USER_002",
                                                                "messsage": "이미 탈퇴한 사용자 입니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "PLAN_002",
                                                    summary = "비활성화된 구독 플랜입니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "PLAN_002",
                                                                "messsage": "비활성화된 구독 플랜입니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "PARTY_001",
                                                    summary = "초대 코드 생성 실패",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "PARTY_001",
                                                                "messsage": "초대 코드 생성 실패",
                                                                "data": null
                                                            }
                                                            """
                                            )
                                    }
                            )
                    )
            }
    )
    @PostMapping
    public ApiResult<PartyCreateResponse> create(@Valid @RequestBody PartyCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        PartyCreateResponse response = partyCreateBusiness.create(userId, request);
        return ApiResult.success(response);
    }

    @Operation(
            summary = "파티코드 조회",
            description = "초대코드로 파티 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공"),
                    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "PARTY_002",
                                                    summary = "파티가 존재 하지 않습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "PARTY_002",
                                                                "message": "파티가 존재 하지 않습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "SUBSCRIPTION_001",
                                                    summary = "구독이 존재하지 않습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "SUBSCRIPTION_001",
                                                                "message": "구독이 존재하지 않습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "PLAN_001",
                                                    summary = "구독 플랜 정보를 찾을 수 없습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "PLAN_001",
                                                                "message": "구독 플랜 정보를 찾을 수 없습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "USER_001",
                                                    summary = "사용자 정보를 찾을 수 없습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "USER_001",
                                                                "message": "사용자 정보를 찾을 수 없습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "BAD_REQUEST",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "PLAN_002",
                                                    summary = "비활성화된 구독 플랜입니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "PLAN_002",
                                                                "message": "비활성화된 구독 플랜입니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                    }
                            )
                    )
            }
    )
    @GetMapping
    public ApiResult<PartyFindByCodeResponse> codeGetParties(@RequestParam String inviteCode) {
        PartyFindByCodeResponse response = partyFindByCodeBusiness.get(inviteCode);
        return ApiResult.success(response);
    }

    @Operation(
            summary = "파티 코드로 파티 참여",
            description = "초대코드로 파티 참여",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공"),
                    @ApiResponse(responseCode = "400", description = "BAD_REQUEST",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "USER_002",
                                                    summary = "이미 탈퇴한 사용자 입니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "USER_002",
                                                                "message": "이미 탈퇴한 사용자 입니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "PARTY_003",
                                                    summary = "파티멤버가 가득 찼습니다.",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "PARTY_003",
                                                                "message": "파티멤버가 가득 찼습니다.",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "USER_001",
                                                    summary = "사용자 정보를 찾을 수 없습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "USER_001",
                                                                "message": "사용자 정보를 찾을 수 없습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "PARTY_002",
                                                    summary = "파티가 존재 하지 않습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "PARTY_002",
                                                                "message": "파티가 존재 하지 않습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "SUBSCRIPTION_001",
                                                    summary = "구독이 존재하지 않습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "SUBSCRIPTION_001",
                                                                "message": "구독이 존재하지 않습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "PLAN_001",
                                                    summary = "구독 플랜 정보를 찾을 수 없습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "PLAN_001",
                                                                "message": "구독 플랜 정보를 찾을 수 없습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "409", description = "CONFLICT",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "PARTY_004",
                                                    summary = "이미 가입 했습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "PARTY_004",
                                                                "message": "이미 가입 했습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                    }
                            )
                    ),
            }
    )
    @PostMapping("/join")
    public ApiResult<PartyJoinResponse> joinPartyMember(@RequestParam String inviteCode) {
        Long userId = SecurityUtils.getCurrentUserId();
        PartyJoinResponse response = partyInviteJoinBusiness.join(inviteCode, userId);
        return ApiResult.success(response);
    }

    @Operation(
            summary = "파티 자동 매칭",
            description = "파티 자동 매칭",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공"),
                    @ApiResponse(responseCode = "400", description = "BAD_REQUEST",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "USER_002",
                                                    summary = "이미 탈퇴한 사용자 입니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "USER_002",
                                                                "message": "이미 탈퇴한 사용자 입니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "PLAN_002",
                                                    summary = "비활성화된 구독 플랜입니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "PLAN_002",
                                                                "message": "비활성화된 구독 플랜입니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "NOT_FOUND",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "USER_001",
                                                    summary = "사용자 정보를 찾을 수 없습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "USER_001",
                                                                "message": "사용자 정보를 찾을 수 없습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "PLAN_001",
                                                    summary = "구독 플랜 정보를 찾을 수 없습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "PLAN_001",
                                                                "message": "구독 플랜 정보를 찾을 수 없습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                    }
                            )
                    ),
                    @ApiResponse(responseCode = "409", description = "CONFLICT",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "PARTY_004",
                                                    summary = "이미 가입 했습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "PARTY_004",
                                                                "message": "이미 가입 했습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                    }
                            )
                    ),
            }
    )
    @PostMapping("/auto")
    public ApiResult<PartyAutoMatchResponse> autoPartyMember(@Valid @RequestBody PartyAutoMatchRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        PartyAutoMatchResponse response = partyAutoJoinBusiness.auto(userId, request);
        return ApiResult.success(response);
    }

    @DeleteMapping("/{partyId}/members/me")
    public ApiResult<PartyLeaveResponse> leave(@PathVariable Long partyId) {
        Long userId = SecurityUtils.getCurrentUserId();
        PartyLeaveResponse response = partyLeaveBusiness.leave(partyId, userId);
        return ApiResult.success(response);
    }
}
