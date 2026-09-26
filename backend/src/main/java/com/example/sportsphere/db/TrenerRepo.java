package com.example.sportsphere.db;

import com.example.sportsphere.models.Objekat;
import com.example.sportsphere.models.Sport;
import com.example.sportsphere.models.StatusObjekta;
import com.example.sportsphere.models.Trener;
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
public class TrenerRepo {
  private static final String KOLONE = "SELECT t.id,t.first_name,t.last_name,t.specialization,"
      + "t.average_rating,t.hourly_price,t.active,f.id AS facility_id,f.name AS facility_name,"
      + "f.city,f.address,f.opening_time,f.closing_time,f.status AS facility_status,"
      + "s.id AS sport_id,s.name AS sport_name,s.active AS sport_active "
      + "FROM trainers t JOIN facilities f ON f.id=t.facility_id JOIN sports s ON s.id=t.sport_id ";

  private final DataSource izvorPodataka;

  public TrenerRepo(DataSource izvorPodataka) {
    this.izvorPodataka = izvorPodataka;
  }
  public List<Trener> pronadjiSveSortiranoPoPrezimenuPaImenu() {
    return lista(KOLONE + "ORDER BY t.last_name,t.first_name", null);
  }
  public List<Trener> pronadjiAktivnePoStatusuObjektaSortiranoPoPrezimenu(StatusObjekta status) {
    return lista(KOLONE + "WHERE t.active=TRUE AND f.status=? ORDER BY t.last_name,t.first_name",
        status.name());
  }
  public Optional<Trener> pronadjiPoId(Long id) {
    List<Trener> treneri = lista(KOLONE + "WHERE t.id=?", id);
    return treneri.stream().findFirst();
  }
  public Optional<Trener> pronadjiPoIdZaAzuriranje(Long id) {
    List<Trener> treneri = lista(KOLONE + "WHERE t.id=? FOR UPDATE", id);
    return treneri.stream().findFirst();
  }
  public Trener sacuvaj(Trener trener) {
    boolean novi = trener.getId() == null;
    String sql = novi
        ? "INSERT INTO trainers(facility_id,sport_id,first_name,last_name,specialization,average_rating,hourly_price,active) VALUES(?,?,?,?,?,?,?,?)"
        : "UPDATE trainers SET facility_id=?,sport_id=?,first_name=?,last_name=?,specialization=?,average_rating=?,hourly_price=?,active=? WHERE id=?";
    try (Connection veza = izvorPodataka.getConnection();
         PreparedStatement upit = veza.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      upit.setLong(1, trener.getObjekat().getId());
      upit.setLong(2, trener.getSport().getId());
      upit.setString(3, trener.getIme());
      upit.setString(4, trener.getPrezime());
      upit.setString(5, trener.getSpecijalizacija());
      upit.setBigDecimal(6, trener.getProsecnaOcena());
      upit.setBigDecimal(7, trener.getCenaPoSatu());
      upit.setBoolean(8, trener.jeAktivan());
      if (!novi)
        upit.setLong(9, trener.getId());
      upit.executeUpdate();
      if (novi) {
        ResultSet kljucevi = upit.getGeneratedKeys();
        if (kljucevi.next())
          trener.setId(kljucevi.getLong(1));
      }
      return trener;
    } catch (SQLException e) {
      throw new IllegalStateException("Trener nije sačuvan", e);
    }
  }

  private List<Trener> lista(String sql, Object parametar) {
    try (Connection veza = izvorPodataka.getConnection();
         PreparedStatement upit = veza.prepareStatement(sql)) {
      if (parametar != null)
        upit.setObject(1, parametar);
      ResultSet rezultat = upit.executeQuery();
      List<Trener> treneri = new ArrayList<>();
      while (rezultat.next()) treneri.add(mapiraj(rezultat));
      return treneri;
    } catch (SQLException e) {
      throw new IllegalStateException("Treneri nisu učitani", e);
    }
  }

  private Trener mapiraj(ResultSet rezultat) throws SQLException {
    Objekat objekat = new Objekat();
    objekat.setId(rezultat.getLong("facility_id"));
    objekat.setNaziv(rezultat.getString("facility_name"));
    objekat.setGrad(rezultat.getString("city"));
    objekat.setAdresa(rezultat.getString("address"));
    objekat.setVremeOtvaranja(rezultat.getTime("opening_time").toLocalTime());
    objekat.setVremeZatvaranja(rezultat.getTime("closing_time").toLocalTime());
    objekat.setStatus(StatusObjekta.valueOf(rezultat.getString("facility_status")));

    Sport sport = new Sport();
    sport.setId(rezultat.getLong("sport_id"));
    sport.setNaziv(rezultat.getString("sport_name"));
    sport.setAktivan(rezultat.getBoolean("sport_active"));

    Trener trener = new Trener();
    trener.setId(rezultat.getLong("id"));
    trener.setObjekat(objekat);
    trener.setSport(sport);
    trener.setIme(rezultat.getString("first_name"));
    trener.setPrezime(rezultat.getString("last_name"));
    trener.setSpecijalizacija(rezultat.getString("specialization"));
    trener.setProsecnaOcena(rezultat.getBigDecimal("average_rating"));
    trener.setCenaPoSatu(rezultat.getBigDecimal("hourly_price"));
    trener.setAktivan(rezultat.getBoolean("active"));
    return trener;
  }
}
