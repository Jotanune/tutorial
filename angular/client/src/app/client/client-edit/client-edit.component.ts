import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { ClientService } from '../client.service';
import { Client } from '../model/Client';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-client-edit',
    standalone: true,
    imports: [FormsModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatDialogModule, CommonModule],
    templateUrl: './client-edit.component.html',
    styleUrl: './client-edit.component.scss'
})
export class ClientEditComponent implements OnInit {
    form: FormGroup;
    client: Client;
    clients: Client[] = [];

    constructor(
        private fb: FormBuilder,
        public dialogRef: MatDialogRef<ClientEditComponent>,
        @Inject(MAT_DIALOG_DATA) public data: { client: Client },
        private clientService: ClientService
    ) { }

    ngOnInit(): void {
        this.client = this.data.client ? Object.assign({}, this.data.client) : new Client();

        this.form = this.fb.group({
            id: [{ value: this.client.id, disabled: true }],
            name: [this.client.name, Validators.required]
        });

        this.clientService.getClients().subscribe(
            clients => this.clients = clients
        );
    }

    onSave() {
        if (this.form.invalid) {
            return;
        }

        const formValue = this.form.getRawValue();

        const existingClient = this.clients.find(c => 
            c.name.toLowerCase() === formValue.name.toLowerCase() && 
            c.id !== formValue.id
        );

        if (existingClient) {
            this.form.get('name').setErrors({ 'duplicate': true });
            return;
        }

        const clientToSave: Client = {
            id: this.client.id,
            ...formValue
        };

        this.clientService.saveClient(clientToSave).subscribe(() => {
            this.dialogRef.close();
        });
    }

    onClose() {
        this.dialogRef.close();
    }
}
