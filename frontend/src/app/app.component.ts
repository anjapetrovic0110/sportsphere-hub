import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { PrijavaServis } from './services/prijava.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppKomponenta {
  title = 'SportSphere Hub';
  private prijavaServis = inject(PrijavaServis);
  private ruter = inject(Router);
  get korisnik() {
    return this.prijavaServis.dohvatiKorisnika();
  }

  pocetnaKorisnika() {
    if (this.korisnik?.uloga == 'ADMIN') return '/admin';
    if (this.korisnik?.uloga == 'EMPLOYEE') return '/zaposleni';
    return '/sportista';
  }

  odjava() {
    this.prijavaServis.odjava();
    this.ruter.navigate(['/prijava']);
  }
}
