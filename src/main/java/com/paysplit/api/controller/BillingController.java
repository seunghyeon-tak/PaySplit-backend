package com.paysplit.api.controller;

import com.paysplit.api.business.BillingKeyBusiness;
import com.paysplit.api.dto.billing.response.BillingKeyResponse;
import com.paysplit.api.response.ApiResult;
import com.paysplit.common.jwt.JwtProvider;
import com.paysplit.common.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
public class BillingController {
    private final BillingKeyBusiness billingKeyBusiness;

    @GetMapping("/success")
    public ApiResult<BillingKeyResponse> success(@RequestParam String customerKey, @RequestParam String authKey) {
        Long userId = SecurityUtils.getCurrentUserId();
        BillingKeyResponse response = billingKeyBusiness.issueBillingKey(userId, customerKey, authKey);
        return ApiResult.success(response);
    }

    @GetMapping("/fail")
    public ApiResult<Void> fail(@RequestParam String code, @RequestParam String message) {
        return ApiResult.error(code, message, null);
    }
}
