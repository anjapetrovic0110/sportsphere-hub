package com.example.sportsphere.db;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;
import com.example.sportsphere.models.*;
@Repository
public class PromocijaRepo {
  private final DataSource izvorPodataka;
  private final ObjekatRepo objekti;
  private final SportRepo sportovi;
  public PromocijaRepo(DataSource s, ObjekatRepo o, SportRepo sp) {
    izvorPodataka = s;
    objekti = o;
    sportovi = sp;
  }
  public List<Promocija> dohvatiAktuelne(LocalDate datum, int limit) {
    String q =
        "SELECT p.* FROM promotions p JOIN facilities f ON f.id=p.facility_id WHERE p.active=TRUE AND f.status='ACTIVE' AND ? BETWEEN p.starts_on AND p.ends_on ORDER BY p.ends_on LIMIT ?";
    return lista(q, java.sql.Date.valueOf(datum), limit);
  }
  public List<Promocija> dohvatiZaZaposlenog(String korisnickoIme) {
    String q =
        "SELECT p.* FROM promotions p JOIN facilities f ON f.id=p.facility_id JOIN organization_employees oe ON oe.organization_id=f.organization_id JOIN users e ON e.id=oe.employee_id WHERE e.username=? ORDER BY p.starts_on DESC,p.name";
    return lista(q, korisnickoIme);
  }
  public Optional<Promocija> pronadjiPoId(Long id) {
    return lista("SELECT * FROM promotions WHERE id=?", id).stream().findFirst();
  }
  public Promocija sacuvaj(Promocija p) {
    boolean novi = p.getId() == null;
    String q = novi
        ? "INSERT INTO promotions(facility_id,name,starts_on,ends_on,discount_percent,discount_amount,sport_id,active)VALUES(?,?,?,?,?,?,?,?)"
        : "UPDATE promotions SET facility_id=?,name=?,starts_on=?,ends_on=?,discount_percent=?,discount_amount=?,sport_id=?,active=? WHERE id=?";
    try (Connection v = izvorPodataka.getConnection();
         PreparedStatement u = v.prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
      u.setLong(1, p.getObjekat().getId());
      u.setString(2, p.getNaziv());
      u.setDate(3, java.sql.Date.valueOf(p.getDatumPocetka()));
      u.setDate(4, java.sql.Date.valueOf(p.getDatumZavrsetka()));
      u.setBigDecimal(5, p.getProcenatPopusta());
      u.setBigDecimal(6, p.getIznosPopusta());
      if (p.getSport() == null)
        u.setNull(7, Types.BIGINT);
      else
        u.setLong(7, p.getSport().getId());
      u.setBoolean(8, p.jeAktivan());
      if (!novi)
        u.setLong(9, p.getId());
      u.executeUpdate();
      if (novi) {
        ResultSet r = u.getGeneratedKeys();
        if (r.next())
          p.setId(r.getLong(1));
      }
      return p;
    } catch (SQLException e) {
      throw new IllegalStateException("Promocija nije sačuvana", e);
    }
  }
  private List<Promocija> lista(String q, Object... p) {
    try (Connection v = izvorPodataka.getConnection(); PreparedStatement u = v.prepareStatement(q)) {
      for (int i = 0; i < p.length; i++) u.setObject(i + 1, p[i]);
      ResultSet r = u.executeQuery();
      List<Promocija> l = new ArrayList<>();
      while (r.next()) {
        Promocija x = new Promocija();
        x.setId(r.getLong("id"));
        x.setObjekat(objekti.pronadjiPoId(r.getLong("facility_id")).orElseThrow());
        x.setNaziv(r.getString("name"));
        x.setDatumPocetka(r.getDate("starts_on").toLocalDate());
        x.setDatumZavrsetka(r.getDate("ends_on").toLocalDate());
        x.setProcenatPopusta(r.getBigDecimal("discount_percent"));
        x.setIznosPopusta(r.getBigDecimal("discount_amount"));
        long sid = r.getLong("sport_id");
        if (!r.wasNull())
          x.setSport(sportovi.pronadjiPoId(sid).orElse(null));
        x.setAktivan(r.getBoolean("active"));
        l.add(x);
      }
      return l;
    } catch (SQLException e) {
      throw new IllegalStateException("Promocije nisu učitane", e);
    }
  }
}
