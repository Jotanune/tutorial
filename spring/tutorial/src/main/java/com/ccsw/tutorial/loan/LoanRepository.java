package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.loan.model.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * @author ccsw
 *
 */
public interface LoanRepository extends CrudRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {

    @Override
    @EntityGraph(attributePaths = { "game", "game.category", "game.author", "client" })
    List<Loan> findAll();

    @Override
    @EntityGraph(attributePaths = { "game", "game.category", "game.author", "client" })
    Page<Loan> findAll(Specification<Loan> spec, Pageable pageable);

    /**
     * Encuentra préstamos de un juego que se solapen con un rango de fechas
     *
     * @param gameId ID del juego
     * @param startDate fecha de inicio del rango
     * @param endDate fecha de fin del rango
     * @param loanId ID del préstamo a excluir (para actualización)
     * @return lista de préstamos
     */
    @Query("SELECT l FROM Loan l WHERE l.game.id = :gameId " + "AND l.startDate <= :endDate AND l.endDate >= :startDate " + "AND (:loanId IS NULL OR l.id != :loanId)")
    List<Loan> findGameLoansInDateRange(@Param("gameId") Long gameId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("loanId") Long loanId);

    /**
     * Encuentra préstamos de un cliente que se solapen con un rango de fechas
     *
     * @param clientId ID del cliente
     * @param startDate fecha de inicio del rango
     * @param endDate fecha de fin del rango
     * @param loanId ID del préstamo a excluir (para actualización)
     * @return lista de préstamos
     */
    @Query("SELECT l FROM Loan l WHERE l.client.id = :clientId " + "AND l.startDate <= :endDate AND l.endDate >= :startDate " + "AND (:loanId IS NULL OR l.id != :loanId)")
    List<Loan> findClientLoansInDateRange(@Param("clientId") Long clientId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, @Param("loanId") Long loanId);
}
