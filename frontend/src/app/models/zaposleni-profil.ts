export class ZaposleniObjekat {
  id = 0;
  naziv = '';
  grad = '';
  adresa = '';
  status = '';
  sportovi = '';
  resursi = '';
}
export class ZaposleniProfil {
  korisnickoIme = '';
  ime = '';
  prezime = '';
  telefon = '';
  imejl = '';
  urlProfilneSlike = '';
  nazivOrganizacije = '';
  adresaSedista = '';
  maticniBroj = '';
  pib = '';
  objekti: ZaposleniObjekat[] = [];
}
