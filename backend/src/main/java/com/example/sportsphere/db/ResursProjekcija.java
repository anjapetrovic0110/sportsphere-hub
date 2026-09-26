package com.example.sportsphere.db;

import java.math.BigDecimal;

public interface ResursProjekcija {
  Long getId();
  String getNaziv();
  String getTip();
  Integer getKapacitet();
  String getOprema();
  BigDecimal getCena();
  String getSportovi();
}
