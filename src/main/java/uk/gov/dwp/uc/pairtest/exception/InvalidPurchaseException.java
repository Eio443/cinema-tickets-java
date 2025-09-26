package uk.gov.dwp.uc.pairtest.exception;

import lombok.Getter;
import uk.gov.dwp.uc.pairtest.rule.error.ErrorType;

@Getter
public class InvalidPurchaseException extends RuntimeException {
    private final ErrorType errorType;

    public InvalidPurchaseException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
}
