package com.paysplit.common.error;

import com.paysplit.api.response.ApiResult;
import com.paysplit.common.error.party.PartyException;
import com.paysplit.common.error.party_member.PartyMemberErrorCode;
import com.paysplit.common.error.party_member.PartyMemberException;
import com.paysplit.common.error.payment.PaymentException;
import com.paysplit.common.error.settlement.SettlementException;
import com.paysplit.common.error.subscription.SubscriptionException;
import com.paysplit.common.error.subscriptionplan.SubscriptionPlanException;
import com.paysplit.common.error.user.UserException;
import com.paysplit.common.error.user_auth.UserAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PartyMemberException.class)
    public ResponseEntity<ApiResult<?>> handlerPartyMemberException(PartyMemberException e) {
        ErrorCode code = e.getErrorCode();

        log.warn("PartyMemberException : code={}, message={}", code.getCode(), code.getMessage(), e);

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResult.error(code.getCode(), code.getMessage(), null));
    }

    @ExceptionHandler(UserAuthException.class)
    public ResponseEntity<ApiResult<?>> handlerUserAuthException(UserAuthException e) {
        ErrorCode code = e.getErrorCode();

        log.warn("UserAuthException : code={}, message={}", code.getCode(), code.getMessage(), e);

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResult.error(code.getCode(), code.getMessage(), null));
    }

    @ExceptionHandler(SubscriptionException.class)
    public ResponseEntity<ApiResult<?>> handlerSubscriptionException(SubscriptionException e) {
        ErrorCode code = e.getErrorCode();

        log.warn("SubscriptionException : code={}, message={}", code.getCode(), code.getMessage(), e);

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResult.error(code.getCode(), code.getMessage(), null));
    }

    @ExceptionHandler(SubscriptionPlanException.class)
    public ResponseEntity<ApiResult<?>> handlerSubscriptionPlanException(SubscriptionPlanException e) {
        ErrorCode code = e.getErrorCode();

        log.warn("SubscriptionPlanException : code={}, message={}", code.getCode(), code.getMessage(), e);

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResult.error(code.getCode(), code.getMessage(), null));
    }

    @ExceptionHandler(PartyException.class)
    public ResponseEntity<ApiResult<?>> handlerPartyException(PartyException e) {
        ErrorCode code = e.getErrorCode();

        log.warn("PartyException : code={}, message={}", code.getCode(), code.getMessage(), e);

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResult.error(code.getCode(), code.getMessage(), null));
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiResult<?>> handlerUserException(UserException e) {
        ErrorCode code = e.getErrorCode();

        log.warn("UserException : code={}, message={}", code.getCode(), code.getMessage(), e);

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResult.error(code.getCode(), code.getMessage(), null));
    }

    @ExceptionHandler(SettlementException.class)
    public ResponseEntity<ApiResult<?>> handlerSettlementException(SettlementException e) {
        ErrorCode code = e.getErrorCode();

        log.warn("SettlementException : code={}, message={}", code.getCode(), code.getMessage(), e);

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResult.error(code.getCode(), code.getMessage(), null));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResult<?>> handlerPaymentException(PaymentException e) {
        ErrorCode code = e.getErrorCode();

        log.warn("PaymentException : code={}, message={}", code.getCode(), code.getMessage(), e);

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResult.error(code.getCode(), code.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<?>> handlerException(Exception e) {
        CommonErrorCode code = CommonErrorCode.INTERNAL_SERVER_ERROR;

        log.error("Unhandled exception", e);

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResult.error(code.getCode(), code.getMessage(), null));
    }
}
