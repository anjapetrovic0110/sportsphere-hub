package com.example.sportsphere.db;
import java.time.LocalDateTime;

public interface ZaposleniRezervacijaProjekcija {
  Long getId();
  String getSportista();
  String getObjekat();
  String getResurs();
  String getSport();
  String getTip();
  LocalDateTime getPocetak();
  LocalDateTime getKraj();
  String getStatus();
  Long getMozeEvidencija();
}
