import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ObjekatDetalji, ResursPregled } from '../models/objekat';
import { NovaRezervacija, TerminPregled } from '../models/profil';
import { Sport } from '../models/sport';
import { JavniServis } from '../services/javni.service';
import { KorisnikServis } from '../services/korisnik.service';
import { PrijavaServis } from '../services/prijava.service';
import { RegistracijaServis } from '../services/registracija.service';

@Component({
  selector: 'app-objekat-detalji',
  standalone: true,
  imports: [RouterLink, DecimalPipe, DatePipe, FormsModule],
  templateUrl: './objekat-detalji.component.html',
  styleUrl: './objekat-detalji.component.css'
})
export class ObjekatDetaljiKomponenta implements OnInit {
  private ruta = inject(ActivatedRoute);
  private javniServis = inject(JavniServis);
  private prijavaServis = inject(PrijavaServis);
  private korisnikServis = inject(KorisnikServis);
  private registracijaServis = inject(RegistracijaServis);
  private sanitizer = inject(DomSanitizer);
  objekat: ObjekatDetalji | null = null;
  poruka = '';
  korisnik = this.prijavaServis.dohvatiKorisnika();
  zahtev = new NovaRezervacija();
  termini: TerminPregled[] = [];
  sportovi: Sport[] = [];
  sati = Array.from({ length: 17 }, (_, i) => i + 7);
  dani = Array.from({ length: 7 }, (_, i) => {
    const datum = new Date();
    datum.setHours(0, 0, 0, 0);
    datum.setDate(datum.getDate() + i);
    return datum;
  });
  rezervacijaPoruka = '';
  uspesno = false;
  mapaUrl: SafeResourceUrl | null = null;
  preostaloReakcija = 0;
  reakcija: 'LIKE' | 'DISLIKE' = 'LIKE';
  komentar = '';
  reakcijaPoruka = '';
  ngOnInit(): void {
    const id = Number(this.ruta.snapshot.paramMap.get('id'));
    this.javniServis.dohvatiObjekat(id).subscribe({
      next: podaci => {
        this.objekat = podaci;
        if (podaci.geografskaSirina != null && podaci.geografskaDuzina != null) {
          const raspon = 0.008;
          const bbox = `${podaci.geografskaDuzina - raspon},${podaci.geografskaSirina - raspon},${
            podaci.geografskaDuzina + raspon
          },${podaci.geografskaSirina + raspon}`;
          this.mapaUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
            `https://www.openstreetmap.org/export/embed.html?bbox=${
              bbox
            }&layer=mapnik&marker=${podaci.geografskaSirina},${podaci.geografskaDuzina}`
          );
        }
        const od = Number(podaci.radnoVremeOd.slice(0, 2));
        const doSat = Number(podaci.radnoVremeDo.slice(0, 2));
        this.sati = Array.from({ length: Math.max(0, doSat - od) }, (_, i) => od + i);
      },
      error: greska => (this.poruka = greska.error?.poruka || 'Objekat nije pronađen')
    });
    if (this.korisnik?.uloga === 'ATHLETE')
      this.registracijaServis.dohvatiSportove().subscribe({ next: p => (this.sportovi = p) });
    if (this.korisnik?.uloga === 'ATHLETE') this.ucitajReakcije(id);
  }

  ucitajReakcije(id: number) {
    this.korisnikServis.dohvatiReakcije(id).subscribe({
      next: r => {
        this.preostaloReakcija = r.preostalo;
        if (this.objekat) {
          this.objekat.brojLajkova = r.brojLajkova;
          this.objekat.brojDislajkova = r.brojDislajkova;
          this.objekat.komentari = r.komentari;
        }
      },
      error: () => (this.reakcijaPoruka = 'Reakcije trenutno nisu dostupne')
    });
  }
  dodajReakciju() {
    if (!this.objekat) return;
    this.reakcijaPoruka = '';
    this.korisnikServis.dodajReakciju(this.objekat.id, this.reakcija, this.komentar).subscribe({
      next: r => {
        this.preostaloReakcija = r.preostalo;
        this.objekat!.brojLajkova = r.brojLajkova;
        this.objekat!.brojDislajkova = r.brojDislajkova;
        this.objekat!.komentari = r.komentari;
        this.komentar = '';
        this.reakcijaPoruka = 'Reakcija je sačuvana';
      },
      error: e => (this.reakcijaPoruka = e.error?.poruka || 'Reakcija nije sačuvana')
    });
  }

  nazivTipa(tip: string) {
    return (
      (
        {
          OUTDOOR_FIELD: 'Otvoreni teren',
          INDOOR_FIELD: 'Zatvoreni teren',
          SPORTS_HALL: 'Sportska hala'
        } as Record<string, string>
      )[tip] || tip
    );
  }

  izaberiResurs(id: number) {
    this.zahtev.idResursa = id;
    this.rezervacijaPoruka = '';
    this.korisnikServis.dohvatiRaspored(id).subscribe({
      next: podaci => (this.termini = podaci),
      error: () => (this.rezervacijaPoruka = 'Raspored trenutno nije dostupan')
    });
  }

  izabraniResurs(): ResursPregled | undefined {
    return this.objekat?.resursi.find(resurs => resurs.id === this.zahtev.idResursa);
  }

  satiDo() {
    if (!this.sati.length) return [];
    return [...this.sati.slice(1), this.sati[this.sati.length - 1] + 1];
  }

  rotirajResurs(pomeraj: number) {
    if (!this.objekat?.resursi.length) return;
    const trenutni = this.objekat.resursi.findIndex(r => r.id === this.zahtev.idResursa);
    const sledeci =
      (trenutni + pomeraj + this.objekat.resursi.length) % this.objekat.resursi.length;
    this.izaberiResurs(this.objekat.resursi[sledeci].id);
  }

  zauzet(dan: Date, sat: number) {
    const pocetak = this.vreme(dan, sat);
    const kraj = new Date(pocetak.getTime() + 60 * 60 * 1000);
    return this.termini.some(
      termin =>
        this.lokalnoVreme(termin.pocetak) < kraj && this.lokalnoVreme(termin.kraj) > pocetak
    );
  }

  prosao(dan: Date, sat: number) {
    return this.vreme(dan, sat) <= new Date();
  }

  izabran(dan: Date, sat: number) {
    return this.zahtev.datum === this.lokalniDatum(dan) && this.zahtev.satOd === sat;
  }

  izaberiTermin(dan: Date, sat: number) {
    if (this.zauzet(dan, sat) || this.prosao(dan, sat)) return;
    this.zahtev.datum = this.lokalniDatum(dan);
    this.zahtev.satOd = sat;
    this.zahtev.satDo = sat + 1;
    this.rezervacijaPoruka = '';
  }

  rezervisi(forma: NgForm) {
    this.rezervacijaPoruka = '';
    this.uspesno = false;
    if (forma.invalid || this.zahtev.idResursa == null || this.zahtev.idSporta == null) {
      forma.control.markAllAsTouched();
      this.rezervacijaPoruka = 'Popunite sve podatke za rezervaciju';
      return;
    }
    this.korisnikServis.rezervisi(this.zahtev).subscribe({
      next: odgovor => {
        this.rezervacijaPoruka = odgovor.poruka;
        this.uspesno = true;
        this.izaberiResurs(this.zahtev.idResursa!);
      },
      error: greska => (this.rezervacijaPoruka = greska.error?.poruka || 'Rezervacija nije uspela')
    });
  }

  private vreme(dan: Date, sat: number) {
    const vreme = new Date(dan);
    vreme.setHours(sat, 0, 0, 0);
    return vreme;
  }

  private lokalniDatum(datum: Date) {
    const godina = datum.getFullYear();
    const mesec = String(datum.getMonth() + 1).padStart(2, '0');
    const dan = String(datum.getDate()).padStart(2, '0');
    return `${godina}-${mesec}-${dan}`;
  }

  private lokalnoVreme(vrednost: string) {
    const delovi = vrednost.match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})/);
    if (!delovi) return new Date(Number.NaN);
    return new Date(
      Number(delovi[1]),
      Number(delovi[2]) - 1,
      Number(delovi[3]),
      Number(delovi[4]),
      Number(delovi[5])
    );
  }
}
