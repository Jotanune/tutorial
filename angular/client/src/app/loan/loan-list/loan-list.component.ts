import { Component, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Loan } from '../model/Loan';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { FormsModule } from '@angular/forms';
import { LoanService } from '../loan.service';
import { GameService } from '../../game/game.service';
import { ClientService } from '../../client/client.service';
import { MatDialog } from '@angular/material/dialog';
import { Game } from '../../game/model/Game';
import { Client } from '../../client/model/Client';
import { LoanEditComponent } from '../loan-edit/loan-edit.component';
import { DialogConfirmationComponent } from '../../core/dialog-confirmation/dialog-confirmation.component';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

@Component({
    selector: 'app-loan-list',
    standalone: true,
    imports: [
        MatButtonModule,
        MatIconModule,
        MatTableModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatPaginatorModule,
        CommonModule,
        FormsModule
    ],
    templateUrl: './loan-list.component.html',
    styleUrl: './loan-list.component.scss'
})
export class LoanListComponent implements OnInit {
    dataSource = new MatTableDataSource<Loan>();
    displayedColumns: string[] = ['id', 'game', 'client', 'startDate', 'endDate', 'action'];
    
    games: Game[] = [];
    clients: Client[] = [];
    
    filterGame: Game;
    filterClient: Client;
    filterDate: Date;
    
    pageNumber: number = 0;
    pageSize: number = 5;
    totalElements: number = 0;
    pageSizeOptions: number[] = [5, 10, 20];

    constructor(
        private loanService: LoanService,
        private gameService: GameService,
        private clientService: ClientService,
        public dialog: MatDialog,
    ) { }

    ngOnInit(): void {
        this.loadLoans();
        
        this.gameService.getGames().subscribe(
            games => this.games = games
        );
        
        this.clientService.getClients().subscribe(
            clients => this.clients = clients
        );
    }

    loadLoans(): void {
        const gameId = this.filterGame?.id;
        const clientId = this.filterClient?.id;
        const date = this.filterDate ? this.formatDate(this.filterDate) : null;
        
        console.log('=== FILTROS APLICADOS ===');
        console.log('filterGame:', this.filterGame);
        console.log('gameId:', gameId);
        console.log('filterClient:', this.filterClient);
        console.log('clientId:', clientId);
        console.log('filterDate:', this.filterDate);
        console.log('date formateada:', date);
        
        const pageable = {
            pageNumber: this.pageNumber,
            pageSize: this.pageSize
        };
        
        console.log('pageable:', pageable);
        
        this.loanService.getLoans(gameId, clientId, date, pageable).subscribe(
            response => {
                console.log('Respuesta del servidor:', response);
                this.dataSource.data = response.content;
                this.totalElements = response.totalElements;
            },
            error => {
                console.error('Error al cargar préstamos:', error);
            }
        );
    }

    onCleanFilter(): void {
        this.filterGame = null;
        this.filterClient = null;
        this.filterDate = null;
        this.onSearch();
    }

    onSearch(): void {
        this.pageNumber = 0;
        this.loadLoans();
    }

    createLoan() {
        const dialogRef = this.dialog.open(LoanEditComponent, {
            data: {}
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.loadLoans();
            }
        });
    }

    deleteLoan(loan: Loan) {
        const dialogRef = this.dialog.open(DialogConfirmationComponent, {
            data: { 
                title: "Eliminar préstamo", 
                description: "Atención si borra el préstamo se perderán sus datos.<br> ¿Desea eliminar el préstamo?" 
            }
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.loanService.deleteLoan(loan.id).subscribe(result => {
                    this.loadLoans();
                });
            }
        });
    }

    onPageChange(event: PageEvent): void {
        this.pageNumber = event.pageIndex;
        this.pageSize = event.pageSize;
        this.loadLoans();
    }

    formatDate(date: Date): string {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }
}
