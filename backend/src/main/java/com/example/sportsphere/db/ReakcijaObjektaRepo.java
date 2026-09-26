package com.example.sportsphere.db;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;
import com.example.sportsphere.models.*;
@Repository
public class ReakcijaObjektaRepo {
  private final DataSource izvorPodataka;
  private final KorisnikRepo korisnici;
  public ReakcijaObjektaRepo(DataSource s, KorisnikRepo k) {
    izvorPodataka = s;
    korisnici = k;
  }
  public long countByFacilityIdAndAthleteId(Long f, Long a) {
    return broj(
        "SELECT COUNT(*) FROM facility_reactions WHERE facility_id=? AND athlete_id=?", f, a);
  }
  public long countByFacilityIdAndReaction(Long f, TipReakcije r) {
    return broj(
        "SELECT COUNT(*) FROM facility_reactions WHERE facility_id=? AND reaction=?", f, r.name());
  }
  public long brojPotvrdjenihRezervacija(Long a, Long f) {
    return broj(
        "SELECT COUNT(*) FROM reservations rv JOIN resources r ON r.id=rv.resource_id WHERE rv.athlete_id=? AND r.facility_id=? AND rv.status IN('CONFIRMED','ATTENDED')",
        a, f);
  }
  public List<ReakcijaObjekta> dohvatiKomentare(Long id, int limit) {
    String sql =
        "SELECT * FROM facility_reactions WHERE facility_id=? AND comment IS NOT NULL AND TRIM(comment)<>'' ORDER BY created_at DESC,id DESC LIMIT ?";
    try (Connection v = izvorPodataka.getConnection(); PreparedStatement u = v.prepareStatement(sql)) {
      u.setLong(1, id);
      u.setInt(2, limit);
      ResultSet x = u.executeQuery();
      List<ReakcijaObjekta> l = new ArrayList<>();
      while (x.next()) {
        ReakcijaObjekta r = new ReakcijaObjekta();
        r.setId(x.getLong("id"));
        Objekat o = new Objekat();
        o.setId(x.getLong("facility_id"));
        r.setObjekat(o);
        r.setSportista(korisnici.pronadjiPoId(x.getLong("athlete_id")).orElseThrow());
        r.setReakcija(TipReakcije.valueOf(x.getString("reaction")));
        r.setKomentar(x.getString("comment"));
        r.setVremeKreiranja(x.getTimestamp("created_at").toLocalDateTime());
        l.add(r);
      }
      return l;
    } catch (SQLException e) {
      throw new IllegalStateException("Komentari nisu učitani", e);
    }
  }
  public ReakcijaObjekta sacuvaj(ReakcijaObjekta r) {
    try (
        Connection v = izvorPodataka.getConnection();
        PreparedStatement u = v.prepareStatement(
            "INSERT INTO facility_reactions(facility_id,athlete_id,reaction,comment)VALUES(?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {
      u.setLong(1, r.getObjekat().getId());
      u.setLong(2, r.getSportista().getId());
      u.setString(3, r.getReakcija().name());
      u.setString(4, r.getKomentar());
      u.executeUpdate();
      ResultSet x = u.getGeneratedKeys();
      if (x.next())
        r.setId(x.getLong(1));
      return r;
    } catch (SQLException e) {
      throw new IllegalStateException("Reakcija nije sačuvana", e);
    }
  }
  private long broj(String q, Object... p) {
    try (Connection v = izvorPodataka.getConnection(); PreparedStatement u = v.prepareStatement(q)) {
      for (int i = 0; i < p.length; i++) u.setObject(i + 1, p[i]);
      ResultSet r = u.executeQuery();
      r.next();
      return r.getLong(1);
    } catch (SQLException e) {
      throw new IllegalStateException("Reakcije nisu izbrojane", e);
    }
  }
}
