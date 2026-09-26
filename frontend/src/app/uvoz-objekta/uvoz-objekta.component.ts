import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { KorisnikServis } from '../services/korisnik.service';

@Component({
  selector: 'app-uvoz-objekta',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './uvoz-objekta.component.html',
  styleUrl: './uvoz-objekta.component.css'
})
export class UvozObjektaKomponenta {
  private servis = inject(KorisnikServis);
  poruka = '';
  uspesno = false;
  slanje = false;
  uvezi(dogadjaj: Event) {
    const fajl = (dogadjaj.target as HTMLInputElement).files?.[0];
    if (!fajl) return;
    this.poruka = '';
    this.slanje = true;
    this.servis.uveziObjekat(fajl).subscribe({
      next: o => {
        this.poruka = o.poruka;
        this.uspesno = true;
        this.slanje = false;
      },
      error: g => {
        this.poruka = g.error?.poruka || 'Uvoz nije uspeo';
        this.uspesno = false;
        this.slanje = false;
      }
    });
  }
}
