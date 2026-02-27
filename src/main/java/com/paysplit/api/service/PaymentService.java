package com.paysplit.api.service;

import com.paysplit.common.error.payment.PaymentException;
import com.paysplit.db.domain.Payment;
import com.paysplit.db.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public Payment getById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(PAYMENT_NOT_FOUND));
    }
}
