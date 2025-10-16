import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Loan } from './model/Loan';

@Injectable({
    providedIn: 'root'
})
export class LoanService {

    constructor(
        private http: HttpClient
    ) { }

    private baseUrl = 'http://localhost:8080/loan';

    getLoans(gameId?: number, clientId?: number, date?: string, pageable?: any): Observable<any> {
        const url = this.composeFindUrl(gameId, clientId, date);
        console.log('=== PETICIÓN AL BACKEND ===');
        console.log('URL:', url);
        console.log('Body (pageable):', pageable);
        return this.http.post<any>(url, pageable || {});
    }

    saveLoan(loan: Loan): Observable<Loan> {
        const { id } = loan;
        const url = id ? `${this.baseUrl}/${id}` : this.baseUrl;
        return this.http.put<Loan>(url, loan);
    }

    deleteLoan(idLoan: number): Observable<any> {
        return this.http.delete(`${this.baseUrl}/${idLoan}`);
    }

    private composeFindUrl(gameId?: number, clientId?: number, date?: string): string {
        const params = new URLSearchParams();
        if (gameId) {
            params.set('idGame', gameId.toString());
        }
        if (clientId) {
            params.set('idClient', clientId.toString());
        }
        if (date) {
            params.set('date', date);
        }
        const queryString = params.toString();
        return queryString ? `${this.baseUrl}?${queryString}` : this.baseUrl;
    }
}
