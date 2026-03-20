package com.paysplit.api.controller;

import com.paysplit.api.business.SettlementExecuteBusiness;
import com.paysplit.api.dto.settlement.request.SettlementExecuteRequest;
import com.paysplit.api.dto.settlement.response.SettlementExecuteResponse;
import com.paysplit.api.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "정산", description = "정산 관련 API")
@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementExecuteController {
    private final SettlementExecuteBusiness settlementExecuteBusiness;

    @Operation(summary = "정산 생성", description = "정산을 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공"),
                    @ApiResponse(responseCode = "400", description = "BAD_REQUEST",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = {
                                            @ExampleObject(
                                                    name = "SETTLEMENT_007",
                                                    summary = "REVERSAL 또는 ADJUSTMENT 정산에는 원본 정산이 필요합니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "SETTLEMENT_007",
                                                                "messsage": "REVERSAL 또는 ADJUSTMENT 정산에는 원본 정산이 필요합니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "SETTLEMENT_009",
                                                    summary = "원본 정산의 결제 정보가 일치하지 않습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "SETTLEMENT_009",
                                                                "messsage": "원본 정산의 결제 정보가 일치하지 않습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "SETTLEMENT_005",
                                                    summary = "정산 인원 수가 올바르지 않습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "SETTLEMENT_005",
                                                                "messsage": "정산 인원 수가 올바르지 않습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "SETTLEMENT_004",
                                                    summary = "정산 분배 금액이 올바르지 않습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "SETTLEMENT_004",
                                                                "messsage": "정산 분배 금액이 올바르지 않습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "SETTLEMENT_002",
                                                    summary = "현재 상태에서는 정산을 진행할 수 없습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "SETTLEMENT_002",
                                                                "messsage": "현재 상태에서는 정산을 진행할 수 없습니다",
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
                                                    name = "PAYMENT_001",
                                                    summary = "결제 정보를 찾을 수 없습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "PAYMENT_001",
                                                                "messsage": "결제 정보를 찾을 수 없습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "SETTLEMENT_008",
                                                    summary = "원본 정산 정보를 찾을 수 없습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "SETTLEMENT_008",
                                                                "messsage": "원본 정산 정보를 찾을 수 없습니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "SETTLEMENT_001",
                                                    summary = "정산 정보를 찾을 수 없습니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "SETTLEMENT_001",
                                                                "messsage": "정산 정보를 찾을 수 없습니다",
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
                                                    name = "PAYMENT_003",
                                                    summary = "이미 정산이 완료된 결제입니다",
                                                    value = """
                                                            {
                                                                "success": false,
                                                                "code": "PAYMENT_003",
                                                                "messsage": "이미 정산이 완료된 결제입니다",
                                                                "data": null
                                                            }
                                                            """
                                            ),
                                    }
                            )
                    ),
            }
    )
    @PostMapping
    public ApiResult<SettlementExecuteResponse> create(@Valid @RequestBody SettlementExecuteRequest request) {
        SettlementExecuteResponse response = settlementExecuteBusiness.execute(request);

        return ApiResult.success(response);
    }
}
