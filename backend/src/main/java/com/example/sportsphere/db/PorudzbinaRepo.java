package com.example.sportsphere.db;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;
import com.example.sportsphere.models.*;
@Repository
public class PorudzbinaRepo {
  private final DataSource izvorPodataka;
  private final KorisnikRepo korisnici;
  private final OrganizacijaRepo organizacije;
  private final OpremaRepo oprema;
  public PorudzbinaRepo(
      DataSource s, KorisnikRepo k, OrganizacijaRepo o, OpremaRepo e) {
    izvorPodataka = s;
    korisnici = k;
    organizacije = o;
    oprema = e;
  }
  public List<Porudzbina> dohvatiZaSportistu(String u) {
    return lista(
        "JOIN users u ON u.id=eo.athlete_id WHERE u.username=? ORDER BY eo.created_at DESC", u);
  }
  public Optional<Porudzbina> zakljucaj(Long id) {
    return lista("WHERE eo.id=? FOR UPDATE", id).stream().findFirst();
  }
  public List<Porudzbina> dohvatiZaZaposlenog(String u) {
    return lista(
        "JOIN organization_employees oe ON oe.organization_id=eo.organization_id JOIN users e ON e.id=oe.employee_id WHERE e.username=? ORDER BY eo.created_at DESC",
        u);
  }
  public List<PrometOpremeProjekcija> izvestajPrometa(String u, LocalDateTime od, LocalDateTime d) {
    return projekcije(
        "SELECT oi.equipment_name naziv,SUM(oi.quantity) quantity,SUM(oi.quantity*oi.unit_price) promet FROM equipment_orders eo JOIN equipment_order_items oi ON oi.order_id=eo.id JOIN organization_employees oe ON oe.organization_id=eo.organization_id JOIN users e ON e.id=oe.employee_id WHERE e.username=? AND eo.status='PICKED_UP' AND eo.updated_at>=? AND eo.updated_at<? GROUP BY oi.equipment_name ORDER BY promet DESC",
        PrometOpremeRed::new, u, od, d);
  }
  public List<PotrosnjaSportistaProjekcija> potrosnjaSvihSportista() {
    return projekcije(
        "SELECT CONCAT(u.first_name,' ',u.last_name) sportista,COALESCE(SUM(eo.total_amount),0) ukupno FROM users u LEFT JOIN equipment_orders eo ON eo.athlete_id=u.id AND eo.status='PICKED_UP' WHERE u.role='ATHLETE' AND u.deleted=FALSE GROUP BY u.id,u.first_name,u.last_name ORDER BY ukupno DESC",
        PotrosnjaSportistaRed::new);
  }
  public List<Porudzbina> sacuvajSve(Collection<Porudzbina> ps) {
    List<Porudzbina> l = new ArrayList<>();
    for (Porudzbina p : ps) l.add(sacuvaj(p));
    return l;
  }
  public Porudzbina sacuvaj(Porudzbina porudzbina) {
    boolean nova = porudzbina.getId() == null;
    String sql = nova
        ? "INSERT INTO equipment_orders(athlete_id,organization_id,status,total_amount) VALUES(?,?,?,?)"
        : "UPDATE equipment_orders SET athlete_id=?,organization_id=?,status=?,total_amount=? WHERE id=?";
    try (Connection veza = izvorPodataka.getConnection()) {
      veza.setAutoCommit(false);
      try {
        try (PreparedStatement upit = veza.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
          upit.setLong(1, porudzbina.getSportista().getId());
          upit.setLong(2, porudzbina.getOrganizacija().getId());
          upit.setString(3, porudzbina.getStatus().name());
          upit.setBigDecimal(4, porudzbina.getUkupanIznos());
          if (!nova)
            upit.setLong(5, porudzbina.getId());
          upit.executeUpdate();
          if (nova) {
            ResultSet kljucevi = upit.getGeneratedKeys();
            if (kljucevi.next())
              porudzbina.setId(kljucevi.getLong(1));
            for (StavkaPorudzbine stavka : porudzbina.getStavke()) {
              ubaciStavku(veza, porudzbina, stavka);
            }
          }
        }
        veza.commit();
        return porudzbina;
      } catch (SQLException e) {
        veza.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Porudžbina nije sačuvana", e);
    }
  }
  private void ubaciStavku(Connection v, Porudzbina p, StavkaPorudzbine s) throws SQLException {
    try (
        PreparedStatement u = v.prepareStatement(
            "INSERT INTO equipment_order_items(order_id,equipment_id,equipment_name,quantity,unit_price)VALUES(?,?,?,?,?)",
            Statement.RETURN_GENERATED_KEYS)) {
      u.setLong(1, p.getId());
      u.setLong(2, s.getOprema().getId());
      u.setString(3, s.getNazivOpreme());
      u.setInt(4, s.getKolicina());
      u.setBigDecimal(5, s.getJedinicnaCena());
      u.executeUpdate();
      ResultSet r = u.getGeneratedKeys();
      if (r.next())
        s.setId(r.getLong(1));
    }
  }
  private List<Porudzbina> lista(String dodatak, Object p) {
    String q = "SELECT DISTINCT eo.* FROM equipment_orders eo " + dodatak;
    try (Connection v = izvorPodataka.getConnection(); PreparedStatement u = v.prepareStatement(q)) {
      u.setObject(1, p);
      ResultSet r = u.executeQuery();
      List<Porudzbina> l = new ArrayList<>();
      while (r.next()) {
        Porudzbina x = new Porudzbina();
        x.setId(r.getLong("id"));
        x.setSportista(korisnici.pronadjiPoId(r.getLong("athlete_id")).orElseThrow());
        x.setOrganizacija(organizacije.pronadjiPoId(r.getLong("organization_id")).orElseThrow());
        x.setStatus(StatusPorudzbine.valueOf(r.getString("status")));
        x.setUkupanIznos(r.getBigDecimal("total_amount"));
        x.setVremeKreiranja(r.getTimestamp("created_at").toLocalDateTime());
        x.setStavke(stavke(v, x));
        l.add(x);
      }
      return l;
    } catch (SQLException e) {
      throw new IllegalStateException("Porudžbine nisu učitane", e);
    }
  }
  private List<StavkaPorudzbine> stavke(Connection v, Porudzbina p) throws SQLException {
    try (PreparedStatement u = v.prepareStatement(
             "SELECT * FROM equipment_order_items WHERE order_id=? ORDER BY id")) {
      u.setLong(1, p.getId());
      ResultSet r = u.executeQuery();
      List<StavkaPorudzbine> l = new ArrayList<>();
      while (r.next()) {
        StavkaPorudzbine s = new StavkaPorudzbine();
        s.setId(r.getLong("id"));
        s.setPorudzbina(p);
        s.setOprema(oprema.pronadjiPoId(r.getLong("equipment_id")).orElseThrow());
        s.setNazivOpreme(r.getString("equipment_name"));
        s.setKolicina(r.getInt("quantity"));
        s.setJedinicnaCena(r.getBigDecimal("unit_price"));
        l.add(s);
      }
      return l;
    }
  }
  private <T> List<T> projekcije(String q, MapiranjeReda<T> mapiranje, Object... p) {
    try (Connection v = izvorPodataka.getConnection(); PreparedStatement u = v.prepareStatement(q)) {
      for (int i = 0; i < p.length; i++) {
        if (p[i] instanceof LocalDateTime t)
          u.setTimestamp(i + 1, Timestamp.valueOf(t));
        else
          u.setObject(i + 1, p[i]);
      }
      ResultSet r = u.executeQuery();
      List<T> l = new ArrayList<>();
      while (r.next()) l.add(mapiranje.mapiraj(r));
      return l;
    } catch (SQLException e) {
      throw new IllegalStateException("Izveštaj nije učitan", e);
    }
  }
  @FunctionalInterface
  private interface MapiranjeReda<T> {
    T mapiraj(ResultSet rezultat) throws SQLException;
  }
  private record PrometOpremeRed(String naziv, Long kolicina, java.math.BigDecimal promet)
      implements PrometOpremeProjekcija {
    PrometOpremeRed(ResultSet r) throws SQLException {
      this(r.getString("naziv"), r.getLong("quantity"), r.getBigDecimal("promet"));
    }
    public String getNaziv() { return naziv; }
    public Long getKolicina() { return kolicina; }
    public java.math.BigDecimal getPromet() { return promet; }
  }
  private record PotrosnjaSportistaRed(String sportista, java.math.BigDecimal ukupno)
      implements PotrosnjaSportistaProjekcija {
    PotrosnjaSportistaRed(ResultSet r) throws SQLException {
      this(r.getString("sportista"), r.getBigDecimal("ukupno"));
    }
    public String getSportista() { return sportista; }
    public java.math.BigDecimal getUkupno() { return ukupno; }
  }
}
