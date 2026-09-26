package com.example.sportsphere.models.helpers;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
public class NovaLozinkaZahtev {
  @NotBlank private String token;
  @NotBlank
  @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])[A-Za-z].{7,11}$",
      message=
          "Lozinka mora imati 8-12 karaktera, početi slovom i sadržati veliko slovo, broj i specijalni karakter")
  private String lozinka;
  public String getToken() {
    return token;
  }
  public void setToken(String v) {
    token = v;
  }
  public String getLozinka() {
    return lozinka;
  }
  public void setLozinka(String v) {
    lozinka = v;
  }
}
