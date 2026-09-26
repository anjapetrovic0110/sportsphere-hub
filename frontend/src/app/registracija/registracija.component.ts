import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Registracija } from '../models/registracija';
import { Sport } from '../models/sport';
import { RegistracijaServis } from '../services/registracija.service';

@Component({
  selector: 'app-registracija',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './registracija.component.html',
  styleUrl: './registracija.component.css'
})
export class RegistracijaKomponenta implements OnInit {
  private registracijaServis = inject(RegistracijaServis);
  podaci: Registracija = new Registracija();
  sportovi: Sport[] = [];
  slika: File | null = null;
  pregledSlike = '';
  poruka = '';
  uspesno = false;
  slanje = false;
  generisanje = false;
  ngOnInit(): void {
    this.registracijaServis.dohvatiSportove().subscribe({
      next: podaci => (this.sportovi = podaci),
      error: () => (this.poruka = 'Sportovi trenutno nisu dostupni')
    });
  }

  promenaSporta(id: number, izabran: boolean) {
    if (izabran) {
      if (this.podaci.idOmiljenihSportova.length < 5) this.podaci.idOmiljenihSportova.push(id);
    } else {
      this.podaci.idOmiljenihSportova = this.podaci.idOmiljenihSportova.filter(idSporta => idSporta != id);
    }
  }

  promenaSlike(dogadjaj: Event) {
    const input = dogadjaj.target as HTMLInputElement;
    this.slika = input.files?.[0] || null;
    if (this.pregledSlike) URL.revokeObjectURL(this.pregledSlike);
    this.pregledSlike = this.slika ? URL.createObjectURL(this.slika) : '';
    if (this.slika && !['image/png', 'image/jpeg'].includes(this.slika.type)) {
      this.poruka = 'Profilna slika mora biti PNG ili JPG';
      this.slika = null;
      this.pregledSlike = '';
    } else if (this.slika && this.slika.size > 5 * 1024 * 1024) {
      this.poruka = 'Profilna slika može imati najviše 5 MB';
      this.slika = null;
      this.pregledSlike = '';
    }
  }

  async generisiAvatar() {
    this.generisanje = true;
    this.poruka = '';
    try {
      const ime = this.podaci.ime.trim();
      const prezime = this.podaci.prezime.trim();
      const seme = `${ime} ${prezime}`.trim() || this.podaci.korisnickoIme.trim() || 'SportSphere';
      const [{ Avatar, Style }, { default: avataaars }] = await Promise.all([
        import('@dicebear/core'),
        import('@dicebear/styles/avataaars.json')
      ]);
      const stil = new Style(avataaars);
      const avatar = new Avatar(stil, {
        seed: seme,
        backgroundColor: ['#b6e3f4', '#c0aede', '#d1d4f9']
      });
      const svgSlika = new Image();
      await new Promise<void>((resolve, reject) => {
        svgSlika.onload = () => resolve();
        svgSlika.onerror = () => reject(new Error('DiceBear SVG nije učitan'));
        svgSlika.src = avatar.toDataUri();
      });
      const platno = document.createElement('canvas');
      platno.width = 512;
      platno.height = 512;
      const kontekst = platno.getContext('2d');
      if (!kontekst) throw new Error();
      kontekst.drawImage(svgSlika, 0, 0, platno.width, platno.height);
      const png = await new Promise<Blob>((resolve, reject) =>
        platno.toBlob(b => (b ? resolve(b) : reject(new Error('PNG nije napravljen'))), 'image/png')
      );
      this.slika = new File([png], 'dicebear-avatar.png', { type: 'image/png' });
      if (this.pregledSlike) URL.revokeObjectURL(this.pregledSlike);
      this.pregledSlike = URL.createObjectURL(this.slika);
    } catch {
      this.poruka = 'DiceBear avatar trenutno nije moguće generisati. Otpremite profilnu sliku.';
    } finally {
      this.generisanje = false;
    }
  }

  registracija(forma: NgForm) {
    this.poruka = '';
    this.uspesno = false;
    if (this.generisanje) {
      this.poruka = 'Sačekajte da se avatar generiše';
      return;
    }
    if (forma.invalid) {
      forma.control.markAllAsTouched();
      this.poruka = 'Proverite označena polja i ispravite podatke';
      return;
    }
    if (this.podaci.idOmiljenihSportova.length > 5) {
      this.poruka = 'Možete izabrati najviše pet sportova';
      return;
    }
    this.slanje = true;
    this.registracijaServis.registruj(this.podaci, this.slika).subscribe({
      next: odgovor => {
        this.poruka = odgovor.poruka;
        this.uspesno = true;
        this.slanje = false;
        this.podaci = new Registracija();
        this.slika = null;
        this.pregledSlike = '';
      },
      error: greska => {
        this.poruka = greska.error?.poruka || 'Registracija nije uspela';
        this.slanje = false;
      }
    });
  }
}
