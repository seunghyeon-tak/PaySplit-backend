package com.paysplit.api.service;

import com.paysplit.common.error.payment.PaymentErrorCode;
import com.paysplit.common.error.payment.PaymentException;
import com.paysplit.db.domain.Payment;
import com.paysplit.db.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.paysplit.common.error.payment.PaymentErrorCode.PAYMENT_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    /*
    * 정산 실행 (중복 호출) 방지를 위해 비관락으로 결제 row를 잠금
    * */
    public Payment getByIdForUpdate(Long paymentId) {
        return paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new PaymentException(PAYMENT_NOT_FOUND));
    }

    @Transactional
    public void settleIfNotSettled(Long paymentId) {
        int updated = paymentRepository.markSettledIfNotSettled(paymentId, LocalDateTime.now());

        if (updated == 0) {
            // 이미 정산됨
            throw new PaymentException(PaymentErrorCode.INVALID_PAYMENT_STATE);
        }
    }
}
