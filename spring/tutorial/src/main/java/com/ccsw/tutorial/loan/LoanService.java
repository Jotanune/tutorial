package com.ccsw.tutorial.loan;

import com.ccsw.tutorial.loan.model.Loan;
import com.ccsw.tutorial.loan.model.LoanDto;
import com.ccsw.tutorial.loan.model.LoanSearchDto;
import org.springframework.data.domain.Page;

/**
 * @author ccsw
 *
 */
public interface LoanService {

    /**
     * Recupera un préstamo {@link Loan} a través de su ID
     *
     * @param id PK de la entidad
     * @return {@link Loan}
     */
    Loan get(Long id);

    /**
     * Método para recuperar un listado paginado de {@link Loan}
     *
     * @param dto dto de búsqueda
     * @return {@link Page} de {@link Loan}
     */
    Page<Loan> findPage(LoanSearchDto dto);

    /**
     * Método para crear o actualizar un {@link Loan}
     *
     * @param id PK de la entidad
     * @param dto datos de la entidad
     * @throws Exception si no se cumplen las reglas de validación
     */
    void save(Long id, LoanDto dto) throws Exception;

    /**
     * Método para borrar un {@link Loan}
     *
     * @param id PK de la entidad
     * @throws Exception si hay algún problema al borrar
     */
    void delete(Long id) throws Exception;
}
