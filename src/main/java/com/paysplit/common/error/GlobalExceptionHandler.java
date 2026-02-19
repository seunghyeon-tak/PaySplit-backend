package com.paysplit.common.error;

import com.paysplit.api.response.ApiResponse;
import com.paysplit.common.error.payment.PaymentException;
import com.paysplit.common.error.settlement.SettlementException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SettlementException.class)
    public ResponseEntity<ApiResponse<?>> handlerSettlementException(SettlementException e) {
        ErrorCode code = e.getErrorCode();

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.error(code.getCode(), code.getMessage(), null));
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<ApiResponse<?>> handlerPaymentException(PaymentException e) {
        ErrorCode code = e.getErrorCode();

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.error(code.getCode(), code.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handlerException(Exception e) {
        CommonErrorCode code = CommonErrorCode.INTERNAL_SERVER_ERROR;

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.error(code.getCode(), code.getMessage(), null));
    }
}
