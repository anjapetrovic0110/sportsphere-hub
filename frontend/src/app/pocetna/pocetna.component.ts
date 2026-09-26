import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PocetnaPodaci } from '../models/pocetna-podaci';
import { JavniServis } from '../services/javni.service';

@Component({
  selector: 'app-pocetna',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './pocetna.component.html',
  styleUrl: './pocetna.component.css'
})
export class PocetnaKomponenta implements OnInit {
  private javniServis = inject(JavniServis);
  podaci = new PocetnaPodaci();
  ucitavanje = true;
  poruka = '';
  ngOnInit(): void {
    this.javniServis.dohvatiPocetnu().subscribe({
      next: podaci => {
        this.podaci = podaci;
        this.ucitavanje = false;
      },
      error: () => {
        this.poruka = 'Podaci trenutno nisu dostupni';
        this.ucitavanje = false;
      }
    });
  }
}
