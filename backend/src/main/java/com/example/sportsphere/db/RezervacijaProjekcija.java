package com.example.sportsphere.db;

import java.time.LocalDateTime;

public interface RezervacijaProjekcija {
  Long getId();
  String getObjekat();
  String getGrad();
  String getResurs();
  String getSport();
  LocalDateTime getPocetak();
  LocalDateTime getKraj();
  String getStatus();
}
