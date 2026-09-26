package com.example.sportsphere.db;

import com.example.sportsphere.models.Sport;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class SportRepo {
  private final DataSource izvorPodataka;

  public SportRepo(DataSource izvorPodataka) {
    this.izvorPodataka = izvorPodataka;
  }
  public List<Sport> pronadjiAktivneSortiranoPoNazivu() {
    return lista("SELECT id,name,active FROM sports WHERE active=TRUE ORDER BY name");
  }
  public List<Sport> pronadjiAktivnePoIdentifikatorima(List<Long> ids) {
    if (ids.isEmpty())
      return List.of();
    String mesta = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
    try (Connection veza = izvorPodataka.getConnection();
         PreparedStatement upit = veza.prepareStatement(
             "SELECT id,name,active FROM sports WHERE active=TRUE AND id IN (" + mesta + ")")) {
      for (int i = 0; i < ids.size(); i++) upit.setLong(i + 1, ids.get(i));
      return procitajListu(upit.executeQuery());
    } catch (SQLException e) {
      throw new IllegalStateException("Sportovi nisu učitani", e);
    }
  }
  public Optional<Sport> pronadjiPoId(Long id) {
    try (Connection veza = izvorPodataka.getConnection();
         PreparedStatement upit =
             veza.prepareStatement("SELECT id,name,active FROM sports WHERE id=?")) {
      upit.setLong(1, id);
      ResultSet rezultat = upit.executeQuery();
      return rezultat.next() ? Optional.of(mapiraj(rezultat)) : Optional.empty();
    } catch (SQLException e) {
      throw new IllegalStateException("Sport nije učitan", e);
    }
  }
  public boolean postojiNazivBezObziraNaVelicinuSlova(String naziv) {
    try (Connection veza = izvorPodataka.getConnection();
         PreparedStatement upit =
             veza.prepareStatement("SELECT COUNT(*) FROM sports WHERE LOWER(name)=LOWER(?)")) {
      upit.setString(1, naziv);
      ResultSet rezultat = upit.executeQuery();
      rezultat.next();
      return rezultat.getLong(1) > 0;
    } catch (SQLException e) {
      throw new IllegalStateException("Sport nije proveren", e);
    }
  }
  public Sport sacuvaj(Sport sport) {
    String sql = sport.getId() == null ? "INSERT INTO sports(name,active) VALUES(?,?)"
                                       : "UPDATE sports SET name=?,active=? WHERE id=?";
    try (Connection veza = izvorPodataka.getConnection();
         PreparedStatement upit = veza.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      upit.setString(1, sport.getNaziv());
      upit.setBoolean(2, sport.jeAktivan());
      if (sport.getId() != null)
        upit.setLong(3, sport.getId());
      upit.executeUpdate();
      if (sport.getId() == null) {
        ResultSet kljucevi = upit.getGeneratedKeys();
        if (kljucevi.next())
          sport.setId(kljucevi.getLong(1));
      }
      return sport;
    } catch (SQLException e) {
      throw new IllegalStateException("Sport nije sačuvan", e);
    }
  }

  private List<Sport> lista(String sql) {
    try (Connection veza = izvorPodataka.getConnection();
         PreparedStatement upit = veza.prepareStatement(sql)) {
      return procitajListu(upit.executeQuery());
    } catch (SQLException e) {
      throw new IllegalStateException("Sportovi nisu učitani", e);
    }
  }

  private List<Sport> procitajListu(ResultSet rezultat) throws SQLException {
    List<Sport> sportovi = new ArrayList<>();
    while (rezultat.next()) sportovi.add(mapiraj(rezultat));
    return sportovi;
  }

  private Sport mapiraj(ResultSet rezultat) throws SQLException {
    Sport sport = new Sport();
    sport.setId(rezultat.getLong("id"));
    sport.setNaziv(rezultat.getString("name"));
    sport.setAktivan(rezultat.getBoolean("active"));
    return sport;
  }
}
