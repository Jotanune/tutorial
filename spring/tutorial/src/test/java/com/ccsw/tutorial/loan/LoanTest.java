package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.client.ClientService;
import com.ccsw.tutorial.client.model.Client;
import com.ccsw.tutorial.client.model.ClientDto;
import com.ccsw.tutorial.common.pagination.PageableRequest;
import com.ccsw.tutorial.game.GameService;
import com.ccsw.tutorial.game.model.Game;
import com.ccsw.tutorial.game.model.GameDto;
import com.ccsw.tutorial.loan.model.Loan;
import com.ccsw.tutorial.loan.model.LoanDto;
import com.ccsw.tutorial.loan.model.LoanSearchDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private GameService gameService;

    @Mock
    private ClientService clientService;

    @InjectMocks
    private LoanServiceImpl loanService;

    @Test
    public void findPageShouldReturnPagedLoans() {

        LoanSearchDto dto = new LoanSearchDto();
        dto.setPageable(new PageableRequest(0, 5));

        List<Loan> loans = new ArrayList<>();
        loans.add(mock(Loan.class));
        Page<Loan> page = new PageImpl<>(loans);

        when(loanRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        Page<Loan> result = loanService.findPage(dto);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    public void saveWithEndDateBeforeStartDateShouldThrowException() {

        LoanDto dto = new LoanDto();
        dto.setStartDate(LocalDate.of(2024, 1, 10));
        dto.setEndDate(LocalDate.of(2024, 1, 5));

        Exception exception = assertThrows(Exception.class, () -> {
            loanService.save(null, dto);
        });

        assertTrue(exception.getMessage().contains("La fecha de fin no puede ser anterior a la fecha de inicio"));
        verify(loanRepository, never()).save(any());
    }

    @Test
    public void saveWithMoreThan14DaysShouldThrowException() {

        LoanDto dto = new LoanDto();
        dto.setStartDate(LocalDate.of(2024, 1, 1));
        dto.setEndDate(LocalDate.of(2024, 1, 16));

        Exception exception = assertThrows(Exception.class, () -> {
            loanService.save(null, dto);
        });

        assertTrue(exception.getMessage().contains("El periodo de préstamo no puede ser superior a 14 días"));
        verify(loanRepository, never()).save(any());
    }

    @Test
    public void saveWithGameAlreadyLoanedShouldThrowException() {

        LoanDto dto = new LoanDto();
        dto.setStartDate(LocalDate.of(2024, 1, 1));
        dto.setEndDate(LocalDate.of(2024, 1, 10));

        GameDto gameDto = new GameDto();
        gameDto.setId(1L);
        dto.setGame(gameDto);

        ClientDto clientDto = new ClientDto();
        clientDto.setId(1L);
        dto.setClient(clientDto);

        List<Loan> existingLoans = new ArrayList<>();
        existingLoans.add(mock(Loan.class));

        when(loanRepository.findGameLoansInDateRange(any(), any(), any(), any())).thenReturn(existingLoans);

        Exception exception = assertThrows(Exception.class, () -> {
            loanService.save(null, dto);
        });

        assertTrue(exception.getMessage().contains("El juego ya está prestado en ese periodo de fechas"));
        verify(loanRepository, never()).save(any());
    }

    @Test
    public void saveWithClientHaving2LoansShouldThrowException() {

        LoanDto dto = new LoanDto();
        dto.setStartDate(LocalDate.of(2024, 1, 1));
        dto.setEndDate(LocalDate.of(2024, 1, 10));

        GameDto gameDto = new GameDto();
        gameDto.setId(1L);
        dto.setGame(gameDto);

        ClientDto clientDto = new ClientDto();
        clientDto.setId(1L);
        dto.setClient(clientDto);

        List<Loan> gameLoans = new ArrayList<>();
        List<Loan> clientLoans = new ArrayList<>();
        clientLoans.add(mock(Loan.class));
        clientLoans.add(mock(Loan.class));

        when(loanRepository.findGameLoansInDateRange(any(), any(), any(), any())).thenReturn(gameLoans);
        when(loanRepository.findClientLoansInDateRange(any(), any(), any(), any())).thenReturn(clientLoans);

        Exception exception = assertThrows(Exception.class, () -> {
            loanService.save(null, dto);
        });

        assertTrue(exception.getMessage().contains("El cliente ya tiene 2 juegos prestados en ese periodo de fechas"));
        verify(loanRepository, never()).save(any());
    }

    @Test
    public void saveWithClientHaving1LoanShouldSucceed() throws Exception {

        LoanDto dto = new LoanDto();
        dto.setStartDate(LocalDate.of(2024, 1, 1));
        dto.setEndDate(LocalDate.of(2024, 1, 10));

        GameDto gameDto = new GameDto();
        gameDto.setId(1L);
        dto.setGame(gameDto);

        ClientDto clientDto = new ClientDto();
        clientDto.setId(1L);
        dto.setClient(clientDto);

        Game game = mock(Game.class);
        Client client = mock(Client.class);

        List<Loan> gameLoans = new ArrayList<>();
        List<Loan> clientLoans = new ArrayList<>();
        clientLoans.add(mock(Loan.class)); // Solo 1 préstamo existente

        when(loanRepository.findGameLoansInDateRange(any(), any(), any(), any())).thenReturn(gameLoans);
        when(loanRepository.findClientLoansInDateRange(any(), any(), any(), any())).thenReturn(clientLoans);
        when(gameService.get(1L)).thenReturn(game);
        when(clientService.get(1L)).thenReturn(client);

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);

        // No debería lanzar excepción porque solo tiene 1 préstamo
        loanService.save(null, dto);

        verify(loanRepository).save(loanCaptor.capture());
    }

    @Test
    public void saveValidLoanShouldInsert() throws Exception {

        LoanDto dto = new LoanDto();
        dto.setStartDate(LocalDate.of(2024, 1, 1));
        dto.setEndDate(LocalDate.of(2024, 1, 10));

        GameDto gameDto = new GameDto();
        gameDto.setId(1L);
        dto.setGame(gameDto);

        ClientDto clientDto = new ClientDto();
        clientDto.setId(1L);
        dto.setClient(clientDto);

        Game game = mock(Game.class);
        Client client = mock(Client.class);

        when(loanRepository.findGameLoansInDateRange(any(), any(), any(), any())).thenReturn(new ArrayList<>());
        when(loanRepository.findClientLoansInDateRange(any(), any(), any(), any())).thenReturn(new ArrayList<>());
        when(gameService.get(1L)).thenReturn(game);
        when(clientService.get(1L)).thenReturn(client);

        ArgumentCaptor<Loan> loanCaptor = ArgumentCaptor.forClass(Loan.class);

        loanService.save(null, dto);

        verify(loanRepository).save(loanCaptor.capture());
        assertEquals(game, loanCaptor.getValue().getGame());
        assertEquals(client, loanCaptor.getValue().getClient());
    }

    @Test
    public void deleteExistingLoanShouldDelete() throws Exception {

        Long loanId = 1L;
        Loan loan = mock(Loan.class);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        loanService.delete(loanId);

        verify(loanRepository).deleteById(loanId);
    }

    @Test
    public void deleteNonExistingLoanShouldThrowException() {

        Long loanId = 999L;

        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            loanService.delete(loanId);
        });

        assertTrue(exception.getMessage().contains("No existe el préstamo"));
        verify(loanRepository, never()).deleteById(any());
    }
}
