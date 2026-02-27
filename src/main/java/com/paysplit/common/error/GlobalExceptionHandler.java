package com.paysplit.common.error;

import com.paysplit.api.response.ApiResponse;
import com.paysplit.common.error.payment.PaymentException;
import com.paysplit.common.error.settlement.SettlementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SettlementException.class)
    public ResponseEntity<ApiResponse<?>> handlerSettlementException(SettlementException e) {
        ErrorCode code = e.getErrorCode();

        log.warn("SettlementException : code={}, message={}", code.getCode(), code.getMessage(), e);

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.error(code.getCode(), code.getMessage(), null));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<?>> handlerPaymentException(PaymentException e) {
        ErrorCode code = e.getErrorCode();

        log.warn("PaymentException : code={}, message={}", code.getCode(), code.getMessage(), e);

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.error(code.getCode(), code.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handlerException(Exception e) {
        CommonErrorCode code = CommonErrorCode.INTERNAL_SERVER_ERROR;

        log.error("Unhandled exception", e);

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.error(code.getCode(), code.getMessage(), null));
    }
}
