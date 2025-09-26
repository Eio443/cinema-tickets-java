package uk.gov.dwp.uc.pairtest.rule.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorType {
    THIRD_PARTY_ERROR("third party dependencies error"),
    ACCOUNT_ID_ERROR("invalid account id"),
    NO_ADULT_ERROR("no adult ticket found"),
    ABOVE_MAX_ERROR("ticket quantity above approved maximum per purchase"),
    TICKET_REQUEST_ERROR("ticket quantity cannot be less than 1 or null"),
    INFANT_ADULT_ERROR("an adult cannot lap many infants");
    private  final String message;
}
