package com.example.sportsphere.db;
import java.time.LocalTime;

public interface ResursRezervacijaProjekcija {
  Long getId();
  LocalTime getOtvaranje();
  LocalTime getZatvaranje();
}
