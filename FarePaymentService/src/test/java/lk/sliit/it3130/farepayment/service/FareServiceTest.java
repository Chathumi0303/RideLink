package lk.sliit.it3130.farepayment.service;

import lk.sliit.it3130.farepayment.repository.FareRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class FareServiceTest {

    @Test
    void calculatesFareUsingDocumentedRule() {
        FareService service = new FareService(mock(FareRepository.class));
        assertEquals(650.00, service.calculate(5.0, 10).doubleValue());
    }

    @Test
    void calculatesSmallRideCorrectly() {
        FareService service = new FareService(mock(FareRepository.class));
        assertEquals(230.00, service.calculate(1.0, 0).doubleValue());
    }
}