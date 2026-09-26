package com.example.sportsphere.services;

import com.example.sportsphere.db.*;
import com.example.sportsphere.models.*;
import com.example.sportsphere.models.helpers.NovaRezervacijaZahtev;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfilServisTest {
  private final KorisnikRepo korisnici = mock(KorisnikRepo.class);
  private final RezervacijaRepo rezervacije = mock(RezervacijaRepo.class);
  private final ProfilServis servis = new ProfilServis(korisnici, mock(SportRepo.class), rezervacije,
      mock(ObjekatRepo.class), mock(OrganizacijaRepo.class), mock(IndividualniTreningRepo.class), "/tmp");

  @BeforeEach void setup() {
    var korisnik = new Korisnik(); korisnik.setUloga(Uloga.ATHLETE);
    when(korisnici.pronadjiPoKorisnickomImenu("ana")).thenReturn(Optional.of(korisnik));
    var resurs = mock(ResursRezervacijaProjekcija.class);
    when(resurs.getOtvaranje()).thenReturn(LocalTime.of(7, 0));
    when(resurs.getZatvaranje()).thenReturn(LocalTime.of(22, 0));
    when(rezervacije.zakljucajResurs(1L)).thenReturn(Optional.of(resurs));
    when(rezervacije.podrzavaSport(1L, 1L)).thenReturn(1L);
    when(rezervacije.rezervisi(anyString(), anyLong(), anyLong(), any(), any())).thenReturn(1);
  }

  @Test void midnightCannotBypassClosingTime() { rejects(zahtev(21, 24)); }
  @Test void afterClosingIsRejected() { rejects(zahtev(21, 23)); }
  @Test void beforeOpeningIsRejected() { rejects(zahtev(6, 8)); }
  @Test void zeroDurationIsRejected() { rejects(zahtev(10, 10)); }
  @Test void overlappingReservationIsRejected() {
    when(rezervacije.brojPreklapanja(eq(1L), any(), any())).thenReturn(1L);
    rejects(zahtev(10, 11));
  }
  @Test void reservationMayEndExactlyAtClosing() {
    var z = zahtev(21, 22);
    servis.rezervisi("ana", z);
    verify(rezervacije).rezervisi("ana", 1L, 1L, z.getDatum().atTime(21, 0), z.getDatum().atTime(22, 0));
  }
  private void rejects(NovaRezervacijaZahtev z) {
    assertThrows(IllegalArgumentException.class, () -> servis.rezervisi("ana", z));
    verify(rezervacije, never()).rezervisi(anyString(), anyLong(), anyLong(), any(), any());
  }
  private NovaRezervacijaZahtev zahtev(int od, int doSata) {
    var z = new NovaRezervacijaZahtev();
    z.setIdentifikatorResursa(1L); z.setIdentifikatorSporta(1L);
    z.setDatum(LocalDate.now().plusDays(2)); z.setSatOd(od); z.setSatDo(doSata); return z;
  }
}
