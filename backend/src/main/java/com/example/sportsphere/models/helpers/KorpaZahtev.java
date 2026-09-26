package com.example.sportsphere.models.helpers;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public class KorpaZahtev {
  @NotEmpty(message= "Korpa je prazna") @Valid private List<Stavka> stavke;
  public List<Stavka> getStavke() {
    return stavke;
  }
  public void setStavke(List<Stavka> v) {
    stavke = v;
  }
  public static class Stavka {
    @NotNull private Long identifikatorOpreme;
    @Min(value = 1, message= "Količina mora biti najmanje 1")
    @Max(value = 100, message= "Količina može biti najviše 100")
    private int kolicina;
    public Long getIdentifikatorOpreme() {
      return identifikatorOpreme;
    }
    public void setIdentifikatorOpreme(Long v) {
      identifikatorOpreme = v;
    }
    public int getKolicina() {
      return kolicina;
    }
    public void setKolicina(int v) {
      kolicina = v;
    }
  }
}
