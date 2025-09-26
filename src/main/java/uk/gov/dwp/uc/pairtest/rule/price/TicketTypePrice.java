package uk.gov.dwp.uc.pairtest.rule.price;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TicketTypePrice {
    ADULT(25),
    CHILD(15),
    INFANT(0);
    private final int ticketPrice;
}
