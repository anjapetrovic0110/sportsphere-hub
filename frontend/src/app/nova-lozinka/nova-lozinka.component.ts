import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LozinkaServis } from '../services/lozinka.service';

@Component({
  selector: 'app-nova-lozinka',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './nova-lozinka.component.html',
  styleUrl: '../zaboravljena-lozinka/zaboravljena-lozinka.component.css'
})
export class NovaLozinkaKomponenta implements OnInit {
  private ruta = inject(ActivatedRoute);
  private servis = inject(LozinkaServis);
  token = '';
  lozinka = '';
  potvrda = '';
  poruka = '';
  uspeh = false;
  ngOnInit() {
    this.token = this.ruta.snapshot.queryParamMap.get('token') || '';
  }
  sacuvaj(f: NgForm) {
    if (f.invalid || this.lozinka !== this.potvrda) {
      this.poruka =
        this.lozinka !== this.potvrda ? 'Lozinke se ne podudaraju' : 'Unesite ispravnu lozinku';
      return;
    }
    this.servis.promeni(this.token, this.lozinka).subscribe({
      next: o => {
        this.poruka = o.poruka;
        this.uspeh = true;
      },
      error: e => (this.poruka = e.error?.poruka || 'Lozinka nije promenjena')
    });
  }
}
