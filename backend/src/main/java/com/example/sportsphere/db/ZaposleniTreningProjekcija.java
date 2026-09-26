package com.example.sportsphere.db;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ZaposleniTreningProjekcija {
  Long getId();
  String getSportista();
  String getTrener();
  String getObjekat();
  String getSport();
  LocalDateTime getPocetak();
  LocalDateTime getKraj();
  String getStatus();
  BigDecimal getCena();
  Integer getMozeEvidencija();
}
