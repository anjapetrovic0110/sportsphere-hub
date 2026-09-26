export interface SportStatistika {
  sport: string;
  odigrano: number;
  rezervisano: number;
}
export interface MesecStatistika {
  mesec: string;
  broj: number;
}
export interface PotrosnjaStatistika {
  sportista: string;
  ukupno: number;
}
export interface Statistika {
  sportovi: SportStatistika[];
  meseci: MesecStatistika[];
  potrosnja: PotrosnjaStatistika[];
}
