import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { PrijavaServis } from './prijava.service';

function proveraUloge(uloga: string): boolean | ReturnType<Router['createUrlTree']> {
  const prijavaServis = inject(PrijavaServis);
  const ruter = inject(Router);
  const korisnik = prijavaServis.dohvatiKorisnika();
  if (korisnik?.uloga == uloga) return true;
  return ruter.createUrlTree([uloga == 'ADMIN' ? '/administracija/prijava' : '/prijava']);
}

export const adminGuard: CanActivateFn = () => proveraUloge('ADMIN');
export const sportistaGuard: CanActivateFn = () => proveraUloge('ATHLETE');
export const zaposleniGuard: CanActivateFn = () => proveraUloge('EMPLOYEE');
