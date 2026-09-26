package com.example.sportsphere.db;

import java.math.BigDecimal;

public interface ObjekatPretragaProjekcija {
  Long getId();
  String getNaziv();
  String getGrad();
  String getAdresa();
  String getSportovi();
  BigDecimal getNajmanjaCena();
}
