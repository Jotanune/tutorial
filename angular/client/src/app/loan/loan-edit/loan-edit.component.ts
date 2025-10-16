import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { CommonModule } from '@angular/common';
import { LoanService } from '../loan.service';
import { Loan } from '../model/Loan';
import { Game } from '../../game/model/Game';
import { Client } from '../../client/model/Client';
import { GameService } from '../../game/game.service';
import { ClientService } from '../../client/client.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

@Component({
    selector: 'app-loan-edit',
    standalone: true,
    imports: [
        FormsModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatSelectModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatSnackBarModule,
        CommonModule
    ],
    templateUrl: './loan-edit.component.html',
    styleUrl: './loan-edit.component.scss',
})
export class LoanEditComponent implements OnInit {
    loan: Loan;
    games: Game[] = [];
    clients: Client[] = [];

    constructor(
        public dialogRef: MatDialogRef<LoanEditComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any,
        private loanService: LoanService,
        private gameService: GameService,
        private clientService: ClientService,
        private snackBar: MatSnackBar
    ) { }

    ngOnInit(): void {
        this.loan = this.data.loan ? Object.assign({}, this.data.loan) : new Loan();

        this.gameService.getGames().subscribe((games) => {
            this.games = games;

            if (this.loan.game != null) {
                const gameFilter: Game[] = games.filter(
                    (game) => game.id == this.data.loan.game.id
                );
                if (gameFilter != null) {
                    this.loan.game = gameFilter[0];
                }
            }
        });

        this.clientService.getClients().subscribe((clients) => {
            this.clients = clients;

            if (this.loan.client != null) {
                const clientFilter: Client[] = clients.filter(
                    (client) => client.id == this.data.loan.client.id
                );
                if (clientFilter != null) {
                    this.loan.client = clientFilter[0];
                }
            }
        });
    }

    async onSave() {
        // Validar que todos los campos estén completos
        if (!this.loan.client || !this.loan.game || !this.loan.startDate || !this.loan.endDate) {
            this.showError('Todos los campos son obligatorios');
            return;
        }

        const startDate = new Date(this.loan.startDate);
        const endDate = new Date(this.loan.endDate);

        // Validación 1: La fecha de fin NO puede ser anterior a la fecha de inicio
        if (endDate < startDate) {
            this.showError('La fecha de fin no puede ser anterior a la fecha de inicio');
            return;
        }

        // Validación 2: El periodo de préstamo máximo solo podrá ser de 14 días
        const diffTime = Math.abs(endDate.getTime() - startDate.getTime());
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        
        if (diffDays > 14) {
            this.showError('El periodo de préstamo máximo es de 14 días');
            return;
        }

        // Validación 3 y 4: Verificar disponibilidad con el backend
        this.loanService.saveLoan(this.loan).subscribe({
            next: (result) => {
                this.dialogRef.close(true);
            },
            error: (error) => {
                console.error('Error completo del servidor:', error);
                
                // Intentar extraer el mensaje de error del backend
                let errorMessage = 'Error al guardar el préstamo';
                
                // Primero intentar leer error.error que puede ser string o objeto
                if (error.error) {
                    console.log('error.error:', error.error);
                    console.log('tipo de error.error:', typeof error.error);
                    
                    // Si error.error es un string directo
                    if (typeof error.error === 'string') {
                        // Intentar parsear como JSON por si viene como string JSON
                        try {
                            const parsed = JSON.parse(error.error);
                            errorMessage = parsed.message || parsed.error || error.error;
                        } catch {
                            // No es JSON, usar el string directamente
                            errorMessage = error.error;
                        }
                    }
                    // Si error.error es un objeto
                    else if (typeof error.error === 'object') {
                        errorMessage = error.error.message || error.error.error || error.error.description || errorMessage;
                    }
                }
                // Si no hay error.error, intentar con error.message
                else if (error.message) {
                    errorMessage = error.message;
                }
                // Si no hay nada, usar el statusText
                else if (error.statusText && error.statusText !== 'Unknown Error') {
                    errorMessage = error.statusText;
                }
                
                console.log('Mensaje final a mostrar:', errorMessage);
                this.showError(errorMessage);
            }
        });
    }

    onClose() {
        this.dialogRef.close();
    }

    private showError(message: string): void {
        this.snackBar.open(message, 'Cerrar', {
            duration: 5000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['error-snackbar']
        });
    }
}
