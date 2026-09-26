package com.example.sportsphere.services;

import com.example.sportsphere.db.ObjekatRepo;
import com.example.sportsphere.db.OrganizacijaRepo;
import com.example.sportsphere.db.PromocijaRepo;
import com.example.sportsphere.db.SportRepo;
import com.example.sportsphere.models.Objekat;
import com.example.sportsphere.models.Promocija;
import com.example.sportsphere.models.Sport;
import com.example.sportsphere.models.helpers.PromocijaZahtev;
import com.example.sportsphere.models.helpers.ZaposleniPromocija;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PromocijaServis {
  private final PromocijaRepo promocijaRepozitorijum;
  private final ObjekatRepo objekatRepozitorijum;
  private final OrganizacijaRepo organizacijaRepozitorijum;
  private final SportRepo sportRepozitorijum;

  public PromocijaServis(PromocijaRepo promocijaRepozitorijum, ObjekatRepo objekatRepozitorijum,
      OrganizacijaRepo organizacijaRepozitorijum, SportRepo sportRepozitorijum) {
    this.promocijaRepozitorijum = promocijaRepozitorijum;
    this.objekatRepozitorijum = objekatRepozitorijum;
    this.organizacijaRepozitorijum = organizacijaRepozitorijum;
    this.sportRepozitorijum = sportRepozitorijum;
  }

  @Transactional(readOnly = true)
  public List<ZaposleniPromocija> dohvati(String korisnickoIme) {
    organizacijaRepozitorijum.pronadjiPoKorisnickomImenuZaposlenog(korisnickoIme).orElseThrow(
        () -> new IllegalArgumentException("Organizacija nije pronađena"));
    return promocijaRepozitorijum.dohvatiZaZaposlenog(korisnickoIme)
        .stream()
        .map(ZaposleniPromocija::new)
        .toList();
  }

  @Transactional
  public ZaposleniPromocija dodaj(String korisnickoIme, PromocijaZahtev zahtev) {
    Promocija promocija = new Promocija();
    primeni(korisnickoIme, promocija, zahtev);
    return new ZaposleniPromocija(promocijaRepozitorijum.sacuvaj(promocija));
  }

  @Transactional
  public ZaposleniPromocija izmeni(String korisnickoIme, Long id, PromocijaZahtev zahtev) {
    Long identifikatorOrganizacije = dohvatiOrganizacijuId(korisnickoIme);
    Promocija promocija =
        promocijaRepozitorijum.pronadjiPoId(id)
            .filter(p -> p.getObjekat().getOrganizacija().getId().equals(identifikatorOrganizacije))
            .orElseThrow(() -> new IllegalArgumentException("Promocija nije pronađena"));
    primeni(korisnickoIme, promocija, zahtev);
    return new ZaposleniPromocija(promocijaRepozitorijum.sacuvaj(promocija));
  }

  private void primeni(String korisnickoIme, Promocija promocija, PromocijaZahtev zahtev) {
    Long identifikatorOrganizacije = dohvatiOrganizacijuId(korisnickoIme);
    Objekat objekat =
        objekatRepozitorijum.pronadjiPoIdIOrganizaciji(zahtev.getIdentifikatorObjekta(), identifikatorOrganizacije)
            .orElseThrow(() -> new IllegalArgumentException("Sportski objekat nije pronađen"));
    if (zahtev.getDatumZavrsetka().isBefore(zahtev.getDatumPocetka()))
      throw new IllegalArgumentException("Krajnji datum ne može biti pre početnog");
    boolean imaProcenat = pozitivan(zahtev.getProcenatPopusta());
    boolean imaIznos = pozitivan(zahtev.getIznosPopusta());
    if (imaProcenat == imaIznos)
      throw new IllegalArgumentException("Unesite ili procenat ili fiksni iznos popusta");

    Sport sport = null;
    if (zahtev.getIdentifikatorSporta() != null) {
      sport = sportRepozitorijum.pronadjiPoId(zahtev.getIdentifikatorSporta())
                  .filter(Sport::jeAktivan)
                  .orElseThrow(() -> new IllegalArgumentException("Izabrani sport nije dostupan"));
      if (objekatRepozitorijum.podrzavaSport(objekat.getId(), sport.getId()) == 0)
        throw new IllegalArgumentException("Izabrani sport nije dostupan u ovom objektu");
    }

    promocija.setObjekat(objekat);
    promocija.setNaziv(zahtev.getNaziv().trim());
    promocija.setDatumPocetka(zahtev.getDatumPocetka());
    promocija.setDatumZavrsetka(zahtev.getDatumZavrsetka());
    promocija.setProcenatPopusta(imaProcenat ? zahtev.getProcenatPopusta() : null);
    promocija.setIznosPopusta(imaIznos ? zahtev.getIznosPopusta() : null);
    promocija.setSport(sport);
    promocija.setAktivan(zahtev.jeAktivan());
  }

  private Long dohvatiOrganizacijuId(String korisnickoIme) {
    return organizacijaRepozitorijum.pronadjiPoKorisnickomImenuZaposlenog(korisnickoIme)
        .orElseThrow(() -> new IllegalArgumentException("Organizacija nije pronađena"))
        .getId();
  }

  private boolean pozitivan(BigDecimal vrednost) {
    return vrednost != null && vrednost.compareTo(BigDecimal.ZERO) > 0;
  }
}
