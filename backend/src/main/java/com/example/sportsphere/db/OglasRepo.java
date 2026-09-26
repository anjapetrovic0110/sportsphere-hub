package com.example.sportsphere.db;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;
import com.example.sportsphere.models.*;
@Repository
public class OglasRepo {
  private final DataSource izvorPodataka;
  private final KorisnikRepo korisnici;
  private final SportRepo sportovi;
  public OglasRepo(DataSource s, KorisnikRepo k, SportRepo sp) {
    izvorPodataka = s;
    korisnici = k;
    sportovi = sp;
  }
  public List<Oglas> pronadjiAktivneSortiranoPoDatumuIVremenuIgre() {
    return lista("WHERE active=TRUE ORDER BY play_date,play_time", null);
  }
  public Optional<Oglas> pronadjiPoId(Long id) {
    return lista("WHERE id=?", id).stream().findFirst();
  }
  public Optional<Oglas> pronadjiPoIdZaAzuriranje(Long id) {
    return lista("WHERE id=? FOR UPDATE", id).stream().findFirst();
  }
  public Oglas sacuvaj(Oglas o) {
    boolean novi = o.getId() == null;
    String sql = novi
        ? "INSERT INTO teammate_ads(owner_id,sport_id,city,play_date,play_time,missing_players,active)VALUES(?,?,?,?,?,?,?)"
        : "UPDATE teammate_ads SET owner_id=?,sport_id=?,city=?,play_date=?,play_time=?,missing_players=?,active=? WHERE id=?";
    try (Connection v = izvorPodataka.getConnection();
         PreparedStatement u = v.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      u.setLong(1, o.getVlasnik().getId());
      u.setLong(2, o.getSport().getId());
      u.setString(3, o.getGrad());
      u.setDate(4, java.sql.Date.valueOf(o.getDatumIgre()));
      u.setTime(5, Time.valueOf(o.getVremeIgre()));
      u.setInt(6, o.getNedostajuciIgraci());
      u.setBoolean(7, o.jeAktivan());
      if (!novi)
        u.setLong(8, o.getId());
      u.executeUpdate();
      if (novi) {
        ResultSet r = u.getGeneratedKeys();
        if (r.next())
          o.setId(r.getLong(1));
      }
      return o;
    } catch (SQLException e) {
      throw new IllegalStateException("Oglas nije sačuvan", e);
    }
  }
  private List<Oglas> lista(String dodatak, Object p) {
    try (Connection v = izvorPodataka.getConnection();
         PreparedStatement u = v.prepareStatement("SELECT * FROM teammate_ads " + dodatak)) {
      if (p != null)
        u.setObject(1, p);
      ResultSet r = u.executeQuery();
      List<Oglas> l = new ArrayList<>();
      while (r.next()) {
        Oglas o = new Oglas();
        o.setId(r.getLong("id"));
        o.setVlasnik(korisnici.pronadjiPoId(r.getLong("owner_id")).orElseThrow());
        o.setSport(sportovi.pronadjiPoId(r.getLong("sport_id")).orElseThrow());
        o.setGrad(r.getString("city"));
        o.setDatumIgre(r.getDate("play_date").toLocalDate());
        o.setVremeIgre(r.getTime("play_time").toLocalTime());
        o.setNedostajuciIgraci(r.getInt("missing_players"));
        o.setAktivan(r.getBoolean("active"));
        l.add(o);
      }
      return l;
    } catch (SQLException e) {
      throw new IllegalStateException("Oglasi nisu učitani", e);
    }
  }
}
