package faang.school.promotionservice.exception;

public class SyncFromRedisToDbException extends RuntimeException {
    public SyncFromRedisToDbException(String message) {
        super(message);
    }
}
