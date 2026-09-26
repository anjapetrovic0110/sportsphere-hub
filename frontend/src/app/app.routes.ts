import { Routes } from '@angular/router';
import { AdministratorPrijavaKomponenta } from './admin-prijava/admin-prijava.component';
import { RegistracijeKomponenta } from './admin/registracije/registracije.component';
import { IzvestajiKomponenta } from './izvestaji/izvestaji.component';
import { KorisnikPocetnaKomponenta } from './korisnik-pocetna/korisnik-pocetna.component';
import { NovaLozinkaKomponenta } from './nova-lozinka/nova-lozinka.component';
import { NoviObjekatKomponenta } from './novi-objekat/novi-objekat.component';
import { ObjekatDetaljiKomponenta } from './objekat-detalji/objekat-detalji.component';
import { ObjektiKomponenta } from './objekti/objekti.component';
import { OglasiKomponenta } from './oglasi/oglasi.component';
import { OpremaZaposleniKomponenta } from './oprema-zaposleni/oprema-zaposleni.component';
import { PocetnaKomponenta } from './pocetna/pocetna.component';
import { PorudzbineZaposleniKomponenta } from './porudzbine-zaposleni/porudzbine-zaposleni.component';
import { PrijavaKomponenta } from './prijava/prijava.component';
import { ProdavnicaKomponenta } from './prodavnica/prodavnica.component';
import { ProfilKomponenta } from './profil/profil.component';
import { PromocijeKomponenta } from './promocije/promocije.component';
import { RegistracijaKomponenta } from './registracija/registracija.component';
import { adminGuard, sportistaGuard, zaposleniGuard } from './services/uloga.guard';
import { StatistikaKomponenta } from './statistika/statistika.component';
import { TreninziKomponenta } from './treninzi/treninzi.component';
import { UvozObjektaKomponenta } from './uvoz-objekta/uvoz-objekta.component';
import { ZaboravljenaLozinkaKomponenta } from './zaboravljena-lozinka/zaboravljena-lozinka.component';
import { ZaposleniKalendarKomponenta } from './zaposleni-kalendar/zaposleni-kalendar.component';
import { ZaposleniProfilKomponenta } from './zaposleni-profil/zaposleni-profil.component';
import { ZaposleniRezervacijeKomponenta } from './zaposleni-rezervacije/zaposleni-rezervacije.component';

export const routes: Routes = [
  { path: '', component: PocetnaKomponenta },
  { path: 'objekti', component: ObjektiKomponenta },
  { path: 'objekti/:id', component: ObjekatDetaljiKomponenta },
  { path: 'prijava', component: PrijavaKomponenta },
  { path: 'administracija/prijava', component: AdministratorPrijavaKomponenta },
  { path: 'registracija', component: RegistracijaKomponenta },
  { path: 'zaboravljena-lozinka', component: ZaboravljenaLozinkaKomponenta },
  { path: 'nova-lozinka', component: NovaLozinkaKomponenta },
  {
    path: 'sportista',
    component: KorisnikPocetnaKomponenta,
    canActivate: [sportistaGuard]
  },
  {
    path: 'sportista/profil',
    component: ProfilKomponenta,
    canActivate: [sportistaGuard]
  },
  {
    path: 'sportista/oglasi',
    component: OglasiKomponenta,
    canActivate: [sportistaGuard]
  },
  {
    path: 'sportista/treninzi',
    component: TreninziKomponenta,
    canActivate: [sportistaGuard]
  },
  {
    path: 'sportista/prodavnica',
    component: ProdavnicaKomponenta,
    canActivate: [sportistaGuard]
  },
  {
    path: 'sportista/statistika',
    component: StatistikaKomponenta,
    canActivate: [sportistaGuard]
  },
  {
    path: 'zaposleni',
    component: KorisnikPocetnaKomponenta,
    canActivate: [zaposleniGuard]
  },
  {
    path: 'zaposleni/profil',
    component: ZaposleniProfilKomponenta,
    canActivate: [zaposleniGuard]
  },
  {
    path: 'zaposleni/objekti/novi',
    component: NoviObjekatKomponenta,
    canActivate: [zaposleniGuard]
  },
  {
    path: 'zaposleni/objekti/:id/izmena',
    component: NoviObjekatKomponenta,
    canActivate: [zaposleniGuard]
  },
  {
    path: 'zaposleni/objekti/uvoz',
    component: UvozObjektaKomponenta,
    canActivate: [zaposleniGuard]
  },
  {
    path: 'zaposleni/rezervacije',
    component: ZaposleniRezervacijeKomponenta,
    canActivate: [zaposleniGuard]
  },
  {
    path: 'zaposleni/kalendar',
    component: ZaposleniKalendarKomponenta,
    canActivate: [zaposleniGuard]
  },
  {
    path: 'zaposleni/promocije',
    component: PromocijeKomponenta,
    canActivate: [zaposleniGuard]
  },
  {
    path: 'zaposleni/oprema',
    component: OpremaZaposleniKomponenta,
    canActivate: [zaposleniGuard]
  },
  {
    path: 'zaposleni/porudzbine',
    component: PorudzbineZaposleniKomponenta,
    canActivate: [zaposleniGuard]
  },
  {
    path: 'zaposleni/izvestaji',
    component: IzvestajiKomponenta,
    canActivate: [zaposleniGuard]
  },
  { path: 'admin', component: RegistracijeKomponenta, canActivate: [adminGuard] },
  { path: '**', redirectTo: '' }
];
