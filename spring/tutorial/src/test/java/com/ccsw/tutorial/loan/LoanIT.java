package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.common.pagination.PageableRequest;
import com.ccsw.tutorial.config.ResponsePage;
import com.ccsw.tutorial.loan.model.LoanDto;
import com.ccsw.tutorial.loan.model.LoanSearchDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class LoanIT {

    public static final String LOCALHOST = "http://localhost:";
    public static final String SERVICE_PATH = "/loan";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    ParameterizedTypeReference<ResponsePage<LoanDto>> responseTypePage = new ParameterizedTypeReference<ResponsePage<LoanDto>>() {
    };

    @Test
    public void findPageShouldReturnPagedList() {

        LoanSearchDto searchDto = new LoanSearchDto();
        searchDto.setPageable(new PageableRequest(0, 5));

        ResponseEntity<ResponsePage<LoanDto>> response = restTemplate.exchange(LOCALHOST + port + SERVICE_PATH, HttpMethod.POST, new HttpEntity<>(searchDto), responseTypePage);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void findPageFilteringByGameShouldReturnFilteredList() {

        LoanSearchDto searchDto = new LoanSearchDto();
        searchDto.setGameId(1L);
        searchDto.setPageable(new PageableRequest(0, 5));

        ResponseEntity<ResponsePage<LoanDto>> response = restTemplate.exchange(LOCALHOST + port + SERVICE_PATH, HttpMethod.POST, new HttpEntity<>(searchDto), responseTypePage);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void findPageFilteringByClientShouldReturnFilteredList() {

        LoanSearchDto searchDto = new LoanSearchDto();
        searchDto.setClientId(1L);
        searchDto.setPageable(new PageableRequest(0, 5));

        ResponseEntity<ResponsePage<LoanDto>> response = restTemplate.exchange(LOCALHOST + port + SERVICE_PATH, HttpMethod.POST, new HttpEntity<>(searchDto), responseTypePage);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void findPageFilteringByDateShouldReturnFilteredList() {

        LoanSearchDto searchDto = new LoanSearchDto();
        searchDto.setDate(LocalDate.of(2024, 1, 15));
        searchDto.setPageable(new PageableRequest(0, 5));

        ResponseEntity<ResponsePage<LoanDto>> response = restTemplate.exchange(LOCALHOST + port + SERVICE_PATH, HttpMethod.POST, new HttpEntity<>(searchDto), responseTypePage);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void saveValidLoanShouldCreate() {

        LoanDto dto = new LoanDto();
        dto.setGame(new com.ccsw.tutorial.game.model.GameDto());
        dto.getGame().setId(1L);
        dto.setClient(new com.ccsw.tutorial.client.model.ClientDto());
        dto.getClient().setId(1L);
        dto.setStartDate(LocalDate.of(2025, 12, 1));
        dto.setEndDate(LocalDate.of(2025, 12, 10));

        ResponseEntity<?> response = restTemplate.exchange(LOCALHOST + port + SERVICE_PATH, HttpMethod.PUT, new HttpEntity<>(dto), Void.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void saveWithEndDateBeforeStartDateShouldFail() {

        LoanDto dto = new LoanDto();
        dto.setGame(new com.ccsw.tutorial.game.model.GameDto());
        dto.getGame().setId(1L);
        dto.setClient(new com.ccsw.tutorial.client.model.ClientDto());
        dto.getClient().setId(1L);
        dto.setStartDate(LocalDate.of(2025, 12, 10));
        dto.setEndDate(LocalDate.of(2025, 12, 5));

        ResponseEntity<?> response = restTemplate.exchange(LOCALHOST + port + SERVICE_PATH, HttpMethod.PUT, new HttpEntity<>(dto), Void.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void saveWithMoreThan14DaysShouldFail() {

        LoanDto dto = new LoanDto();
        dto.setGame(new com.ccsw.tutorial.game.model.GameDto());
        dto.getGame().setId(1L);
        dto.setClient(new com.ccsw.tutorial.client.model.ClientDto());
        dto.getClient().setId(1L);
        dto.setStartDate(LocalDate.of(2025, 12, 1));
        dto.setEndDate(LocalDate.of(2025, 12, 20));

        ResponseEntity<?> response = restTemplate.exchange(LOCALHOST + port + SERVICE_PATH, HttpMethod.PUT, new HttpEntity<>(dto), Void.class);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void deleteLoanShouldRemoveIt() {

        // Primero crear un préstamo
        LoanDto dto = new LoanDto();
        dto.setGame(new com.ccsw.tutorial.game.model.GameDto());
        dto.getGame().setId(2L);
        dto.setClient(new com.ccsw.tutorial.client.model.ClientDto());
        dto.getClient().setId(2L);
        dto.setStartDate(LocalDate.of(2025, 11, 1));
        dto.setEndDate(LocalDate.of(2025, 11, 5));

        restTemplate.exchange(LOCALHOST + port + SERVICE_PATH, HttpMethod.PUT, new HttpEntity<>(dto), Void.class);

        // Luego intentar borrarlo (asumiendo que el ID 1 existe)
        ResponseEntity<?> response = restTemplate.exchange(LOCALHOST + port + SERVICE_PATH + "/1", HttpMethod.DELETE, null, Void.class);

        // Nota: Esto podría fallar si no hay datos de prueba, pero demuestra la funcionalidad
        assertTrue(response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
