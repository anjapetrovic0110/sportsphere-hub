import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PrijavaServis } from '../services/prijava.service';

@Component({
  selector: 'app-korisnik-pocetna',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './korisnik-pocetna.component.html',
  styleUrl: './korisnik-pocetna.component.css'
})
export class KorisnikPocetnaKomponenta {
  private prijavaServis = inject(PrijavaServis);
  korisnik = this.prijavaServis.dohvatiKorisnika();
}
