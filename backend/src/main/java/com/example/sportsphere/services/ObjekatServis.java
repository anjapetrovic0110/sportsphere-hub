package com.example.sportsphere.services;

import com.example.sportsphere.db.ObjekatRepo;
import com.example.sportsphere.db.OrganizacijaRepo;
import com.example.sportsphere.db.SportRepo;
import com.example.sportsphere.models.Objekat;
import com.example.sportsphere.models.Resurs;
import com.example.sportsphere.models.StatusObjekta;
import com.example.sportsphere.models.TipResursa;
import com.example.sportsphere.models.helpers.NoviObjekatZahtev;
import com.example.sportsphere.models.helpers.NoviResursZahtev;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ObjekatServis {
  private final ObjekatRepo objekatRepozitorijum;
  private final OrganizacijaRepo organizacijaRepozitorijum;
  private final SportRepo sportRepozitorijum;
  private final ObjectMapper mapiranjeObjekata;
  private final Validator validator;

  public ObjekatServis(ObjekatRepo objekatRepozitorijum,
      OrganizacijaRepo organizacijaRepozitorijum, SportRepo sportRepozitorijum,
      ObjectMapper mapiranjeObjekata, Validator validator) {
    this.objekatRepozitorijum = objekatRepozitorijum;
    this.organizacijaRepozitorijum = organizacijaRepozitorijum;
    this.sportRepozitorijum = sportRepozitorijum;
    this.mapiranjeObjekata = mapiranjeObjekata;
    this.validator = validator;
  }

  @Transactional
  public void dodaj(String korisnickoIme, NoviObjekatZahtev zahtev) {
    var organizacija = organizacijaRepozitorijum.pronadjiPoKorisnickomImenuZaposlenog(korisnickoIme).orElseThrow(
        () -> new IllegalArgumentException("Organizacija nije pronađena"));
    if (objekatRepozitorijum.postojiUOrganizacijiSaNazivom(organizacija.getId(), zahtev.getNaziv().trim()))
      throw new IllegalArgumentException("Organizacija već ima objekat sa tim nazivom");
    Objekat objekat = new Objekat();
    objekat.setOrganizacija(organizacija);
    primeniPodatke(objekat, zahtev);
    objekatRepozitorijum.sacuvaj(objekat);
  }

  @Transactional(readOnly = true)
  public NoviObjekatZahtev dohvati(String korisnickoIme, Long id) {
    var organizacija = organizacijaRepozitorijum.pronadjiPoKorisnickomImenuZaposlenog(korisnickoIme).orElseThrow(
        () -> new IllegalArgumentException("Organizacija nije pronađena"));
    Objekat objekat = objekatRepozitorijum.pronadjiPoIdIOrganizaciji(id, organizacija.getId())
                          .orElseThrow(() -> new IllegalArgumentException("Objekat nije pronađen"));
    return napraviZahtev(objekat);
  }

  @Transactional
  public void izmeni(String korisnickoIme, Long id, NoviObjekatZahtev zahtev) {
    var organizacija = organizacijaRepozitorijum.pronadjiPoKorisnickomImenuZaposlenog(korisnickoIme).orElseThrow(
        () -> new IllegalArgumentException("Organizacija nije pronađena"));
    Objekat objekat = objekatRepozitorijum.pronadjiPoIdIOrganizaciji(id, organizacija.getId())
                          .orElseThrow(() -> new IllegalArgumentException("Objekat nije pronađen"));
    if (objekatRepozitorijum.postojiUOrganizacijiSaNazivomIDrugacijimId(
            organizacija.getId(), zahtev.getNaziv().trim(), id))
      throw new IllegalArgumentException("Organizacija već ima objekat sa tim nazivom");
    primeniPodatke(objekat, zahtev);
    objekatRepozitorijum.sacuvaj(objekat);
  }

  public void uvezi(String korisnickoIme, MultipartFile fajl) {
    if (fajl == null || fajl.isEmpty())
      throw new IllegalArgumentException("Izaberite JSON fajl");
    if (fajl.getSize() > 1024 * 1024)
      throw new IllegalArgumentException("JSON fajl može imati najviše 1 MB");
    try {
      NoviObjekatZahtev zahtev =
          mapiranjeObjekata.readValue(fajl.getInputStream(), NoviObjekatZahtev.class);
      var greske = validator.validate(zahtev);
      if (!greske.isEmpty())
        throw new IllegalArgumentException(greske.iterator().next().getMessage());
      dodaj(korisnickoIme, zahtev);
    } catch (IOException e) {
      throw new IllegalArgumentException("JSON fajl nije ispravan");
    }
  }

  private void primeniPodatke(Objekat objekat, NoviObjekatZahtev zahtev) {
    if (!zahtev.getVremeZatvaranja().isAfter(zahtev.getVremeOtvaranja()))
      throw new IllegalArgumentException("Vreme zatvaranja mora biti posle otvaranja");
    objekat.setNaziv(zahtev.getNaziv().trim());
    objekat.setGrad(zahtev.getGrad().trim());
    objekat.setAdresa(zahtev.getAdresa().trim());
    objekat.setOpis(zahtev.getOpis());
    objekat.setGeografskaSirina(zahtev.getGeografskaSirina());
    objekat.setGeografskaDuzina(zahtev.getGeografskaDuzina());
    objekat.setVremeOtvaranja(zahtev.getVremeOtvaranja());
    objekat.setVremeZatvaranja(zahtev.getVremeZatvaranja());
    objekat.setDozvoljeniNedolasci(zahtev.getDozvoljeniNedolasci());
    objekat.setStatus(StatusObjekta.PENDING);
    primeniResurse(objekat, zahtev);
  }

  private void primeniResurse(Objekat objekat, NoviObjekatZahtev zahtev) {
    var postojeci = new HashMap<Long, Resurs>();
    for (Resurs resurs : objekat.getResursi()) {
      postojeci.put(resurs.getId(), resurs);
      resurs.setAktivan(false);
    }
    var nazivi = new HashSet<String>();
    for (NoviResursZahtev podaci : zahtev.getResursi()) {
      if (!nazivi.add(podaci.getNaziv().trim().toLowerCase()))
        throw new IllegalArgumentException("Nazivi resursa moraju biti jedinstveni");
      TipResursa tip;
      try {
        tip = TipResursa.valueOf(podaci.getTip());
      } catch (Exception e) {
        throw new IllegalArgumentException("Tip resursa nije ispravan");
      }
      var ids = podaci.getIdentifikatoriSportova().stream().distinct().toList();
      var sportovi = sportRepozitorijum.pronadjiAktivnePoIdentifikatorima(ids);
      if (ids.isEmpty() || sportovi.size() != ids.size())
        throw new IllegalArgumentException("Izabran je nepostojeći sport");
      Resurs resurs = podaci.getId() == null ? new Resurs() : postojeci.get(podaci.getId());
      if (resurs == null)
        throw new IllegalArgumentException("Resurs ne pripada izabranom objektu");
      if (resurs.getId() == null) {
        resurs.setObjekat(objekat);
        objekat.getResursi().add(resurs);
      }
      resurs.setNaziv(podaci.getNaziv().trim());
      resurs.setTip(tip);
      resurs.setKapacitet(podaci.getKapacitet());
      resurs.setOpisOpreme(podaci.getOpisOpreme());
      resurs.setCenaPoSatu(podaci.getCenaPoSatu());
      resurs.setSportovi(sportovi);
      resurs.setAktivan(true);
    }
  }

  private NoviObjekatZahtev napraviZahtev(Objekat objekat) {
    NoviObjekatZahtev z = new NoviObjekatZahtev();
    z.setNaziv(objekat.getNaziv());
    z.setGrad(objekat.getGrad());
    z.setAdresa(objekat.getAdresa());
    z.setOpis(objekat.getOpis());
    z.setGeografskaSirina(objekat.getGeografskaSirina());
    z.setGeografskaDuzina(objekat.getGeografskaDuzina());
    z.setVremeOtvaranja(objekat.getVremeOtvaranja());
    z.setVremeZatvaranja(objekat.getVremeZatvaranja());
    z.setDozvoljeniNedolasci(objekat.getDozvoljeniNedolasci());
    z.setResursi(
        objekat.getResursi().stream().filter(Resurs::jeAktivan).map(this::napraviResurs).toList());
    return z;
  }

  private NoviResursZahtev napraviResurs(Resurs resurs) {
    NoviResursZahtev z = new NoviResursZahtev();
    z.setId(resurs.getId());
    z.setNaziv(resurs.getNaziv());
    z.setTip(resurs.getTip().name());
    z.setKapacitet(resurs.getKapacitet());
    z.setOpisOpreme(resurs.getOpisOpreme());
    z.setCenaPoSatu(resurs.getCenaPoSatu());
    z.setIdentifikatoriSportova(resurs.getSportovi().stream().map(s -> s.getId()).toList());
    return z;
  }
}
