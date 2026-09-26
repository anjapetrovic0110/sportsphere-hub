package com.example.sportsphere.services;

import com.example.sportsphere.db.*;
import com.example.sportsphere.models.*;
import com.example.sportsphere.models.helpers.KorpaZahtev;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OpremaServisTest {
  private final OpremaRepo oprema = mock(OpremaRepo.class);
  private final KorisnikRepo korisnici = mock(KorisnikRepo.class);
  private final PorudzbinaRepo porudzbine = mock(PorudzbinaRepo.class);
  private final OpremaServis servis = new OpremaServis(oprema, mock(OrganizacijaRepo.class),
      mock(SportRepo.class), korisnici, porudzbine, "/tmp");
  private Korisnik kupac;
  private Oprema artikal;

  @BeforeEach void setup() {
    kupac = new Korisnik(); kupac.setId(1L); kupac.setUloga(Uloga.ATHLETE);
    when(korisnici.pronadjiPoKorisnickomImenu("ana")).thenReturn(Optional.of(kupac));
    var org = new Organizacija(); org.setId(1L); org.setNaziv("Demo");
    artikal = new Oprema(); artikal.setId(1L); artikal.setNaziv("Lopta");
    artikal.setAktivan(true); artikal.setZaliha(5); artikal.setCena(new BigDecimal("100.00"));
    artikal.setOrganizacija(org);
    when(oprema.zakljucajSve(List.of(1L))).thenReturn(List.of(artikal));
    when(porudzbine.sacuvajSve(any())).thenAnswer(i -> new ArrayList<>(i.<Collection<Porudzbina>>getArgument(0)));
  }
  @Test void orderingDecrementsStockAndCalculatesTotal() {
    var rezultat = servis.naruci("ana", korpa(2));
    assertEquals(3, artikal.getZaliha());
    assertEquals(new BigDecimal("200.00"), rezultat.getFirst().getUkupno());
    verify(oprema).sacuvaj(artikal);
  }
  @Test void insufficientStockDoesNotCreateOrder() {
    assertThrows(IllegalArgumentException.class, () -> servis.naruci("ana", korpa(6)));
    assertEquals(5, artikal.getZaliha());
    verify(oprema, never()).sacuvaj(any());
    verify(porudzbine, never()).sacuvajSve(any());
  }
  @Test void cancellationRestoresStockOnlyOnce() {
    var p = new Porudzbina(); p.setSportista(kupac);
    var stavka = new StavkaPorudzbine(); stavka.setOprema(artikal); stavka.setKolicina(2);
    p.getStavke().add(stavka);
    when(porudzbine.zakljucaj(10L)).thenReturn(Optional.of(p));
    servis.otkazi("ana", 10L);
    assertEquals(7, artikal.getZaliha());
    assertEquals(StatusPorudzbine.CANCELLED, p.getStatus());
    assertThrows(IllegalArgumentException.class, () -> servis.otkazi("ana", 10L));
    assertEquals(7, artikal.getZaliha());
    verify(oprema, times(1)).sacuvaj(artikal);
  }
  private KorpaZahtev korpa(int kolicina) {
    var s = new KorpaZahtev.Stavka(); s.setIdentifikatorOpreme(1L); s.setKolicina(kolicina);
    var k = new KorpaZahtev(); k.setStavke(List.of(s)); return k;
  }
}
