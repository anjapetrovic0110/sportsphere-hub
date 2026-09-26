package com.example.sportsphere.db;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;
import com.example.sportsphere.models.*;
@Repository
public class OpremaRepo {
  private final DataSource izvorPodataka;
  private final OrganizacijaRepo organizacije;
  private final SportRepo sportovi;
  public OpremaRepo(DataSource s, OrganizacijaRepo o, SportRepo sp) {
    izvorPodataka = s;
    organizacije = o;
    sportovi = sp;
  }
  public List<Oprema> pronadjiPoOrganizacijiSortiranoPoNazivu(Long id) {
    return lista("WHERE organization_id=? ORDER BY name", List.of(id));
  }
  public boolean postojiUOrganizacijiSaNazivomBezObziraNaVelicinuSlova(Long id, String n) {
    return broj("SELECT COUNT(*) FROM equipment WHERE organization_id=? AND LOWER(name)=LOWER(?)",
               id, n)
        > 0;
  }
  public boolean postojiUOrganizacijiSaNazivomIDrugacijimIdBezObziraNaVelicinuSlova(Long id, String n, Long oid) {
    return broj(
               "SELECT COUNT(*) FROM equipment WHERE organization_id=? AND LOWER(name)=LOWER(?) AND id<>?",
               id, n, oid)
        > 0;
  }
  public List<Oprema> pronadjiAktivneSortiranoPoNazivu() {
    return lista("WHERE active=TRUE ORDER BY name", List.of());
  }
  public Optional<Oprema> pronadjiPoId(Long id) {
    return lista("WHERE id=?", List.of(id)).stream().findFirst();
  }
  public List<Oprema> zakljucajSve(List<Long> ids) {
    if (ids.isEmpty())
      return List.of();
    return lista("WHERE id IN(" + String.join(",", Collections.nCopies(ids.size(), "?"))
            + ") ORDER BY id FOR UPDATE",
        new ArrayList<>(ids));
  }
  public Oprema sacuvaj(Oprema o) {
    boolean novi = o.getId() == null;
    String q = novi
        ? "INSERT INTO equipment(organization_id,sport_id,name,image_path,price,stock,active)VALUES(?,?,?,?,?,?,?)"
        : "UPDATE equipment SET organization_id=?,sport_id=?,name=?,image_path=?,price=?,stock=?,active=? WHERE id=?";
    try (Connection v = izvorPodataka.getConnection();
         PreparedStatement u = v.prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
      u.setLong(1, o.getOrganizacija().getId());
      u.setLong(2, o.getSport().getId());
      u.setString(3, o.getNaziv());
      u.setString(4, o.getPutanjaSlike());
      u.setBigDecimal(5, o.getCena());
      u.setInt(6, o.getZaliha());
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
      throw new IllegalStateException("Oprema nije sačuvana", e);
    }
  }
  private List<Oprema> lista(String dodatak, List<?> p) {
    try (Connection v = izvorPodataka.getConnection();
         PreparedStatement u = v.prepareStatement("SELECT * FROM equipment " + dodatak)) {
      for (int i = 0; i < p.size(); i++) u.setObject(i + 1, p.get(i));
      ResultSet r = u.executeQuery();
      List<Oprema> l = new ArrayList<>();
      while (r.next()) {
        Oprema o = new Oprema();
        o.setId(r.getLong("id"));
        o.setOrganizacija(organizacije.pronadjiPoId(r.getLong("organization_id")).orElseThrow());
        o.setSport(sportovi.pronadjiPoId(r.getLong("sport_id")).orElseThrow());
        o.setNaziv(r.getString("name"));
        o.setPutanjaSlike(r.getString("image_path"));
        o.setCena(r.getBigDecimal("price"));
        o.setZaliha(r.getInt("stock"));
        o.setAktivan(r.getBoolean("active"));
        l.add(o);
      }
      return l;
    } catch (SQLException e) {
      throw new IllegalStateException("Oprema nije učitana", e);
    }
  }
  private long broj(String q, Object... p) {
    try (Connection v = izvorPodataka.getConnection(); PreparedStatement u = v.prepareStatement(q)) {
      for (int i = 0; i < p.length; i++) u.setObject(i + 1, p[i]);
      ResultSet r = u.executeQuery();
      r.next();
      return r.getLong(1);
    } catch (SQLException e) {
      throw new IllegalStateException("Oprema nije proverena", e);
    }
  }
}
