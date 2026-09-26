import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Prijava } from '../models/prijava';
import { PrijavaServis } from '../services/prijava.service';

@Component({
  selector: 'app-prijava',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './prijava.component.html',
  styleUrl: './prijava.component.css'
})
export class PrijavaKomponenta {
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
        if (korisnik.uloga == 'ADMIN') {
          this.prijavaServis.odjava();
          this.poruka = 'Administratorska prijava nije dostupna na ovoj stranici';
          return;
        }
        this.prijavaServis.sacuvajKorisnika(korisnik);
        if (korisnik.uloga == 'EMPLOYEE') this.ruter.navigate(['/zaposleni']);
        else this.ruter.navigate(['/sportista']);
      },
      error: greska => {
        this.poruka = greska.error?.poruka || 'Prijava nije uspela';
        this.slanje = false;
      }
    });
  }
}
