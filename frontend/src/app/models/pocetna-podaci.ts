export class TopObjekat {
  id = 0;
  naziv = '';
  grad = '';
  brojLajkova = 0;
}
export class PromocijaPregled {
  id = 0;
  naziv = '';
  objekat = '';
  datumOd = '';
  datumDo = '';
  procenat: number | null = null;
  iznos: number | null = null;
}
export class PocetnaPodaci {
  brojObjekata = 0;
  topObjekti: TopObjekat[] = [];
  promocije: PromocijaPregled[] = [];
}
