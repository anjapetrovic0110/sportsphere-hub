export class NoviResurs {
  id: number | null = null;
  naziv = '';
  tip = 'OUTDOOR_FIELD';
  kapacitet = 4;
  opisOpreme = '';
  cenaPoSatu = 0;
  idSportova: number[] = [];
}
export class NoviObjekat {
  naziv = '';
  grad = '';
  adresa = '';
  opis = '';
  geografskaSirina: number | null = null;
  geografskaDuzina: number | null = null;
  vremeOtvaranja = '08:00';
  vremeZatvaranja = '22:00';
  dozvoljeniNedolasci = 3;
  resursi: NoviResurs[] = [new NoviResurs()];
}
export class ObjekatZahtev {
  id = 0;
  naziv = '';
  grad = '';
  adresa = '';
  organizacija = '';
  brojResursa = 0;
}
