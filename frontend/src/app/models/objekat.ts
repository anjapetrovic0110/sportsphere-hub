export class ObjekatPretraga {
  id = 0;
  naziv = '';
  grad = '';
  adresa = '';
  sportovi = '';
  najmanjaCena: number | null = null;
}

export class ResursPregled {
  id = 0;
  naziv = '';
  tip = '';
  kapacitet: number | null = null;
  oprema = '';
  cena: number | null = null;
  sportovi = '';
}

export class ObjekatDetalji {
  id = 0;
  naziv = '';
  grad = '';
  adresa = '';
  opis = '';
  geografskaSirina: number | null = null;
  geografskaDuzina: number | null = null;
  radnoVremeOd = '';
  radnoVremeDo = '';
  brojLajkova = 0;
  brojDislajkova = 0;
  resursi: ResursPregled[] = [];
  slike: string[] = [];
  komentari: KomentarPregled[] = [];
}

export interface KomentarPregled {
  id: number;
  autor: string;
  reakcija: 'LIKE' | 'DISLIKE';
  komentar: string;
  vreme: string;
  moj: boolean;
}
export interface ReakcijePregled {
  brojLajkova: number;
  brojDislajkova: number;
  preostalo: number;
  komentari: KomentarPregled[];
}
