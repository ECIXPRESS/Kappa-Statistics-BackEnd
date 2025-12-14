package edu.dosw.Kappa_Stats_BackEnd.Exception;

import java.util.Map;

public class ApplicationException extends RuntimeException {

    private final String errorCode;
    private final Map<String, Object> details;
    private final ErrorType errorType;

    public enum ErrorType {
        VALIDATION,
        NOT_FOUND,
        BUSINESS_RULE,
        TECHNICAL,
        SECURITY
    }

    public ApplicationException(String message, String errorCode, ErrorType errorType) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.details = null;
    }

    public ApplicationException(String message, String errorCode, ErrorType errorType,
                                Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.details = details;
    }

    public ApplicationException(String message, String errorCode, ErrorType errorType,
                                Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.details = null;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public boolean hasDetails() {
        return details != null && !details.isEmpty();
    }

    public static ApplicationException validation(String message, String errorCode) {
        return new ApplicationException(message, errorCode, ErrorType.VALIDATION);
    }

    public static ApplicationException validation(String message, String errorCode,
                                                  Map<String, Object> details) {
        return new ApplicationException(message, errorCode, ErrorType.VALIDATION, details);
    }

    public static ApplicationException notFound(String message, String errorCode) {
        return new ApplicationException(message, errorCode, ErrorType.NOT_FOUND);
    }

    public static ApplicationException notFound(String message, String errorCode,
                                                Map<String, Object> details) {
        return new ApplicationException(message, errorCode, ErrorType.NOT_FOUND, details);
    }

    public static ApplicationException businessRule(String message, String errorCode) {
        return new ApplicationException(message, errorCode, ErrorType.BUSINESS_RULE);
    }

    public static ApplicationException technical(String message, String errorCode) {
        return new ApplicationException(message, errorCode, ErrorType.TECHNICAL);
    }

    public static ApplicationException technical(String message, String errorCode,
                                                 Throwable cause) {
        return new ApplicationException(message, errorCode, ErrorType.TECHNICAL, cause);
    }
}