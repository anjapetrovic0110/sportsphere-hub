import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { KorisnikServis } from '../services/korisnik.service';

@Component({
  selector: 'app-izvestaji',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './izvestaji.component.html',
  styleUrl: './izvestaji.component.css'
})
export class IzvestajiKomponenta {
  private servis = inject(KorisnikServis);
  mesec = new Date().toISOString().slice(0, 7);
  poruka = '';
  preuzmi(tip: 'popunjenost' | 'promet') {
    if (!this.mesec) {
      this.poruka = 'Izaberite mesec';
      return;
    }
    this.servis.izvestaj(tip, this.mesec).subscribe({
      next: b => {
        const u = URL.createObjectURL(b);
        const a = document.createElement('a');
        a.href = u;
        a.download = `izvestaj-${tip}-${this.mesec}.pdf`;
        a.click();
        URL.revokeObjectURL(u);
        this.poruka = 'PDF izveštaj je generisan';
      },
      error: g => (this.poruka = g.error?.poruka || 'Izveštaj nije generisan')
    });
  }
}
