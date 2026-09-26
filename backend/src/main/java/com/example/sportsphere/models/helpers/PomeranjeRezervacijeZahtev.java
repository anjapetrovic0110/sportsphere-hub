package com.example.sportsphere.models.helpers;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;
public class PomeranjeRezervacijeZahtev {
  @NotNull private LocalDateTime pocetak;
  public LocalDateTime getPocetak() {
    return pocetak;
  }
  public void setPocetak(LocalDateTime v) {
    pocetak = v;
  }
}
