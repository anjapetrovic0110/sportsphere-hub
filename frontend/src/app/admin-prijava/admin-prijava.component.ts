import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Prijava } from '../models/prijava';
import { PrijavaServis } from '../services/prijava.service';

@Component({
  selector: 'app-admin-prijava',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './admin-prijava.component.html',
  styleUrl: '../prijava/prijava.component.css'
})
export class AdministratorPrijavaKomponenta {
  private prijavaServis = inject(PrijavaServis);
  private ruter = inject(Router);
  podaci = new Prijava();
  poruka = '';
  slanje = false;

  prijava() {
    this.poruka = '';
    this.slanje = true;
    this.prijavaServis.prijava(this.podaci).subscribe({
      next: korisnik => {
        this.slanje = false;
        if (korisnik.uloga != 'ADMIN') {
          this.prijavaServis.odjava();
          this.poruka = 'Ova stranica je namenjena samo administratoru';
          return;
        }
        this.prijavaServis.sacuvajKorisnika(korisnik);
        this.ruter.navigate(['/admin']);
      },
      error: greska => {
        this.poruka = greska.error?.poruka || 'Prijava nije uspela';
        this.slanje = false;
      }
    });
  }
}
