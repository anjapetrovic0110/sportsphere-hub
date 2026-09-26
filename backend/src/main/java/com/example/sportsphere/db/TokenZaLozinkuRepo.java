package com.example.sportsphere.db;
import java.sql.*;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;
import com.example.sportsphere.models.*;
@Repository
public class TokenZaLozinkuRepo {
  private final DataSource izvorPodataka;
  private final KorisnikRepo korisnici;
  public TokenZaLozinkuRepo(DataSource s, KorisnikRepo k) {
    izvorPodataka = s;
    korisnici = k;
  }
  public Optional<TokenZaLozinku> zakljucajPoHashu(String h) {
    try (Connection v = izvorPodataka.getConnection();
         PreparedStatement u = v.prepareStatement(
             "SELECT * FROM password_reset_tokens WHERE token_hash=? FOR UPDATE")) {
      u.setString(1, h);
      ResultSet r = u.executeQuery();
      if (!r.next())
        return Optional.empty();
      TokenZaLozinku t = new TokenZaLozinku();
      t.setId(r.getLong("id"));
      t.setKorisnik(korisnici.pronadjiPoId(r.getLong("user_id")).orElseThrow());
      t.setHesTokena(r.getString("token_hash"));
      t.setVremeIsteka(r.getTimestamp("expires_at").toLocalDateTime());
      Timestamp p = r.getTimestamp("used_at");
      if (p != null)
        t.setVremeKoriscenja(p.toLocalDateTime());
      return Optional.of(t);
    } catch (SQLException e) {
      throw new IllegalStateException("Token nije učitan", e);
    }
  }
  public int ponistiAktivne(Long id) {
    try (
        Connection v = izvorPodataka.getConnection();
        PreparedStatement u = v.prepareStatement(
            "UPDATE password_reset_tokens SET used_at=CURRENT_TIMESTAMP WHERE user_id=? AND used_at IS NULL")) {
      u.setLong(1, id);
      return u.executeUpdate();
    } catch (SQLException e) {
      throw new IllegalStateException("Tokeni nisu poništeni", e);
    }
  }
  public TokenZaLozinku sacuvaj(TokenZaLozinku t) {
    try (
        Connection v = izvorPodataka.getConnection();
        PreparedStatement u = v.prepareStatement(
            "INSERT INTO password_reset_tokens(user_id,token_hash,expires_at,used_at)VALUES(?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {
      u.setLong(1, t.getKorisnik().getId());
      u.setString(2, t.getHesTokena());
      u.setTimestamp(3, Timestamp.valueOf(t.getVremeIsteka()));
      if (t.getVremeKoriscenja() == null)
        u.setNull(4, Types.TIMESTAMP);
      else
        u.setTimestamp(4, Timestamp.valueOf(t.getVremeKoriscenja()));
      u.executeUpdate();
      ResultSet r = u.getGeneratedKeys();
      if (r.next())
        t.setId(r.getLong(1));
      return t;
    } catch (SQLException e) {
      throw new IllegalStateException("Token nije sačuvan", e);
    }
  }
}
