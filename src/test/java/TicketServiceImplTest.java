import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.rule.error.ErrorType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;


@ExtendWith(MockitoExtension.class)
public class TicketServiceImplTest {
    @Mock
    private TicketPaymentService ticketPaymentService;

    @Mock
    private SeatReservationService seatReservationService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    @Tag("accountIdValidation")
    void givenAccountIdZero_whenPurchaseTickets_thenThrowException() {
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(0L, ticketTypeRequests));
        assertEquals(ErrorType.ACCOUNT_ID_ERROR.name(), invalidPurchaseException.getErrorType().name());
        assertEquals(ErrorType.ACCOUNT_ID_ERROR.getMessage(), invalidPurchaseException.getErrorType().getMessage());
    }

    @Test
    @Tag("accountIdValidation")
    void givenAccountIdNegative_whenPurchaseTickets_thenThrowException() {
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(-5L, ticketTypeRequests));
        assertEquals(ErrorType.ACCOUNT_ID_ERROR.name(), invalidPurchaseException.getErrorType().name());
        assertEquals(ErrorType.ACCOUNT_ID_ERROR.getMessage(), invalidPurchaseException.getErrorType().getMessage());
    }

    @Test
    @Tag("accountIdValidation")
    void givenAccountIdNull_whenPurchaseTickets_thenThrowException() {
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(null, ticketTypeRequests));
        assertEquals(ErrorType.ACCOUNT_ID_ERROR.name(), invalidPurchaseException.getErrorType().name());
        assertEquals(ErrorType.ACCOUNT_ID_ERROR.getMessage(), invalidPurchaseException.getErrorType().getMessage());
    }

    @Test
    @Tag("adultTicketValidation")
    void givenNoAdultTicket_whenPurchaseTickets_thenThrowException() {
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2)
        };
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(5L, ticketTypeRequests));
        assertEquals(ErrorType.NO_ADULT_ERROR.name(), invalidPurchaseException.getErrorType().name());
        assertEquals(ErrorType.NO_ADULT_ERROR.getMessage(), invalidPurchaseException.getErrorType().getMessage());
    }

    @Test
    @Tag("maximumTicketsValidation")
    void givenTotalTicketsAboveMax_whenPurchaseTickets_thenThrowException() {
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 15),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(5L, ticketTypeRequests));
        assertEquals(ErrorType.ABOVE_MAX_ERROR.name(), invalidPurchaseException.getErrorType().name());
        assertEquals(ErrorType.ABOVE_MAX_ERROR.getMessage(), invalidPurchaseException.getErrorType().getMessage());
    }

    @Test
    @Tag("maximumTicketsValidation")
    void givenOnlyAdultTicketsAboveMax_whenPurchaseTickets_thenThrowException() {
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26)
        };
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(5L, ticketTypeRequests));
        assertEquals(ErrorType.ABOVE_MAX_ERROR.name(), invalidPurchaseException.getErrorType().name());
        assertEquals(ErrorType.ABOVE_MAX_ERROR.getMessage(), invalidPurchaseException.getErrorType().getMessage());
    }

    @Test
    @Tag("maximumTicketsValidation")
    void givenOnlyAdultTicketsAtMax_whenPurchaseTickets_thenMakePaymentAndReserveSeat() {
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 25)
        };
        ticketService.purchaseTickets(5L, ticketTypeRequests);
        Mockito.verify(ticketPaymentService).makePayment(5L, 625);
        Mockito.verify(seatReservationService).reserveSeat(5L, 25);
    }

    @Test
    @Tag("infantAdultMismatchValidation")
    void givenInfantTicketsMoreThanAdult_whenPurchaseTickets_thenThrowException() {
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 15)
        };
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(5L, ticketTypeRequests));
        assertEquals(ErrorType.INFANT_ADULT_ERROR.name(), invalidPurchaseException.getErrorType().name());
        assertEquals(ErrorType.INFANT_ADULT_ERROR.getMessage(), invalidPurchaseException.getErrorType().getMessage());
    }

    @Test
    @Tag("ticketTypeRequestValidation")
    void givenEmptyTicketTypeRequest_whenPurchaseTickets_thenThrowException() {
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {

        };
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(5L, ticketTypeRequests));
        assertEquals(ErrorType.NO_ADULT_ERROR.name(), invalidPurchaseException.getErrorType().name());
        assertEquals(ErrorType.NO_ADULT_ERROR.getMessage(), invalidPurchaseException.getErrorType().getMessage());
    }

    @Test
    @Tag("ticketTypeRequestValidation")
    void givenTicketTypeRequestNull_whenPurchaseTickets_thenThrowException() {
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(5L, null));
        assertEquals(ErrorType.TICKET_REQUEST_ERROR.name(), invalidPurchaseException.getErrorType().name());
        assertEquals(ErrorType.TICKET_REQUEST_ERROR.getMessage(), invalidPurchaseException.getErrorType().getMessage());
    }

    @Test
    @Tag("makePaymentAndReserveSeatValidation")
    void givenValidTicket_whenPurchaseTickets_thenMakePaymentAndReserveSeat() {
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2)};

        ticketService.purchaseTickets(5L, ticketTypeRequests);
        Mockito.verify(ticketPaymentService).makePayment(5L, 50);
        Mockito.verify(seatReservationService).reserveSeat(5L, 2);
    }

    @Test
    @Tag("makePaymentAndReserveSeatValidation")
    void givenValidTicketTypes_whenPurchaseTickets_thenMakePaymentAndReserveSeat() {
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 3)
        };

        ticketService.purchaseTickets(5L, ticketTypeRequests);
        Mockito.verify(ticketPaymentService).makePayment(5L, 325);
        Mockito.verify(seatReservationService).reserveSeat(5L, 15);
    }

    //error calling third party exception capture
    @Test
    @Tag("thirdPartyExceptionTest")
    void givenThirdPartyException_whenPurchaseTickets_thenThrowException() {
        TicketTypeRequest[] ticketTypeRequests = new TicketTypeRequest[] {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };

        doThrow(new RuntimeException("Connection error")).when(ticketPaymentService)
                .makePayment(5L, 80);
        InvalidPurchaseException invalidPurchaseException = assertThrows(InvalidPurchaseException.class,
                () -> ticketService.purchaseTickets(5L, ticketTypeRequests));
        assertEquals(ErrorType.THIRD_PARTY_ERROR.name(), invalidPurchaseException.getErrorType().name());
        assertEquals(ErrorType.THIRD_PARTY_ERROR.getMessage(), invalidPurchaseException.getErrorType().getMessage());
    }

}
