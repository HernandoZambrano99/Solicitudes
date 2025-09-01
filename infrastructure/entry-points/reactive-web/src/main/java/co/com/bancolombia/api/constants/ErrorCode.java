package co.com.bancolombia.api.constants;

public enum ErrorCode {
    VALIDATION_FAILED("APP-001"),
    CONFLICT("APP-002"),
    BAD_REQUEST("APP-003"),
    NOT_FOUND("APP-004"),
    FORBIDDEN("APP-005"),
    INTERNAL_ERROR("APP-999");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}