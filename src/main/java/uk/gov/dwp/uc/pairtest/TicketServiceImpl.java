package uk.gov.dwp.uc.pairtest;

import lombok.RequiredArgsConstructor;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.rule.error.ErrorType;
import uk.gov.dwp.uc.pairtest.rule.price.TicketTypePrice;

import java.util.Arrays;

import static uk.gov.dwp.uc.pairtest.utils.constant.TicketRequestConstant.MAX_TICKET_REQUEST;

@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        validate(accountId, ticketTypeRequests);
        int totalAmountToPay = calculateTotalAmountToPay(ticketTypeRequests);
        int totalSeatsToAllocate = calculateTotalSeatsToAllocate(ticketTypeRequests);
        try {
            ticketPaymentService.makePayment(accountId, totalAmountToPay);
            seatReservationService.reserveSeat(accountId, totalSeatsToAllocate);
        } catch (Exception e) {
            throw new InvalidPurchaseException(ErrorType.THIRD_PARTY_ERROR, e.getMessage());
        }
    }

    private int calculateTotalAmountToPay(TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        return Arrays.stream(ticketTypeRequests)
                .mapToInt(t -> TicketTypePrice.valueOf(t.getTicketType().name()).getTicketPrice() * t.getNoOfTickets())
                .sum();
    }

    private int calculateTotalSeatsToAllocate(TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        return Arrays.stream(ticketTypeRequests).filter(t -> t.getTicketType() != TicketTypeRequest.Type.INFANT).mapToInt(TicketTypeRequest::getNoOfTickets).sum();
    }

    private void validate(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        if (!isAccountIdValid(accountId)) {
            throw new InvalidPurchaseException(ErrorType.ACCOUNT_ID_ERROR, String.format("Invalid account Id %s", accountId));
        }

        if (ticketTypeRequests != null) {
            if (!hasAdultTicket(ticketTypeRequests)) {
                throw new InvalidPurchaseException(ErrorType.NO_ADULT_ERROR, "Ticket must contain at least an adult ticket");
            }

            if (isTicketAboveMax(ticketTypeRequests)) {
                throw new InvalidPurchaseException(ErrorType.ABOVE_MAX_ERROR, String.format("Only a maximum of %d tickets can be purchased at a time", MAX_TICKET_REQUEST));
            }

            if (isInfantAdultMismatch(ticketTypeRequests)) {
                throw new InvalidPurchaseException(ErrorType.INFANT_ADULT_ERROR, "Infant sits on Adult laps - infant tickets cannot be more than Adult tickets");
            }
        }
        else {
            throw new InvalidPurchaseException(ErrorType.TICKET_REQUEST_ERROR, "The minimum ticket quantity cannot be less than 1 or null");
        }
    }

    private boolean isAccountIdValid (Long accountId) {
        return accountId != null && accountId >= 1;
    }

    private boolean isTicketAboveMax (TicketTypeRequest... ticketTypeRequests) {
        int totalNoOfTickets =  Arrays.stream(ticketTypeRequests).mapToInt(TicketTypeRequest::getNoOfTickets).sum();
        return totalNoOfTickets > MAX_TICKET_REQUEST;
    }

    private boolean hasAdultTicket ( TicketTypeRequest... ticketTypeRequests) {
        return  Arrays.stream(ticketTypeRequests)
                .anyMatch(t -> t.getTicketType().equals(TicketTypeRequest.Type.ADULT) && t.getNoOfTickets() > 0);
    }

    private boolean isInfantAdultMismatch ( TicketTypeRequest... ticketTypeRequests) {
        int numberOfInfantTicket =  Arrays.stream(ticketTypeRequests).filter(t -> t.getTicketType() == TicketTypeRequest.Type.INFANT).mapToInt(TicketTypeRequest::getNoOfTickets).sum();

        if (numberOfInfantTicket > 0) {
            int numberOfAdultTicketType =  Arrays.stream(ticketTypeRequests).filter(t -> t.getTicketType() == TicketTypeRequest.Type.ADULT).mapToInt(TicketTypeRequest::getNoOfTickets).sum();
            return numberOfInfantTicket > numberOfAdultTicketType;
        }
        return false;
    }
}
