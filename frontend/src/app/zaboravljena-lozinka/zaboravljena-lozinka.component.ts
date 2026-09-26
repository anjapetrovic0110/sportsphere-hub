import { Component, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LozinkaServis } from '../services/lozinka.service';

@Component({
  selector: 'app-zaboravljena-lozinka',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './zaboravljena-lozinka.component.html',
  styleUrl: './zaboravljena-lozinka.component.css'
})
export class ZaboravljenaLozinkaKomponenta {
  private servis = inject(LozinkaServis);
  identifikator = '';
  poruka = '';
  posalji(f: NgForm) {
    if (f.invalid) return;
    this.poruka = '';
    this.servis.zahtev(this.identifikator).subscribe({
      next: o => {
        this.poruka = o.poruka;
      },
      error: e => (this.poruka = e.error?.poruka || 'Zahtev nije obrađen')
    });
  }
}
