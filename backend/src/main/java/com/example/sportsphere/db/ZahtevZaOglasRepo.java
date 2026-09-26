package com.example.sportsphere.db;
import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;
import com.example.sportsphere.models.*;
@Repository
public class ZahtevZaOglasRepo {
  private final DataSource izvorPodataka;
  private final OglasRepo oglasi;
  private final KorisnikRepo korisnici;
  public ZahtevZaOglasRepo(DataSource s, OglasRepo o, KorisnikRepo k) {
    izvorPodataka = s;
    oglasi = o;
    korisnici = k;
  }
  public List<ZahtevZaOglas> pronadjiPoOglasuSortiranoPoId(Long id) {
    return lista("ad_id=? ORDER BY id", id);
  }
  public Optional<ZahtevZaOglas> pronadjiPoId(Long id) {
    return lista("id=?", id).stream().findFirst();
  }
  public boolean postojiZaOglasISportistu(Long a, Long k) {
    return broj("SELECT COUNT(*) FROM teammate_join_requests WHERE ad_id=? AND athlete_id=?", a, k)
        > 0;
  }
  public long countByAdIdAndStatus(Long a, StatusZahteva s) {
    return broj(
        "SELECT COUNT(*) FROM teammate_join_requests WHERE ad_id=? AND status=?", a, s.name());
  }
  public ZahtevZaOglas sacuvaj(ZahtevZaOglas z) {
    boolean novi = z.getId() == null;
    String sql = novi
        ? "INSERT INTO teammate_join_requests(ad_id,athlete_id,status)VALUES(?,?,?)"
        : "UPDATE teammate_join_requests SET ad_id=?,athlete_id=?,status=? WHERE id=?";
    try (Connection v = izvorPodataka.getConnection();
         PreparedStatement u = v.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      u.setLong(1, z.getOglas().getId());
      u.setLong(2, z.getSportista().getId());
      u.setString(3, z.getStatus().name());
      if (!novi)
        u.setLong(4, z.getId());
      u.executeUpdate();
      if (novi) {
        ResultSet r = u.getGeneratedKeys();
        if (r.next())
          z.setId(r.getLong(1));
      }
      return z;
    } catch (SQLException e) {
      throw new IllegalStateException("Zahtev nije sačuvan", e);
    }
  }
  private List<ZahtevZaOglas> lista(String uslov, Object p) {
    try (Connection v = izvorPodataka.getConnection();
         PreparedStatement u =
             v.prepareStatement("SELECT * FROM teammate_join_requests WHERE " + uslov)) {
      u.setObject(1, p);
      ResultSet r = u.executeQuery();
      List<ZahtevZaOglas> l = new ArrayList<>();
      while (r.next()) {
        ZahtevZaOglas z = new ZahtevZaOglas();
        z.setId(r.getLong("id"));
        z.setOglas(oglasi.pronadjiPoId(r.getLong("ad_id")).orElseThrow());
        z.setSportista(korisnici.pronadjiPoId(r.getLong("athlete_id")).orElseThrow());
        z.setStatus(StatusZahteva.valueOf(r.getString("status")));
        l.add(z);
      }
      return l;
    } catch (SQLException e) {
      throw new IllegalStateException("Zahtevi nisu učitani", e);
    }
  }
  private long broj(String sql, Object... p) {
    try (Connection v = izvorPodataka.getConnection(); PreparedStatement u = v.prepareStatement(sql)) {
      for (int i = 0; i < p.length; i++) u.setObject(i + 1, p[i]);
      ResultSet r = u.executeQuery();
      r.next();
      return r.getLong(1);
    } catch (SQLException e) {
      throw new IllegalStateException("Zahtev nije proveren", e);
    }
  }
}
