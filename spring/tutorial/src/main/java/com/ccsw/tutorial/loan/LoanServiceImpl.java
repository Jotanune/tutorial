package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.client.ClientService;
import com.ccsw.tutorial.common.criteria.SearchCriteria;
import com.ccsw.tutorial.game.GameService;
import com.ccsw.tutorial.loan.model.Loan;
import com.ccsw.tutorial.loan.model.LoanDto;
import com.ccsw.tutorial.loan.model.LoanSearchDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @author ccsw
 *
 */
@Service
@Transactional
public class LoanServiceImpl implements LoanService {

    @Autowired
    LoanRepository loanRepository;

    @Autowired
    GameService gameService;

    @Autowired
    ClientService clientService;

    /**
     * {@inheritDoc}
     */
    @Override
    public Loan get(Long id) {
        return this.loanRepository.findById(id).orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Loan> findPage(LoanSearchDto dto) {

        LoanSpecification gameSpec = new LoanSpecification(new SearchCriteria("game.id", ":", dto.getGameId()));
        LoanSpecification clientSpec = new LoanSpecification(new SearchCriteria("client.id", ":", dto.getClientId()));

        Specification<Loan> spec = Specification.allOf(gameSpec, clientSpec);

        // Si hay una fecha, filtrar por préstamos activos en esa fecha
        if (dto.getDate() != null) {
            LoanSpecification startDateSpec = new LoanSpecification(new SearchCriteria("startDate", "<=", dto.getDate()));
            LoanSpecification endDateSpec = new LoanSpecification(new SearchCriteria("endDate", ">=", dto.getDate()));
            spec = Specification.allOf(spec, startDateSpec, endDateSpec);
        }

        // Si no hay pageable, usar valores por defecto
        if (dto.getPageable() == null) {
            return this.loanRepository.findAll(spec, org.springframework.data.domain.PageRequest.of(0, 10));
        }

        return this.loanRepository.findAll(spec, dto.getPageable().getPageable());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(Long id, LoanDto dto) throws Exception {

        // Validación 1: La fecha de fin NO puede ser anterior a la fecha de inicio
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new Exception("La fecha de fin no puede ser anterior a la fecha de inicio");
        }

        // Validación 2: El periodo de préstamo máximo solo puede ser de 14 días
        long daysBetween = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate());
        if (daysBetween > 14) {
            throw new Exception("El periodo de préstamo no puede ser superior a 14 días");
        }

        // Validación 3: El mismo juego no puede estar prestado a dos clientes distintos en un mismo día
        List<Loan> gameLoans = loanRepository.findGameLoansInDateRange(dto.getGame().getId(), dto.getStartDate(), dto.getEndDate(), id);

        if (!gameLoans.isEmpty()) {
            throw new Exception("El juego ya está prestado en ese periodo de fechas");
        }

        // Validación 4: Un mismo cliente no puede tener prestados más de 2 juegos en un mismo día
        // La query ya excluye el préstamo actual (si estamos editando), por lo que si ya hay 2 o más
        // préstamos que se solapan con el nuevo rango, no se puede guardar
        List<Loan> clientLoans = loanRepository.findClientLoansInDateRange(dto.getClient().getId(), dto.getStartDate(), dto.getEndDate(), id);

        // Si ya hay 2 o más préstamos, no se puede añadir/actualizar
        if (clientLoans.size() >= 2) {
            throw new Exception("El cliente ya tiene 2 juegos prestados en ese periodo de fechas");
        }

        // Si todas las validaciones pasan, guardar el préstamo
        Loan loan;

        if (id == null) {
            loan = new Loan();
        } else {
            loan = this.loanRepository.findById(id).orElse(null);
            if (loan == null) {
                throw new Exception("No existe el préstamo");
            }
        }

        BeanUtils.copyProperties(dto, loan, "id", "game", "client");

        loan.setGame(gameService.get(dto.getGame().getId()));
        loan.setClient(clientService.get(dto.getClient().getId()));

        this.loanRepository.save(loan);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Long id) throws Exception {

        if (this.loanRepository.findById(id).orElse(null) == null) {
            throw new Exception("No existe el préstamo");
        }

        this.loanRepository.deleteById(id);
    }
}
