package com.example.sportsphere.models;

import java.time.Instant;

public class StatusAplikacije {
  private String aplikacija;
  private String status;
  private Instant vreme;

  public StatusAplikacije(String aplikacija, String status, Instant vreme) {
    this.aplikacija = aplikacija;
    this.status = status;
    this.vreme = vreme;
  }

  public String getAplikacija() {
    return aplikacija;
  }
  public void setAplikacija(String aplikacija) {
    this.aplikacija = aplikacija;
  }
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }
  public Instant getVreme() {
    return vreme;
  }
  public void setVreme(Instant vreme) {
    this.vreme = vreme;
  }
}
