package com.example.sportsphere.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class RezervacijaRepo {
  private final DataSource izvorPodataka;

  public RezervacijaRepo(DataSource izvorPodataka) {
    this.izvorPodataka = izvorPodataka;
  }

  public List<RezervacijaProjekcija> dohvatiZaSportistu(String korisnickoIme) {
    String sql =
        "SELECT rv.id,f.name objekat,f.city city,r.name resurs,s.name sport,rv.starts_at pocetak,"
        + "rv.ends_at kraj,rv.status FROM reservations rv JOIN users u ON u.id=rv.athlete_id "
        + "JOIN resources r ON r.id=rv.resource_id JOIN facilities f ON f.id=r.facility_id "
        + "JOIN sports s ON s.id=rv.sport_id WHERE u.username=? ORDER BY rv.starts_at DESC";
    return lista(sql, RezervacijaRed::new, korisnickoIme);
  }

  public int otkazi(Long id, String korisnickoIme, LocalDateTime granica) {
    return izmena(
        "UPDATE reservations rv JOIN users u ON u.id=rv.athlete_id SET rv.status='CANCELLED' "
            + "WHERE rv.id=? AND u.username=? AND rv.status IN('PENDING','CONFIRMED') AND rv.starts_at>=?",
        id, korisnickoIme, granica);
  }

  public Optional<ResursRezervacijaProjekcija> zakljucajResurs(Long id) {
    return jedan("SELECT r.id,f.opening_time otvaranje,f.closing_time zatvaranje FROM resources r "
            + "JOIN facilities f ON f.id=r.facility_id WHERE r.id=? AND r.active=TRUE AND f.status='ACTIVE' FOR UPDATE",
        ResursRezervacijaRed::new, id);
  }

  public List<TerminProjekcija> dohvatiZauzete(Long id, LocalDateTime od, LocalDateTime doVreme) {
    return lista("SELECT starts_at pocetak,ends_at kraj FROM reservations WHERE resource_id=? "
            + "AND status IN('PENDING','CONFIRMED') AND starts_at<? AND ends_at>? ORDER BY starts_at",
        TerminRed::new, id, doVreme, od);
  }

  public long podrzavaSport(Long identifikatorResursa, Long identifikatorSporta) {
    return broj("SELECT COUNT(*) FROM resource_sports rs JOIN sports s ON s.id=rs.sport_id "
            + "WHERE rs.resource_id=? AND rs.sport_id=? AND s.active=TRUE",
        identifikatorResursa, identifikatorSporta);
  }

  public long brojPreklapanja(Long id, LocalDateTime pocetak, LocalDateTime kraj) {
    return broj(
        "SELECT COUNT(*) FROM reservations WHERE resource_id=? AND status IN('PENDING','CONFIRMED') "
            + "AND starts_at<? AND ends_at>?",
        id, kraj, pocetak);
  }

  public int rezervisi(
      String korisnickoIme, Long identifikatorResursa, Long identifikatorSporta, LocalDateTime pocetak, LocalDateTime kraj) {
    return izmena(
        "INSERT INTO reservations(athlete_id,resource_id,sport_id,starts_at,ends_at,status) "
            + "SELECT id,?,?,?,?,'PENDING' FROM users WHERE username=?",
        identifikatorResursa, identifikatorSporta, pocetak, kraj, korisnickoIme);
  }

  public List<ZaposleniRezervacijaProjekcija> dohvatiZaZaposlenog(String korisnickoIme) {
    LocalDateTime sada = LocalDateTime.now(java.time.ZoneId.of("Europe/Belgrade"));
    String sql =
        "SELECT rv.id,CONCAT(a.first_name,' ',a.last_name) sportista,f.name objekat,r.name resurs,"
        + "r.type type,s.name sport,rv.starts_at pocetak,rv.ends_at kraj,rv.status,"
        + "(? <=DATE_ADD(rv.starts_at,INTERVAL 10 MINUTE) "
        + "AND rv.status IN('PENDING','CONFIRMED')) mozeEvidencija FROM reservations rv "
        + "JOIN users a ON a.id=rv.athlete_id JOIN resources r ON r.id=rv.resource_id "
        + "JOIN facilities f ON f.id=r.facility_id JOIN sports s ON s.id=rv.sport_id "
        + "JOIN organization_employees oe ON oe.organization_id=f.organization_id "
        + "JOIN users e ON e.id=oe.employee_id WHERE e.username=? ORDER BY rv.starts_at DESC";
    return lista(sql, ZaposleniRezervacijaRed::new, sada, korisnickoIme);
  }

  public int evidentiraj(String korisnickoIme, Long id, String status) {
    LocalDateTime sada = LocalDateTime.now(java.time.ZoneId.of("Europe/Belgrade"));
    String sql = "UPDATE reservations rv JOIN resources r ON r.id=rv.resource_id "
        + "JOIN facilities f ON f.id=r.facility_id JOIN organization_employees oe ON oe.organization_id=f.organization_id "
        + "JOIN users e ON e.id=oe.employee_id SET rv.status=? WHERE rv.id=? AND e.username=? "
        + "AND rv.status IN('PENDING','CONFIRMED') "
        + "AND ?<=DATE_ADD(rv.starts_at,INTERVAL 10 MINUTE)";
    return izmena(sql, status, id, korisnickoIme, sada);
  }

  public Optional<NedolazakProjekcija> podaciNedolaska(Long id) {
    return jedan("SELECT f.id identifikatorObjekta,rv.athlete_id identifikatorSportiste,f.allowed_no_shows dozvoljeno "
            + "FROM reservations rv JOIN resources r ON r.id=rv.resource_id "
            + "JOIN facilities f ON f.id=r.facility_id WHERE rv.id=?",
        NedolazakRed::new, id);
  }

  public long brojNedolazaka(Long identifikatorSportiste, Long identifikatorObjekta) {
    return broj("SELECT COUNT(*) FROM reservations rv JOIN resources r ON r.id=rv.resource_id "
            + "WHERE rv.athlete_id=? AND r.facility_id=? AND rv.status='NO_SHOW'",
        identifikatorSportiste, identifikatorObjekta);
  }

  public int blokiraj(Long identifikatorSportiste, Long identifikatorObjekta) {
    return izmena(
        "INSERT IGNORE INTO facility_athlete_blocks(facility_id,athlete_id,blocked_at) VALUES(?,?,NOW())",
        identifikatorObjekta, identifikatorSportiste);
  }

  public long blokiran(String korisnickoIme, Long identifikatorResursa) {
    return broj(
        "SELECT COUNT(*) FROM facility_athlete_blocks b JOIN resources r ON r.facility_id=b.facility_id "
            + "JOIN users u ON u.id=b.athlete_id WHERE r.id=? AND u.username=?",
        identifikatorResursa, korisnickoIme);
  }

  public long blokiranUObjektu(String korisnickoIme, Long identifikatorObjekta) {
    return broj(
        "SELECT COUNT(*) FROM facility_athlete_blocks b JOIN users u ON u.id=b.athlete_id "
            + "WHERE b.facility_id=? AND u.username=?",
        identifikatorObjekta, korisnickoIme);
  }

  public List<SportStatistikaProjekcija> statistikaPoSportu(String korisnickoIme) {
    String sql =
        "SELECT s.name sport,SUM(CASE WHEN rv.status='ATTENDED' THEN 1 ELSE 0 END) odigrano,"
        + "SUM(CASE WHEN rv.status IN('PENDING','CONFIRMED') THEN 1 ELSE 0 END) rezervisano "
        + "FROM reservations rv JOIN sports s ON s.id=rv.sport_id JOIN users u ON u.id=rv.athlete_id "
        + "WHERE u.username=? AND rv.status IN('PENDING','CONFIRMED','ATTENDED') "
        + "GROUP BY s.id,s.name ORDER BY s.name";
    return lista(sql, SportStatistikaRed::new, korisnickoIme);
  }

  public List<MesecStatistikaProjekcija> mesecnaStatistika(String korisnickoIme) {
    String sql =
        "SELECT DATE_FORMAT(rv.starts_at,'%Y-%m') mesec,COUNT(*) broj FROM reservations rv "
        + "JOIN users u ON u.id=rv.athlete_id WHERE u.username=? "
        + "AND rv.status IN('PENDING','CONFIRMED','ATTENDED') "
        + "AND rv.starts_at>=DATE_SUB(CURRENT_DATE,INTERVAL 11 MONTH) "
        + "GROUP BY DATE_FORMAT(rv.starts_at,'%Y-%m') ORDER BY mesec";
    return lista(sql, MesecStatistikaRed::new, korisnickoIme);
  }

  public Optional<PomeranjeRezervacijeProjekcija> podaciZaPomeranje(String korisnickoIme, Long id) {
    String sql =
        "SELECT rv.resource_id identifikatorResursa,r.type type,f.opening_time otvaranje,f.closing_time zatvaranje,"
        + "TIMESTAMPDIFF(MINUTE,rv.starts_at,rv.ends_at) trajanjeMinuta,rv.starts_at pocetak "
        + "FROM reservations rv JOIN resources r ON r.id=rv.resource_id JOIN facilities f ON f.id=r.facility_id "
        + "JOIN organization_employees oe ON oe.organization_id=f.organization_id "
        + "JOIN users e ON e.id=oe.employee_id WHERE rv.id=? AND e.username=? "
        + "AND rv.status IN('PENDING','CONFIRMED')";
    return jedan(sql, PomeranjeRezervacijeRed::new, id, korisnickoIme);
  }

  public int pomeri(String korisnickoIme, Long id, LocalDateTime pocetak, LocalDateTime kraj) {
    String sql = "UPDATE reservations rv JOIN resources r ON r.id=rv.resource_id "
        + "JOIN facilities f ON f.id=r.facility_id JOIN organization_employees oe ON oe.organization_id=f.organization_id "
        + "JOIN users e ON e.id=oe.employee_id SET rv.starts_at=?,rv.ends_at=? "
        + "WHERE rv.id=? AND e.username=? AND rv.status IN('PENDING','CONFIRMED') "
        + "AND NOT EXISTS(SELECT 1 FROM (SELECT id,resource_id,starts_at,ends_at,status FROM reservations) x "
        + "WHERE x.id<>? AND x.resource_id=rv.resource_id AND x.status IN('PENDING','CONFIRMED') "
        + "AND x.starts_at<? AND x.ends_at>?)";
    return izmena(sql, pocetak, kraj, id, korisnickoIme, id, kraj, pocetak);
  }

  private int izmena(String sql, Object... parametri) {
    try (Connection veza = izvorPodataka.getConnection();
         PreparedStatement upit = veza.prepareStatement(sql)) {
      postavi(upit, parametri);
      return upit.executeUpdate();
    } catch (SQLException e) {
      throw new IllegalStateException("Promena rezervacije nije sačuvana", e);
    }
  }

  private long broj(String sql, Object... parametri) {
    try (Connection veza = izvorPodataka.getConnection();
         PreparedStatement upit = veza.prepareStatement(sql)) {
      postavi(upit, parametri);
      ResultSet rezultat = upit.executeQuery();
      rezultat.next();
      return rezultat.getLong(1);
    } catch (SQLException e) {
      throw new IllegalStateException("Podaci rezervacije nisu učitani", e);
    }
  }

  private <T> Optional<T> jedan(String sql, MapiranjeReda<T> mapiranje, Object... parametri) {
    List<T> rezultat = lista(sql, mapiranje, parametri);
    return rezultat.stream().findFirst();
  }

  private <T> List<T> lista(String sql, MapiranjeReda<T> mapiranje, Object... parametri) {
    try (Connection veza = izvorPodataka.getConnection();
         PreparedStatement upit = veza.prepareStatement(sql)) {
      postavi(upit, parametri);
      ResultSet rezultat = upit.executeQuery();
      List<T> lista = new ArrayList<>();
      while (rezultat.next()) lista.add(mapiranje.mapiraj(rezultat));
      return lista;
    } catch (SQLException e) {
      throw new IllegalStateException("Rezervacije nisu učitane", e);
    }
  }

  @FunctionalInterface
  private interface MapiranjeReda<T> {
    T mapiraj(ResultSet rezultat) throws SQLException;
  }

  private record RezervacijaRed(Long id, String objekat, String grad, String resurs, String sport,
      LocalDateTime pocetak, LocalDateTime kraj, String status) implements RezervacijaProjekcija {
    RezervacijaRed(ResultSet r) throws SQLException {
      this(r.getLong("id"), r.getString("objekat"), r.getString("city"), r.getString("resurs"),
          r.getString("sport"), r.getTimestamp("pocetak").toLocalDateTime(),
          r.getTimestamp("kraj").toLocalDateTime(), r.getString("status"));
    }
    public Long getId() { return id; }
    public String getObjekat() { return objekat; }
    public String getGrad() { return grad; }
    public String getResurs() { return resurs; }
    public String getSport() { return sport; }
    public LocalDateTime getPocetak() { return pocetak; }
    public LocalDateTime getKraj() { return kraj; }
    public String getStatus() { return status; }
  }

  private record ResursRezervacijaRed(Long id, java.time.LocalTime otvaranje,
      java.time.LocalTime zatvaranje) implements ResursRezervacijaProjekcija {
    ResursRezervacijaRed(ResultSet r) throws SQLException {
      this(r.getLong("id"), r.getTime("otvaranje").toLocalTime(),
          r.getTime("zatvaranje").toLocalTime());
    }
    public Long getId() { return id; }
    public java.time.LocalTime getOtvaranje() { return otvaranje; }
    public java.time.LocalTime getZatvaranje() { return zatvaranje; }
  }

  private record TerminRed(LocalDateTime pocetak, LocalDateTime kraj) implements TerminProjekcija {
    TerminRed(ResultSet r) throws SQLException {
      this(r.getTimestamp("pocetak").toLocalDateTime(), r.getTimestamp("kraj").toLocalDateTime());
    }
    public LocalDateTime getPocetak() { return pocetak; }
    public LocalDateTime getKraj() { return kraj; }
  }

  private record ZaposleniRezervacijaRed(Long id, String sportista, String objekat, String resurs,
      String sport, String tip, LocalDateTime pocetak, LocalDateTime kraj, String status,
      Long mozeEvidencija) implements ZaposleniRezervacijaProjekcija {
    ZaposleniRezervacijaRed(ResultSet r) throws SQLException {
      this(r.getLong("id"), r.getString("sportista"), r.getString("objekat"),
          r.getString("resurs"), r.getString("sport"), r.getString("type"),
          r.getTimestamp("pocetak").toLocalDateTime(), r.getTimestamp("kraj").toLocalDateTime(),
          r.getString("status"), r.getLong("mozeEvidencija"));
    }
    public Long getId() { return id; }
    public String getSportista() { return sportista; }
    public String getObjekat() { return objekat; }
    public String getResurs() { return resurs; }
    public String getSport() { return sport; }
    public String getTip() { return tip; }
    public LocalDateTime getPocetak() { return pocetak; }
    public LocalDateTime getKraj() { return kraj; }
    public String getStatus() { return status; }
    public Long getMozeEvidencija() { return mozeEvidencija; }
  }

  private record NedolazakRed(Long identifikatorObjekta, Long identifikatorSportiste, Integer dozvoljeno)
      implements NedolazakProjekcija {
    NedolazakRed(ResultSet r) throws SQLException {
      this(r.getLong("identifikatorObjekta"), r.getLong("identifikatorSportiste"), r.getInt("dozvoljeno"));
    }
    public Long getIdentifikatorObjekta() { return identifikatorObjekta; }
    public Long getIdentifikatorSportiste() { return identifikatorSportiste; }
    public Integer getDozvoljeno() { return dozvoljeno; }
  }

  private record SportStatistikaRed(String sport, Long odigrano, Long rezervisano)
      implements SportStatistikaProjekcija {
    SportStatistikaRed(ResultSet r) throws SQLException {
      this(r.getString("sport"), r.getLong("odigrano"), r.getLong("rezervisano"));
    }
    public String getSport() { return sport; }
    public Long getOdigrano() { return odigrano; }
    public Long getRezervisano() { return rezervisano; }
  }

  private record MesecStatistikaRed(String mesec, Long broj)
      implements MesecStatistikaProjekcija {
    MesecStatistikaRed(ResultSet r) throws SQLException {
      this(r.getString("mesec"), r.getLong("broj"));
    }
    public String getMesec() { return mesec; }
    public Long getBroj() { return broj; }
  }

  private record PomeranjeRezervacijeRed(Long identifikatorResursa, String tip,
      java.time.LocalTime otvaranje, java.time.LocalTime zatvaranje, Long trajanjeMinuta,
      LocalDateTime pocetak) implements PomeranjeRezervacijeProjekcija {
    PomeranjeRezervacijeRed(ResultSet r) throws SQLException {
      this(r.getLong("identifikatorResursa"), r.getString("type"), r.getTime("otvaranje").toLocalTime(),
          r.getTime("zatvaranje").toLocalTime(), r.getLong("trajanjeMinuta"),
          r.getTimestamp("pocetak").toLocalDateTime());
    }
    public Long getIdentifikatorResursa() { return identifikatorResursa; }
    public String getTip() { return tip; }
    public java.time.LocalTime getOtvaranje() { return otvaranje; }
    public java.time.LocalTime getZatvaranje() { return zatvaranje; }
    public Long getTrajanjeMinuta() { return trajanjeMinuta; }
    public LocalDateTime getPocetak() { return pocetak; }
  }

  private void postavi(PreparedStatement upit, Object... parametri) throws SQLException {
    for (int i = 0; i < parametri.length; i++) {
      Object vrednost = parametri[i];
      if (vrednost instanceof LocalDateTime vreme)
        upit.setTimestamp(i + 1, Timestamp.valueOf(vreme));
      else
        upit.setObject(i + 1, vrednost);
    }
  }
}
