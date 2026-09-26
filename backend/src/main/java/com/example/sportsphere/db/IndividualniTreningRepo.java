package com.example.sportsphere.db;

import com.example.sportsphere.models.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class IndividualniTreningRepo {
  private final DataSource izvorPodataka;
  private final KorisnikRepo korisnikRepozitorijum;
  private final TrenerRepo trenerRepozitorijum;

  public IndividualniTreningRepo(
      DataSource izvorPodataka, KorisnikRepo korisnikRepozitorijum, TrenerRepo trenerRepozitorijum) {
    this.izvorPodataka = izvorPodataka;
    this.korisnikRepozitorijum = korisnikRepozitorijum;
    this.trenerRepozitorijum = trenerRepozitorijum;
  }
  public List<IndividualniTrening> pronadjiPoKorisnickomImenuSportisteSortiranoPoPocetkuOpadajuce(String korisnickoIme) {
    String sql = "SELECT it.id,it.athlete_id,it.trainer_id,it.starts_at,it.ends_at,it.status "
        + "FROM individual_trainings it JOIN users u ON u.id=it.athlete_id "
        + "WHERE u.username=? ORDER BY it.starts_at DESC";
    try (Connection veza = izvorPodataka.getConnection();
         PreparedStatement upit = veza.prepareStatement(sql)) {
      upit.setString(1, korisnickoIme);
      ResultSet rezultat = upit.executeQuery();
      List<IndividualniTrening> lista = new ArrayList<>();
      while (rezultat.next()) lista.add(mapiraj(rezultat));
      return lista;
    } catch (SQLException e) {
      throw greska("Treninzi nisu učitani", e);
    }
  }
  public boolean postojiPreklapanjeTrenera(
      Long trenerId, StatusTreninga status, LocalDateTime kraj, LocalDateTime pocetak) {
    return postojiPreklapanje("trainer_id", trenerId, status, kraj, pocetak);
  }
  public boolean postojiPreklapanjeSportiste(
      Long identifikatorSportiste, StatusTreninga status, LocalDateTime kraj, LocalDateTime pocetak) {
    return postojiPreklapanje("athlete_id", identifikatorSportiste, status, kraj, pocetak);
  }
  public List<ZaposleniTreningProjekcija> dohvatiZaZaposlenog(String korisnickoIme) {
    LocalDateTime sada = LocalDateTime.now(java.time.ZoneId.of("Europe/Belgrade"));
    String sql =
        "SELECT it.id,CONCAT(a.first_name,' ',a.last_name) sportista,CONCAT(t.first_name,' ',t.last_name) trener,"
        + "f.name objekat,s.name sport,it.starts_at pocetak,it.ends_at kraj,it.status,t.hourly_price price,"
        + "(? <=DATE_ADD(it.starts_at,INTERVAL 10 MINUTE) AND it.status='SCHEDULED') moze "
        + "FROM individual_trainings it JOIN users a ON a.id=it.athlete_id JOIN trainers t ON t.id=it.trainer_id "
        + "JOIN facilities f ON f.id=t.facility_id JOIN sports s ON s.id=t.sport_id "
        + "JOIN organization_employees oe ON oe.organization_id=f.organization_id JOIN users e ON e.id=oe.employee_id "
        + "WHERE e.username=? ORDER BY it.starts_at DESC";
    try (Connection veza = izvorPodataka.getConnection();
         PreparedStatement upit = veza.prepareStatement(sql)) {
      upit.setTimestamp(1, Timestamp.valueOf(sada));
      upit.setString(2, korisnickoIme);
      ResultSet r = upit.executeQuery();
      List<ZaposleniTreningProjekcija> lista = new ArrayList<>();
      while (r.next()) lista.add(new ZaposleniTreningRed(r));
      return lista;
    } catch (SQLException e) {
      throw greska("Treninzi zaposlenog nisu učitani", e);
    }
  }
  public int evidentiraj(String korisnickoIme, Long id, String status) {
    LocalDateTime sada = LocalDateTime.now(java.time.ZoneId.of("Europe/Belgrade"));
    String sql =
        "UPDATE individual_trainings it JOIN trainers t ON t.id=it.trainer_id JOIN facilities f ON f.id=t.facility_id "
        + "JOIN organization_employees oe ON oe.organization_id=f.organization_id JOIN users e ON e.id=oe.employee_id "
        + "SET it.status=? WHERE it.id=? AND e.username=? AND it.status='SCHEDULED' "
        + "AND ?<=DATE_ADD(it.starts_at,INTERVAL 10 MINUTE)";
    try (Connection v = izvorPodataka.getConnection(); PreparedStatement u = v.prepareStatement(sql)) {
      u.setString(1, status);
      u.setLong(2, id);
      u.setString(3, korisnickoIme);
      u.setTimestamp(4, Timestamp.valueOf(sada));
      return u.executeUpdate();
    } catch (SQLException e) {
      throw greska("Evidencija nije sačuvana", e);
    }
  }
  public Optional<NedolazakProjekcija> podaciNedolaska(Long id) {
    String sql = "SELECT f.id objekat_id,it.athlete_id sportista_id,f.allowed_no_shows dozvoljeno "
        + "FROM individual_trainings it JOIN trainers t ON t.id=it.trainer_id JOIN facilities f ON f.id=t.facility_id WHERE it.id=?";
    try (Connection v = izvorPodataka.getConnection(); PreparedStatement u = v.prepareStatement(sql)) {
      u.setLong(1, id);
      ResultSet r = u.executeQuery();
      return r.next() ? Optional.of(new NedolazakRed(r)) : Optional.empty();
    } catch (SQLException e) {
      throw greska("Podaci o nedolasku nisu učitani", e);
    }
  }
  public long ukupanBrojNedolazaka(Long identifikatorSportiste, Long identifikatorObjekta) {
    String sql =
        "SELECT (SELECT COUNT(*) FROM reservations rv JOIN resources r ON r.id=rv.resource_id "
        + "WHERE rv.athlete_id=? AND r.facility_id=? AND rv.status='NO_SHOW')+(SELECT COUNT(*) FROM individual_trainings it "
        + "JOIN trainers t ON t.id=it.trainer_id WHERE it.athlete_id=? AND t.facility_id=? AND it.status='NO_SHOW')";
    try (Connection v = izvorPodataka.getConnection(); PreparedStatement u = v.prepareStatement(sql)) {
      u.setLong(1, identifikatorSportiste);
      u.setLong(2, identifikatorObjekta);
      u.setLong(3, identifikatorSportiste);
      u.setLong(4, identifikatorObjekta);
      ResultSet r = u.executeQuery();
      r.next();
      return r.getLong(1);
    } catch (SQLException e) {
      throw greska("Nedolasci nisu izbrojani", e);
    }
  }
  public IndividualniTrening sacuvaj(IndividualniTrening t) {
    boolean novi = t.getId() == null;
    String sql = novi
        ? "INSERT INTO individual_trainings(athlete_id,trainer_id,starts_at,ends_at,status) VALUES(?,?,?,?,?)"
        : "UPDATE individual_trainings SET athlete_id=?,trainer_id=?,starts_at=?,ends_at=?,status=? WHERE id=?";
    try (Connection v = izvorPodataka.getConnection();
         PreparedStatement u = v.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      u.setLong(1, t.getSportista().getId());
      u.setLong(2, t.getTrener().getId());
      u.setTimestamp(3, Timestamp.valueOf(t.getPocetak()));
      u.setTimestamp(4, Timestamp.valueOf(t.getKraj()));
      u.setString(5, t.getStatus().name());
      if (!novi)
        u.setLong(6, t.getId());
      u.executeUpdate();
      if (novi) {
        ResultSet r = u.getGeneratedKeys();
        if (r.next())
          t.setId(r.getLong(1));
      }
      return t;
    } catch (SQLException e) {
      throw greska("Trening nije sačuvan", e);
    }
  }

  private boolean postojiPreklapanje(
      String kolona, Long id, StatusTreninga status, LocalDateTime kraj, LocalDateTime pocetak) {
    String sql = "SELECT COUNT(*) FROM individual_trainings WHERE " + kolona
        + "=? AND status=? AND starts_at<? AND ends_at>?";
    try (Connection v = izvorPodataka.getConnection(); PreparedStatement u = v.prepareStatement(sql)) {
      u.setLong(1, id);
      u.setString(2, status.name());
      u.setTimestamp(3, Timestamp.valueOf(kraj));
      u.setTimestamp(4, Timestamp.valueOf(pocetak));
      ResultSet r = u.executeQuery();
      r.next();
      return r.getLong(1) > 0;
    } catch (SQLException e) {
      throw greska("Termin nije proveren", e);
    }
  }

  private IndividualniTrening mapiraj(ResultSet r) throws SQLException {
    IndividualniTrening t = new IndividualniTrening();
    t.setId(r.getLong("id"));
    t.setSportista(korisnikRepozitorijum.pronadjiPoId(r.getLong("athlete_id")).orElseThrow());
    t.setTrener(trenerRepozitorijum.pronadjiPoId(r.getLong("trainer_id")).orElseThrow());
    t.setPocetak(r.getTimestamp("starts_at").toLocalDateTime());
    t.setKraj(r.getTimestamp("ends_at").toLocalDateTime());
    t.setStatus(StatusTreninga.valueOf(r.getString("status")));
    return t;
  }

  private IllegalStateException greska(String poruka, SQLException e) {
    return new IllegalStateException(poruka, e);
  }

  private static class NedolazakRed implements NedolazakProjekcija {
    private final Long identifikatorObjekta, identifikatorSportiste;
    private final Integer dozvoljeno;
    NedolazakRed(ResultSet r) throws SQLException {
      identifikatorObjekta = r.getLong("objekat_id");
      identifikatorSportiste = r.getLong("sportista_id");
      dozvoljeno = r.getInt("dozvoljeno");
    }
    public Long getIdentifikatorObjekta() {
      return identifikatorObjekta;
    }
    public Long getIdentifikatorSportiste() {
      return identifikatorSportiste;
    }
    public Integer getDozvoljeno() {
      return dozvoljeno;
    }
  }

  private static class ZaposleniTreningRed implements ZaposleniTreningProjekcija {
    private final Long id;
    private final String sportista, trener, objekat, sport, status;
    private final LocalDateTime pocetak, kraj;
    private final BigDecimal cena;
    private final Integer moze;
    ZaposleniTreningRed(ResultSet r) throws SQLException {
      id = r.getLong("id");
      sportista = r.getString("sportista");
      trener = r.getString("trener");
      objekat = r.getString("objekat");
      sport = r.getString("sport");
      status = r.getString("status");
      pocetak = r.getTimestamp("pocetak").toLocalDateTime();
      kraj = r.getTimestamp("kraj").toLocalDateTime();
      cena = r.getBigDecimal("price");
      moze = r.getInt("moze");
    }
    public Long getId() {
      return id;
    }
    public String getSportista() {
      return sportista;
    }
    public String getTrener() {
      return trener;
    }
    public String getObjekat() {
      return objekat;
    }
    public String getSport() {
      return sport;
    }
    public LocalDateTime getPocetak() {
      return pocetak;
    }
    public LocalDateTime getKraj() {
      return kraj;
    }
    public String getStatus() {
      return status;
    }
    public BigDecimal getCena() {
      return cena;
    }
    public Integer getMozeEvidencija() {
      return moze;
    }
  }
}
