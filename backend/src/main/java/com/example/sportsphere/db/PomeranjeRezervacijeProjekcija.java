package com.example.sportsphere.db;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface PomeranjeRezervacijeProjekcija {

  Long getIdentifikatorResursa();
  String getTip();
  LocalTime getOtvaranje();
  LocalTime getZatvaranje();
  Long getTrajanjeMinuta();
  LocalDateTime getPocetak();
}
