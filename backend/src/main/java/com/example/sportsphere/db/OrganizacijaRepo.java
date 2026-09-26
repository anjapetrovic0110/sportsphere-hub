package com.example.sportsphere.db;

import com.example.sportsphere.models.Korisnik;
import com.example.sportsphere.models.Organizacija;
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
public class OrganizacijaRepo {
  private final DataSource izvorPodataka;
  private final KorisnikRepo korisnikRepozitorijum;

  public OrganizacijaRepo(DataSource izvorPodataka, KorisnikRepo korisnikRepozitorijum) {
    this.izvorPodataka = izvorPodataka;
    this.korisnikRepozitorijum = korisnikRepozitorijum;
  }
  public Optional<Organizacija> pronadjiPoId(Long id) {
    return pronadji("o.id=?", id);
  }
  public Optional<Organizacija> pronadjiPoMaticnomBroju(String maticniBroj) {
    return pronadji("o.registration_number=?", maticniBroj);
  }
  public Optional<Organizacija> pronadjiPoPoreskomBroju(String poreskiBroj) {
    return pronadji("o.tax_id=?", poreskiBroj);
  }
  public Optional<Organizacija> pronadjiPoKorisnickomImenuZaposlenog(String korisnickoIme) {
    String uslov =
        "EXISTS(SELECT 1 FROM organization_employees oe JOIN users u ON u.id=oe.employee_id "
        + "WHERE oe.organization_id=o.id AND u.username=?)";
    return pronadji(uslov, korisnickoIme);
  }
  public Organizacija sacuvaj(Organizacija organizacija) {
    try (Connection veza = izvorPodataka.getConnection()) {
      veza.setAutoCommit(false);
      try {
        if (organizacija.getId() == null)
          ubaci(veza, organizacija);
        else
          izmeni(veza, organizacija);
        sinhronizujZaposlene(veza, organizacija);
        veza.commit();
        return organizacija;
      } catch (SQLException e) {
        veza.rollback();
        throw e;
      }
    } catch (SQLException e) {
      throw new IllegalStateException("Organizacija nije sačuvana", e);
    }
  }

  private Optional<Organizacija> pronadji(String uslov, Object vrednost) {
    String sql = "SELECT o.id,o.name,o.registered_address,o.registration_number,o.tax_id "
        + "FROM employee_organizations o WHERE " + uslov;
    try (Connection veza = izvorPodataka.getConnection();
         PreparedStatement upit = veza.prepareStatement(sql)) {
      upit.setObject(1, vrednost);
      ResultSet rezultat = upit.executeQuery();
      if (!rezultat.next())
        return Optional.empty();
      Organizacija organizacija = mapiraj(rezultat);
      organizacija.setZaposleni(dohvatiZaposlene(veza, organizacija.getId()));
      return Optional.of(organizacija);
    } catch (SQLException e) {
      throw new IllegalStateException("Organizacija nije učitana", e);
    }
  }

  private void ubaci(Connection veza, Organizacija organizacija) throws SQLException {
    String sql =
        "INSERT INTO employee_organizations(name,registered_address,registration_number,tax_id) "
        + "VALUES(?,?,?,?)";
    try (PreparedStatement upit = veza.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      postavi(upit, organizacija);
      upit.executeUpdate();
      ResultSet kljucevi = upit.getGeneratedKeys();
      if (kljucevi.next())
        organizacija.setId(kljucevi.getLong(1));
    }
  }

  private void izmeni(Connection veza, Organizacija organizacija) throws SQLException {
    String sql =
        "UPDATE employee_organizations SET name=?,registered_address=?,registration_number=?,tax_id=? "
        + "WHERE id=?";
    try (PreparedStatement upit = veza.prepareStatement(sql)) {
      postavi(upit, organizacija);
      upit.setLong(5, organizacija.getId());
      upit.executeUpdate();
    }
  }

  private void postavi(PreparedStatement upit, Organizacija organizacija) throws SQLException {
    upit.setString(1, organizacija.getNaziv());
    upit.setString(2, organizacija.getRegistrovanaAdresa());
    upit.setString(3, organizacija.getMaticniBroj());
    upit.setString(4, organizacija.getPoreskiBroj());
  }

  private void sinhronizujZaposlene(Connection veza, Organizacija organizacija)
      throws SQLException {
    try (PreparedStatement brisanje =
             veza.prepareStatement("DELETE FROM organization_employees WHERE organization_id=?")) {
      brisanje.setLong(1, organizacija.getId());
      brisanje.executeUpdate();
    }
    try (PreparedStatement dodavanje = veza.prepareStatement(
             "INSERT INTO organization_employees(organization_id,employee_id) VALUES(?,?)")) {
      for (Korisnik zaposleni : organizacija.getZaposleni()) {
        dodavanje.setLong(1, organizacija.getId());
        dodavanje.setLong(2, zaposleni.getId());
        dodavanje.addBatch();
      }
      dodavanje.executeBatch();
    }
  }

  private List<Korisnik> dohvatiZaposlene(Connection veza, Long identifikatorOrganizacije)
      throws SQLException {
    List<Korisnik> zaposleni = new ArrayList<>();
    String sql = "SELECT u.id FROM users u JOIN organization_employees oe ON oe.employee_id=u.id "
        + "WHERE oe.organization_id=? ORDER BY u.id";
    try (PreparedStatement upit = veza.prepareStatement(sql)) {
      upit.setLong(1, identifikatorOrganizacije);
      ResultSet rezultat = upit.executeQuery();
      while (rezultat.next()) {
        korisnikRepozitorijum.pronadjiPoId(rezultat.getLong(1)).ifPresent(zaposleni::add);
      }
    }
    return zaposleni;
  }

  private Organizacija mapiraj(ResultSet rezultat) throws SQLException {
    Organizacija organizacija = new Organizacija();
    organizacija.setId(rezultat.getLong("id"));
    organizacija.setNaziv(rezultat.getString("name"));
    organizacija.setRegistrovanaAdresa(rezultat.getString("registered_address"));
    organizacija.setMaticniBroj(rezultat.getString("registration_number"));
    organizacija.setPoreskiBroj(rezultat.getString("tax_id"));
    return organizacija;
  }
}
