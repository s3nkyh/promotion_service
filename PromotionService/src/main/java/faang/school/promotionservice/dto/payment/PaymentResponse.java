package faang.school.promotionservice.dto.payment;

import faang.school.promotionservice.dto.Currency;

import java.math.BigDecimal;

public record PaymentResponse(
        PaymentStatus status,
        String requestId,
        int verificationCode,
        long paymentNumber,
        BigDecimal amount,
        Currency currency,
        String message
) {
}
