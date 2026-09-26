package com.example.sportsphere.services;

import com.example.sportsphere.db.KorisnikRepo;
import com.example.sportsphere.db.OrganizacijaRepo;
import com.example.sportsphere.db.SportRepo;
import com.example.sportsphere.models.Korisnik;
import com.example.sportsphere.models.Organizacija;
import com.example.sportsphere.models.Sport;
import com.example.sportsphere.models.StatusKorisnika;
import com.example.sportsphere.models.Uloga;
import com.example.sportsphere.models.helpers.RegistracijaZahtev;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RegistracijaServis {
  private final KorisnikRepo korisnikRepozitorijum;
  private final SportRepo sportRepozitorijum;
  private final OrganizacijaRepo organizacijaRepozitorijum;
  private final PasswordEncoder enkoderLozinke;
  private final Path uploadDirektorijum;

  public RegistracijaServis(KorisnikRepo korisnikRepozitorijum, SportRepo sportRepozitorijum,
      OrganizacijaRepo organizacijaRepozitorijum, PasswordEncoder enkoderLozinke,
      @Value("${app.upload.directory:uploads}") String uploadDirektorijum) {
    this.korisnikRepozitorijum = korisnikRepozitorijum;
    this.sportRepozitorijum = sportRepozitorijum;
    this.organizacijaRepozitorijum = organizacijaRepozitorijum;
    this.enkoderLozinke = enkoderLozinke;
    this.uploadDirektorijum = Path.of(uploadDirektorijum).toAbsolutePath().normalize();
  }

  @Transactional
  public void registruj(RegistracijaZahtev zahtev, MultipartFile slika) {
    String korisnickoIme = zahtev.getKorisnickoIme().trim();
    String email = zahtev.getEmail().trim().toLowerCase(Locale.ROOT);
    if (korisnikRepozitorijum.postojiSaKorisnickimImenom(korisnickoIme))
      throw new IllegalArgumentException("Korisničko ime je zauzeto");
    if (korisnikRepozitorijum.postojiSaEmailom(email))
      throw new IllegalArgumentException("Imejl adresa je zauzeta");

    Uloga uloga;
    try {
      uloga = Uloga.valueOf(zahtev.getUloga());
    } catch (Exception e) {
      throw new IllegalArgumentException("Dozvoljena je registracija sportiste ili zaposlenog");
    }
    if (uloga == Uloga.ADMIN)
      throw new IllegalArgumentException("Administrator se ne registruje javno");

    List<Long> identifikatoriSportova = zahtev.getIdentifikatoriOmiljenihSportova() == null
        ? List.of()
        : zahtev.getIdentifikatoriOmiljenihSportova().stream().distinct().toList();
    if (identifikatoriSportova.size() > 5)
      throw new IllegalArgumentException("Možete izabrati najviše pet sportova");
    List<Sport> sportovi = sportRepozitorijum.pronadjiAktivnePoIdentifikatorima(identifikatoriSportova);
    if (sportovi.size() != identifikatoriSportova.size())
      throw new IllegalArgumentException("Izabran je nepostojeći sport");

    Organizacija organizacija = null;
    if (uloga == Uloga.EMPLOYEE)
      organizacija = pripremiOrganizaciju(zahtev);

    Korisnik korisnik = new Korisnik();
    korisnik.setKorisnickoIme(korisnickoIme);
    korisnik.setHesLozinke(enkoderLozinke.encode(zahtev.getLozinka()));
    korisnik.setIme(zahtev.getIme().trim());
    korisnik.setPrezime(zahtev.getPrezime().trim());
    korisnik.setTelefon(zahtev.getTelefon().trim());
    korisnik.setEmail(email);
    korisnik.setUloga(uloga);
    korisnik.setStatus(StatusKorisnika.PENDING);
    korisnik.setOmiljeniSportovi(sportovi);
    korisnik.setPutanjaProfilneSlike(sacuvajSliku(slika));
    korisnikRepozitorijum.sacuvaj(korisnik);

    if (organizacija != null) {
      organizacija.getZaposleni().add(korisnik);
      organizacijaRepozitorijum.sacuvaj(organizacija);
    }
  }

  private Organizacija pripremiOrganizaciju(RegistracijaZahtev zahtev) {
    if (prazno(zahtev.getNazivOrganizacije()) || prazno(zahtev.getRegistrovanaAdresa()))
      throw new IllegalArgumentException("Naziv i adresa objekta su obavezni");
    if (zahtev.getMaticniBroj() == null || !zahtev.getMaticniBroj().matches("\\d{8}"))
      throw new IllegalArgumentException("Matični broj mora imati tačno 8 cifara");
    if (zahtev.getPoreskiBroj() == null || !zahtev.getPoreskiBroj().matches("[1-9]\\d{8}"))
      throw new IllegalArgumentException("PIB mora imati 9 cifara i ne sme početi nulom");

    Organizacija postojeca =
        organizacijaRepozitorijum.pronadjiPoMaticnomBroju(zahtev.getMaticniBroj()).orElse(null);
    if (postojeca != null) {
      if (!postojeca.getPoreskiBroj().equals(zahtev.getPoreskiBroj()))
        throw new IllegalArgumentException("Matični broj i PIB ne pripadaju istoj organizaciji");
      if (postojeca.getZaposleni().size() >= 2)
        throw new IllegalArgumentException("Objekat već ima dva zaposlena");
      return postojeca;
    }
    if (organizacijaRepozitorijum.pronadjiPoPoreskomBroju(zahtev.getPoreskiBroj()).isPresent())
      throw new IllegalArgumentException("PIB je već registrovan");

    Organizacija nova = new Organizacija();
    nova.setNaziv(zahtev.getNazivOrganizacije().trim());
    nova.setRegistrovanaAdresa(zahtev.getRegistrovanaAdresa().trim());
    nova.setMaticniBroj(zahtev.getMaticniBroj());
    nova.setPoreskiBroj(zahtev.getPoreskiBroj());
    return organizacijaRepozitorijum.sacuvaj(nova);
  }

  private String sacuvajSliku(MultipartFile slika) {
    if (slika == null || slika.isEmpty())
      return "default-profile.png";
    String tip = slika.getContentType();
    if (!("image/jpeg".equals(tip) || "image/png".equals(tip)))
      throw new IllegalArgumentException("Profilna slika mora biti PNG ili JPG");
    if (slika.getSize() > 5 * 1024 * 1024)
      throw new IllegalArgumentException("Profilna slika može imati najviše 5 MB");
    String ekstenzija = "image/png".equals(tip) ? ".png" : ".jpg";
    String naziv = UUID.randomUUID() + ekstenzija;
    try {
      Path direktorijum = uploadDirektorijum.resolve("profili");
      Files.createDirectories(direktorijum);
      Files.copy(
          slika.getInputStream(), direktorijum.resolve(naziv), StandardCopyOption.REPLACE_EXISTING);
      return "profili/" + naziv;
    } catch (IOException e) {
      throw new IllegalStateException("Profilna slika nije sačuvana");
    }
  }

  private boolean prazno(String tekst) {
    return tekst == null || tekst.isBlank();
  }
}
