package com.example.sportsphere.db;

import com.example.sportsphere.models.*;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class KorisnikRepo {
  private final DataSource izvorPodataka;
  public KorisnikRepo(DataSource izvorPodataka) {
    this.izvorPodataka = izvorPodataka;
  }

  public Optional<Korisnik> pronadjiPoId(Long id) {
    return jedan("u.id=?", id);
  }
  public Optional<Korisnik> pronadjiPoKorisnickomImenu(String korisnickoIme) {
    return jedan("u.username=?", korisnickoIme);
  }
  public Optional<Korisnik> pronadjiPoEmailu(String email) {
    return jedan("u.email=?", email);
  }
  public boolean postojiSaKorisnickimImenom(String korisnickoIme) {
    return postoji("username=?", korisnickoIme);
  }
  public boolean postojiSaEmailom(String email) {
    return postoji("email=?", email);
  }

  public boolean postojiSaEmailomIDrugacijimId(String email, Long id) {
    try (Connection veza = izvorPodataka.getConnection();
         PreparedStatement u =
             veza.prepareStatement("SELECT COUNT(*) FROM users WHERE email=? AND id<>?")) {
      u.setString(1, email);
      u.setLong(2, id);
      ResultSet r = u.executeQuery();
      r.next();
      return r.getLong(1) > 0;
    } catch (SQLException e) {
      throw greska(e);
    }
  }

  public List<Korisnik> pronadjiPoStatusuSortiranoPoVremenuKreiranja(StatusKorisnika status) {
    return lista("u.status=? ORDER BY u.created_at", status.name());
  }

  public List<Korisnik> pronadjiNeobrisaneSaDrugacijomUlogomSortiranoPoKorisnickomImenu(Uloga uloga) {
    return lista("u.role<>? AND u.deleted=FALSE ORDER BY u.username", uloga.name());
  }

  public Korisnik sacuvaj(Korisnik k) {
    try (Connection veza = izvorPodataka.getConnection()) {
      veza.setAutoCommit(false);
      try {
        if (k.getId() == null)
          ubaci(veza, k);
        else
          izmeni(veza, k);
        try (PreparedStatement brisi =
                 veza.prepareStatement("DELETE FROM user_favorite_sports WHERE user_id=?")) {
          brisi.setLong(1, k.getId());
          brisi.executeUpdate();
        }
        try (PreparedStatement dodaj = veza.prepareStatement(
                 "INSERT INTO user_favorite_sports(user_id,sport_id) VALUES(?,?)")) {
          for (Sport s : k.getOmiljeniSportovi()) {
            dodaj.setLong(1, k.getId());
            dodaj.setLong(2, s.getId());
            dodaj.addBatch();
          }
          dodaj.executeBatch();
        }
        veza.commit();
        return k;
      } catch (SQLException e) {
        veza.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Korisnik nije sačuvan", e);
    }
  }

  private void ubaci(Connection v, Korisnik k) throws SQLException {
    String sql =
        "INSERT INTO users(username,password_hash,first_name,last_name,phone,email,profile_image_path,role,status,deleted) VALUES(?,?,?,?,?,?,?,?,?,?)";
    try (PreparedStatement u = v.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      postavi(u, k, false);
      u.executeUpdate();
      ResultSet r = u.getGeneratedKeys();
      if (r.next())
        k.setId(r.getLong(1));
    }
  }

  private void izmeni(Connection v, Korisnik k) throws SQLException {
    String sql =
        "UPDATE users SET username=?,password_hash=?,first_name=?,last_name=?,phone=?,email=?,profile_image_path=?,role=?,status=?,deleted=? WHERE id=?";
    try (PreparedStatement u = v.prepareStatement(sql)) {
      postavi(u, k, true);
      u.executeUpdate();
    }
  }

  private void postavi(PreparedStatement u, Korisnik k, boolean id) throws SQLException {
    u.setString(1, k.getKorisnickoIme());
    u.setString(2, k.getHesLozinke());
    u.setString(3, k.getIme());
    u.setString(4, k.getPrezime());
    u.setString(5, k.getTelefon());
    u.setString(6, k.getEmail());
    u.setString(7, k.getPutanjaProfilneSlike());
    u.setString(8, k.getUloga().name());
    u.setString(9, k.getStatus().name());
    u.setBoolean(10, k.jeObrisan());
    if (id)
      u.setLong(11, k.getId());
  }

  private Optional<Korisnik> jedan(String uslov, Object vrednost) {
    List<Korisnik> l = lista(uslov, vrednost);
    return l.stream().findFirst();
  }
  private List<Korisnik> lista(String uslov, Object vrednost) {
    String sql = "SELECT u.* FROM users u WHERE " + uslov;
    try (Connection v = izvorPodataka.getConnection(); PreparedStatement u = v.prepareStatement(sql)) {
      u.setObject(1, vrednost);
      ResultSet r = u.executeQuery();
      List<Korisnik> l = new ArrayList<>();
      while (r.next()) {
        Korisnik k = mapiraj(r);
        k.setOmiljeniSportovi(sportovi(v, k.getId()));
        l.add(k);
      }
      return l;
    } catch (SQLException e) {
      throw greska(e);
    }
  }
  private List<Sport> sportovi(Connection v, Long id) throws SQLException {
    try (
        PreparedStatement u = v.prepareStatement(
            "SELECT s.id,s.name,s.active FROM sports s JOIN user_favorite_sports us ON us.sport_id=s.id WHERE us.user_id=? ORDER BY s.name")) {
      u.setLong(1, id);
      ResultSet r = u.executeQuery();
      List<Sport> l = new ArrayList<>();
      while (r.next()) {
        Sport s = new Sport();
        s.setId(r.getLong(1));
        s.setNaziv(r.getString(2));
        s.setAktivan(r.getBoolean(3));
        l.add(s);
      }
      return l;
    }
  }
  private Korisnik mapiraj(ResultSet r) throws SQLException {
    Korisnik k = new Korisnik();
    k.setId(r.getLong("id"));
    k.setKorisnickoIme(r.getString("username"));
    k.setHesLozinke(r.getString("password_hash"));
    k.setIme(r.getString("first_name"));
    k.setPrezime(r.getString("last_name"));
    k.setTelefon(r.getString("phone"));
    k.setEmail(r.getString("email"));
    k.setPutanjaProfilneSlike(r.getString("profile_image_path"));
    k.setUloga(Uloga.valueOf(r.getString("role")));
    k.setStatus(StatusKorisnika.valueOf(r.getString("status")));
    k.setObrisan(r.getBoolean("deleted"));
    Timestamp c = r.getTimestamp("created_at");
    Timestamp a = r.getTimestamp("updated_at");
    if (c != null)
      k.setVremeKreiranja(c.toLocalDateTime());
    if (a != null)
      k.setVremeAzuriranja(a.toLocalDateTime());
    return k;
  }
  private boolean postoji(String uslov, Object v) {
    try (Connection c = izvorPodataka.getConnection();
         PreparedStatement u = c.prepareStatement("SELECT COUNT(*) FROM users WHERE " + uslov)) {
      u.setObject(1, v);
      ResultSet r = u.executeQuery();
      r.next();
      return r.getLong(1) > 0;
    } catch (SQLException e) {
      throw greska(e);
    }
  }
  private IllegalStateException greska(SQLException e) {
    return new IllegalStateException("Korisnici nisu učitani", e);
  }
}
