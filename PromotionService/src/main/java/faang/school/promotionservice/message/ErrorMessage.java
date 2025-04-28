package faang.school.promotionservice.message;

public class ErrorMessage {
    public static final String TARIFF_PRICE_MISMATCH = "Payment amount does not match the tariff price";
    public static final String USER_NOT_FOUND = "It looks like you haven't registered." +
            " Please register and continue paying again.";
    public static final String PAYMENT_FAILED = "Payment failed";
    public static final String ACTIVE_PROMOTION_NOT_FOUND = "Active promotion not found";
    public static final String FAILED_TO_SYNCHRONIZATION = "Failed to synchronize promotions from Redis to DB";
}
