package com.example.sportsphere.db;
import java.sql.*;
import java.time.*;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;
import com.example.sportsphere.models.*;
@Repository
public class ObjekatRepo {
  private final DataSource izvorPodataka;
  private final OrganizacijaRepo organizacije;
  private final SportRepo sportovi;
  public ObjekatRepo(DataSource s, OrganizacijaRepo o, SportRepo sp) {
    izvorPodataka = s;
    organizacije = o;
    sportovi = sp;
  }
  public long countByStatus(StatusObjekta s) {
    return broj("SELECT COUNT(*) FROM facilities WHERE status=?", s.name());
  }
  public boolean postojiUOrganizacijiSaNazivom(Long o, String n) {
    return broj("SELECT COUNT(*) FROM facilities WHERE organization_id=? AND name=?", o, n) > 0;
  }
  public boolean postojiUOrganizacijiSaNazivomIDrugacijimId(Long o, String n, Long id) {
    return broj("SELECT COUNT(*) FROM facilities WHERE organization_id=? AND name=? AND id<>?", o,
               n, id)
        > 0;
  }
  public Optional<Objekat> pronadjiPoId(Long id) {
    return objekti("WHERE f.id=?", id).stream().findFirst();
  }
  public Optional<Objekat> pronadjiPoIdIOrganizaciji(Long id, Long o) {
    return objekti("WHERE f.id=? AND f.organization_id=?", id, o).stream().findFirst();
  }
  public List<Objekat> pronadjiPoStatusuSortiranoPoNazivu(StatusObjekta s) {
    return objekti("WHERE f.status=? ORDER BY f.name", s.name());
  }
  public Optional<Objekat> pronadjiPoIdIStatusu(Long id, StatusObjekta s) {
    return objekti("WHERE f.id=? AND f.status=?", id, s.name()).stream().findFirst();
  }
  public Optional<Objekat> zakljucajAktivan(Long id, StatusObjekta s) {
    return objekti("WHERE f.id=? AND f.status=? FOR UPDATE", id, s.name()).stream().findFirst();
  }
  public List<TopObjekatProjekcija> dohvatiTopTri() {
    return projekcije(
        "SELECT f.id,f.name naziv,f.city city,COUNT(r.id) brojLajkova FROM facilities f LEFT JOIN facility_reactions r ON r.facility_id=f.id AND r.reaction='LIKE' WHERE f.status='ACTIVE' GROUP BY f.id,f.name,f.city ORDER BY brojLajkova DESC,f.name LIMIT 3",
        TopObjekatRed::new);
  }
  public List<String> dohvatiGradove() {
    try (Connection v = izvorPodataka.getConnection();
         PreparedStatement u = v.prepareStatement(
             "SELECT DISTINCT city FROM facilities WHERE status='ACTIVE' ORDER BY city");
         ResultSet r = u.executeQuery()) {
      List<String> l = new ArrayList<>();
      while (r.next()) l.add(r.getString(1));
      return l;
    } catch (SQLException e) {
      throw greska(e);
    }
  }
  public List<ObjekatPretragaProjekcija> pretrazi(
      String n, String g, Long sid, String tip, boolean slobodno) {
    String q =
        "SELECT f.id,f.name naziv,f.city city,f.address address,GROUP_CONCAT(DISTINCT s.name ORDER BY s.name SEPARATOR ', ') sports,MIN(r.hourly_price) najmanjaCena FROM facilities f JOIN resources r ON r.facility_id=f.id AND r.active=TRUE JOIN resource_sports rs ON rs.resource_id=r.id JOIN sports s ON s.id=rs.sport_id WHERE f.status='ACTIVE' AND (?='' OR LOWER(f.name) LIKE LOWER(CONCAT('%',?,'%'))) AND (?='' OR FIND_IN_SET(f.city,?)>0) AND (? IS NULL OR s.id=?) AND (?='' OR r.type=?) GROUP BY f.id,f.name,f.city,f.address ORDER BY f.name";
    return projekcije(q, ObjekatPretragaRed::new, n, n, g, g, sid, sid, tip, tip);
  }
  public List<ResursProjekcija> dohvatiResurse(Long id) {
    String q =
        "SELECT r.id,r.name naziv,r.type type,r.capacity capacity,r.equipment_description equipment,r.hourly_price price,GROUP_CONCAT(DISTINCT s.name ORDER BY s.name SEPARATOR ', ') sports FROM resources r JOIN resource_sports rs ON rs.resource_id=r.id JOIN sports s ON s.id=rs.sport_id WHERE r.facility_id=? AND r.active=TRUE GROUP BY r.id,r.name,r.type,r.capacity,r.equipment_description,r.hourly_price ORDER BY r.name";
    return projekcije(q, ResursRed::new, id);
  }
  public long brojReakcija(Long id, String r) {
    return broj(
        "SELECT COUNT(*) FROM facility_reactions WHERE facility_id=? AND reaction=?", id, r);
  }
  public List<String> dohvatiSlike(Long id) {
    try (
        Connection v = izvorPodataka.getConnection();
        PreparedStatement u = v.prepareStatement(
            "SELECT image_path FROM facility_images WHERE facility_id=? ORDER BY display_order,id")) {
      u.setLong(1, id);
      ResultSet r = u.executeQuery();
      List<String> l = new ArrayList<>();
      while (r.next()) l.add(r.getString(1));
      return l;
    } catch (SQLException e) {
      throw greska(e);
    }
  }
  public List<ZaposleniObjekatProjekcija> dohvatiZaZaposlenog(String u) {
    String q =
        "SELECT f.id,f.name naziv,f.city city,f.address address,f.status,GROUP_CONCAT(DISTINCT s.name ORDER BY s.name SEPARATOR ', ') sports,GROUP_CONCAT(DISTINCT CONCAT(r.name,' (',r.type,', ',r.capacity,')') ORDER BY r.name SEPARATOR '; ') resources FROM organization_employees oe JOIN users e ON e.id=oe.employee_id JOIN facilities f ON f.organization_id=oe.organization_id LEFT JOIN resources r ON r.facility_id=f.id LEFT JOIN resource_sports rs ON rs.resource_id=r.id LEFT JOIN sports s ON s.id=rs.sport_id WHERE e.username=? GROUP BY f.id,f.name,f.city,f.address,f.status ORDER BY f.name";
    return projekcije(q, ZaposleniObjekatRed::new, u);
  }
  public long podrzavaSport(Long o, Long s) {
    return broj(
        "SELECT COUNT(*) FROM resources r JOIN resource_sports rs ON rs.resource_id=r.id JOIN sports sp ON sp.id=rs.sport_id WHERE r.facility_id=? AND r.active=TRUE AND sp.active=TRUE AND sp.id=?",
        o, s);
  }
  public List<PopunjenostProjekcija> izvestajPopunjenosti(
      String u, LocalDateTime od, LocalDateTime d) {
    String q =
        "SELECT f.name objekat,r.name resurs,COALESCE(SUM(TIMESTAMPDIFF(MINUTE,rv.starts_at,rv.ends_at))/60,0) rezervisaniSati,TIMESTAMPDIFF(MINUTE,f.opening_time,f.closing_time)*DAY(LAST_DAY(?))/60 dostupniSati FROM organization_employees oe JOIN users e ON e.id=oe.employee_id JOIN facilities f ON f.organization_id=oe.organization_id JOIN resources r ON r.facility_id=f.id LEFT JOIN reservations rv ON rv.resource_id=r.id AND rv.status IN('CONFIRMED','ATTENDED') AND rv.starts_at>=? AND rv.starts_at<? WHERE e.username=? GROUP BY f.id,f.name,f.opening_time,f.closing_time,r.id,r.name ORDER BY f.name,r.name";
    return projekcije(q, PopunjenostRed::new, od, od, d, u);
  }
  public Objekat sacuvaj(Objekat objekat) {
    boolean novi = objekat.getId() == null;
    String sql = novi
        ? "INSERT INTO facilities(organization_id,name,city,address,description,latitude,longitude,opening_time,closing_time,allowed_no_shows,status) VALUES(?,?,?,?,?,?,?,?,?,?,?)"
        : "UPDATE facilities SET organization_id=?,name=?,city=?,address=?,description=?,latitude=?,longitude=?,opening_time=?,closing_time=?,allowed_no_shows=?,status=? WHERE id=?";
    try (Connection veza = izvorPodataka.getConnection()) {
      veza.setAutoCommit(false);
      try {
        try (PreparedStatement upit = veza.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
          upit.setLong(1, objekat.getOrganizacija().getId());
          upit.setString(2, objekat.getNaziv());
          upit.setString(3, objekat.getGrad());
          upit.setString(4, objekat.getAdresa());
          upit.setString(5, objekat.getOpis());
          upit.setBigDecimal(6, objekat.getGeografskaSirina());
          upit.setBigDecimal(7, objekat.getGeografskaDuzina());
          upit.setTime(8, Time.valueOf(objekat.getVremeOtvaranja()));
          upit.setTime(9, Time.valueOf(objekat.getVremeZatvaranja()));
          upit.setInt(10, objekat.getDozvoljeniNedolasci());
          upit.setString(11, objekat.getStatus().name());
          if (!novi)
            upit.setLong(12, objekat.getId());
          upit.executeUpdate();
          if (novi) {
            ResultSet kljucevi = upit.getGeneratedKeys();
            if (kljucevi.next())
              objekat.setId(kljucevi.getLong(1));
          }
        }
        for (Resurs resurs : objekat.getResursi()) sacuvajResurs(veza, objekat, resurs);
        veza.commit();
        return objekat;
      } catch (SQLException e) {
        veza.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Objekat nije sačuvan", e);
    }
  }
  private void sacuvajResurs(Connection v, Objekat o, Resurs r) throws SQLException {
    boolean novi = r.getId() == null;
    String q = novi
        ? "INSERT INTO resources(facility_id,name,type,capacity,equipment_description,hourly_price,active)VALUES(?,?,?,?,?,?,?)"
        : "UPDATE resources SET facility_id=?,name=?,type=?,capacity=?,equipment_description=?,hourly_price=?,active=? WHERE id=?";
    try (PreparedStatement u = v.prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
      u.setLong(1, o.getId());
      u.setString(2, r.getNaziv());
      u.setString(3, r.getTip().name());
      u.setInt(4, r.getKapacitet());
      u.setString(5, r.getOpisOpreme());
      u.setBigDecimal(6, r.getCenaPoSatu());
      u.setBoolean(7, r.jeAktivan());
      if (!novi)
        u.setLong(8, r.getId());
      u.executeUpdate();
      if (novi) {
        ResultSet x = u.getGeneratedKeys();
        if (x.next())
          r.setId(x.getLong(1));
      }
    }
    try (PreparedStatement b =
             v.prepareStatement("DELETE FROM resource_sports WHERE resource_id=?")) {
      b.setLong(1, r.getId());
      b.executeUpdate();
    }
    try (PreparedStatement d =
             v.prepareStatement("INSERT INTO resource_sports(resource_id,sport_id)VALUES(?,?)")) {
      for (Sport s : r.getSportovi()) {
        d.setLong(1, r.getId());
        d.setLong(2, s.getId());
        d.addBatch();
      }
      d.executeBatch();
    }
  }
  private List<Objekat> objekti(String dodatak, Object... p) {
    try (Connection v = izvorPodataka.getConnection();
         PreparedStatement u = v.prepareStatement("SELECT f.* FROM facilities f " + dodatak)) {
      postavi(u, p);
      ResultSet r = u.executeQuery();
      List<Objekat> l = new ArrayList<>();
      while (r.next()) {
        Objekat o = new Objekat();
        o.setId(r.getLong("id"));
        o.setOrganizacija(organizacije.pronadjiPoId(r.getLong("organization_id")).orElseThrow());
        o.setNaziv(r.getString("name"));
        o.setGrad(r.getString("city"));
        o.setAdresa(r.getString("address"));
        o.setOpis(r.getString("description"));
        o.setGeografskaSirina(r.getBigDecimal("latitude"));
        o.setGeografskaDuzina(r.getBigDecimal("longitude"));
        o.setVremeOtvaranja(r.getTime("opening_time").toLocalTime());
        o.setVremeZatvaranja(r.getTime("closing_time").toLocalTime());
        o.setDozvoljeniNedolasci(r.getInt("allowed_no_shows"));
        o.setStatus(StatusObjekta.valueOf(r.getString("status")));
        o.setResursi(resursi(v, o));
        l.add(o);
      }
      return l;
    } catch (SQLException e) {
      throw greska(e);
    }
  }
  private List<Resurs> resursi(Connection v, Objekat o) throws SQLException {
    try (PreparedStatement u =
             v.prepareStatement("SELECT * FROM resources WHERE facility_id=? ORDER BY id")) {
      u.setLong(1, o.getId());
      ResultSet x = u.executeQuery();
      List<Resurs> l = new ArrayList<>();
      while (x.next()) {
        Resurs r = new Resurs();
        r.setId(x.getLong("id"));
        r.setObjekat(o);
        r.setNaziv(x.getString("name"));
        r.setTip(TipResursa.valueOf(x.getString("type")));
        r.setKapacitet(x.getInt("capacity"));
        r.setOpisOpreme(x.getString("equipment_description"));
        r.setCenaPoSatu(x.getBigDecimal("hourly_price"));
        r.setAktivan(x.getBoolean("active"));
        r.setSportovi(sportoviResursa(v, r.getId()));
        l.add(r);
      }
      return l;
    }
  }
  private List<Sport> sportoviResursa(Connection v, Long id) throws SQLException {
    try (
        PreparedStatement u = v.prepareStatement(
            "SELECT s.id,s.name,s.active FROM sports s JOIN resource_sports rs ON rs.sport_id=s.id WHERE rs.resource_id=?")) {
      u.setLong(1, id);
      ResultSet x = u.executeQuery();
      List<Sport> l = new ArrayList<>();
      while (x.next()) {
        Sport s = new Sport();
        s.setId(x.getLong(1));
        s.setNaziv(x.getString(2));
        s.setAktivan(x.getBoolean(3));
        l.add(s);
      }
      return l;
    }
  }
  private long broj(String q, Object... p) {
    try (Connection v = izvorPodataka.getConnection(); PreparedStatement u = v.prepareStatement(q)) {
      postavi(u, p);
      ResultSet r = u.executeQuery();
      r.next();
      return r.getLong(1);
    } catch (SQLException e) {
      throw greska(e);
    }
  }
  private <T> List<T> projekcije(String q, MapiranjeReda<T> mapiranje, Object... p) {
    try (Connection v = izvorPodataka.getConnection(); PreparedStatement u = v.prepareStatement(q)) {
      postavi(u, p);
      ResultSet r = u.executeQuery();
      List<T> l = new ArrayList<>();
      while (r.next()) l.add(mapiranje.mapiraj(r));
      return l;
    } catch (SQLException e) {
      throw greska(e);
    }
  }
  @FunctionalInterface
  private interface MapiranjeReda<T> {
    T mapiraj(ResultSet rezultat) throws SQLException;
  }
  private record TopObjekatRed(Long id, String naziv, String grad, Long brojLajkova)
      implements TopObjekatProjekcija {
    TopObjekatRed(ResultSet r) throws SQLException {
      this(r.getLong("id"), r.getString("naziv"), r.getString("city"), r.getLong("brojLajkova"));
    }
    public Long getId() { return id; }
    public String getNaziv() { return naziv; }
    public String getGrad() { return grad; }
    public Long getBrojLajkova() { return brojLajkova; }
  }
  private record ObjekatPretragaRed(Long id, String naziv, String grad, String adresa,
      String sportovi, java.math.BigDecimal najmanjaCena) implements ObjekatPretragaProjekcija {
    ObjekatPretragaRed(ResultSet r) throws SQLException {
      this(r.getLong("id"), r.getString("naziv"), r.getString("city"), r.getString("address"),
          r.getString("sports"), r.getBigDecimal("najmanjaCena"));
    }
    public Long getId() { return id; }
    public String getNaziv() { return naziv; }
    public String getGrad() { return grad; }
    public String getAdresa() { return adresa; }
    public String getSportovi() { return sportovi; }
    public java.math.BigDecimal getNajmanjaCena() { return najmanjaCena; }
  }
  private record ResursRed(Long id, String naziv, String tip, Integer kapacitet, String oprema,
      java.math.BigDecimal cena, String sportovi) implements ResursProjekcija {
    ResursRed(ResultSet r) throws SQLException {
      this(r.getLong("id"), r.getString("naziv"), r.getString("type"), r.getInt("capacity"),
          r.getString("equipment"), r.getBigDecimal("price"), r.getString("sports"));
    }
    public Long getId() { return id; }
    public String getNaziv() { return naziv; }
    public String getTip() { return tip; }
    public Integer getKapacitet() { return kapacitet; }
    public String getOprema() { return oprema; }
    public java.math.BigDecimal getCena() { return cena; }
    public String getSportovi() { return sportovi; }
  }
  private record ZaposleniObjekatRed(Long id, String naziv, String grad, String adresa,
      String status, String sportovi, String resursi) implements ZaposleniObjekatProjekcija {
    ZaposleniObjekatRed(ResultSet r) throws SQLException {
      this(r.getLong("id"), r.getString("naziv"), r.getString("city"), r.getString("address"),
          r.getString("status"), r.getString("sports"), r.getString("resources"));
    }
    public Long getId() { return id; }
    public String getNaziv() { return naziv; }
    public String getGrad() { return grad; }
    public String getAdresa() { return adresa; }
    public String getStatus() { return status; }
    public String getSportovi() { return sportovi; }
    public String getResursi() { return resursi; }
  }
  private record PopunjenostRed(String objekat, String resurs, Long rezervisaniSati,
      Long dostupniSati) implements PopunjenostProjekcija {
    PopunjenostRed(ResultSet r) throws SQLException {
      this(r.getString("objekat"), r.getString("resurs"), r.getLong("rezervisaniSati"),
          r.getLong("dostupniSati"));
    }
    public String getObjekat() { return objekat; }
    public String getResurs() { return resurs; }
    public Long getRezervisaniSati() { return rezervisaniSati; }
    public Long getDostupniSati() { return dostupniSati; }
  }
  private void postavi(PreparedStatement u, Object... p) throws SQLException {
    for (int i = 0; i < p.length; i++) {
      if (p[i] instanceof LocalDateTime t)
        u.setTimestamp(i + 1, Timestamp.valueOf(t));
      else
        u.setObject(i + 1, p[i]);
    }
  }
  private IllegalStateException greska(SQLException e) {
    return new IllegalStateException("Podaci objekta nisu učitani", e);
  }
}
