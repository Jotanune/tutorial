package com.ccsw.tutorial.loan.model;

import com.ccsw.tutorial.common.pagination.PageableRequest;

import java.time.LocalDate;

/**
 * @author ccsw
 *
 */
public class LoanSearchDto {

    private Long gameId;

    private Long clientId;

    private LocalDate date;

    private PageableRequest pageable;

    /**
     * @return gameId
     */
    public Long getGameId() {
        return this.gameId;
    }

    /**
     * @param gameId new value of {@link #getGameId}.
     */
    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    /**
     * @return clientId
     */
    public Long getClientId() {
        return this.clientId;
    }

    /**
     * @param clientId new value of {@link #getClientId}.
     */
    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    /**
     * @return date
     */
    public LocalDate getDate() {
        return this.date;
    }

    /**
     * @param date new value of {@link #getDate}.
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * @return pageable
     */
    public PageableRequest getPageable() {
        return this.pageable;
    }

    /**
     * @param pageable new value of {@link #getPageable}.
     */
    public void setPageable(PageableRequest pageable) {
        this.pageable = pageable;
    }
}
