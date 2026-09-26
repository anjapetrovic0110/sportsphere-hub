package com.example.sportsphere.services;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.sportsphere.db.PorudzbinaRepo;
import com.example.sportsphere.db.RezervacijaRepo;
import com.example.sportsphere.models.helpers.StatistikaPregled;
@Service
public class StatistikaServis {
  private final RezervacijaRepo rezervacije;
  private final PorudzbinaRepo porudzbine;
  public StatistikaServis(RezervacijaRepo r, PorudzbinaRepo p) {
    rezervacije = r;
    porudzbine = p;
  }
  @Transactional(readOnly = true)
  public StatistikaPregled dohvati(String korisnickoIme) {
    return new StatistikaPregled(rezervacije.statistikaPoSportu(korisnickoIme),
        rezervacije.mesecnaStatistika(korisnickoIme), porudzbine.potrosnjaSvihSportista());
  }
}
