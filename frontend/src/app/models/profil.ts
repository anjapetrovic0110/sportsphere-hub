export class Profil {
  korisnickoIme = '';
  ime = '';
  prezime = '';
  telefon = '';
  imejl = '';
  urlProfilneSlike = '';
  idOmiljenihSportova: number[] = [];
}

export class RezervacijaPregled {
  id = 0;
  objekat = '';
  grad = '';
  resurs = '';
  sport = '';
  pocetak = '';
  kraj = '';
  status = '';
  mozeOtkazivanje = false;
}

export class TerminPregled {
  pocetak = '';
  kraj = '';
}
export class NovaRezervacija {
  idResursa: number | null = null;
  idSporta: number | null = null;
  datum = '';
  satOd = 8;
  satDo = 9;
}
