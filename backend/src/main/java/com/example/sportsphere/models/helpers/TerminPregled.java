package com.example.sportsphere.models.helpers;
import com.example.sportsphere.db.TerminProjekcija;
import java.time.LocalDateTime;
public class TerminPregled {
  private LocalDateTime pocetak;
  private LocalDateTime kraj;
  public TerminPregled(TerminProjekcija t) {
    pocetak = t.getPocetak();
    kraj = t.getKraj();
  }
  public LocalDateTime getPocetak() {
    return pocetak;
  }
  public LocalDateTime getKraj() {
    return kraj;
  }
}
