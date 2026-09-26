package com.example.sportsphere.services;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.sportsphere.db.*;
@Service
public class IzvestajServis {
  private final ObjekatRepo objekatRepozitorijum;
  private final PorudzbinaRepo porudzbinaRepozitorijum;
  private final OrganizacijaRepo organizacijaRepozitorijum;
  public IzvestajServis(
      ObjekatRepo o, PorudzbinaRepo p, OrganizacijaRepo org) {
    objekatRepozitorijum = o;
    porudzbinaRepozitorijum = p;
    organizacijaRepozitorijum = org;
  }
  @Transactional(readOnly = true)
  public byte[] popunjenost(String u, YearMonth m) {
    organizacija(u);
    var od = m.atDay(1).atStartOfDay();
    var kraj = m.plusMonths(1).atDay(1).atStartOfDay();
    List<String> l = new ArrayList<>();
    l.add("MESECNI IZVESTAJ O POPUNJENOSTI - " + m);
    l.add("Objekat / resurs | rezervisani sati | popunjenost");
    for (var r : objekatRepozitorijum.izvestajPopunjenosti(u, od, kraj)) {
      double pct =
          r.getDostupniSati() == 0 ? 0 : r.getRezervisaniSati() * 100.0 / r.getDostupniSati();
      l.add(r.getObjekat() + " / " + r.getResurs() + " | " + r.getRezervisaniSati() + " h | "
          + String.format(java.util.Locale.US, "%.2f%%", pct));
    }
    return pdf(l);
  }
  @Transactional(readOnly = true)
  public byte[] promet(String u, YearMonth m) {
    organizacija(u);
    var od = m.atDay(1).atStartOfDay();
    var kraj = m.plusMonths(1).atDay(1).atStartOfDay();
    List<String> l = new ArrayList<>();
    l.add("MESECNI IZVESTAJ O PROMETU OPREME - " + m);
    l.add("Artikal | kolicina | promet RSD");
    for (var r : porudzbinaRepozitorijum.izvestajPrometa(u, od, kraj))
      l.add(r.getNaziv() + " | " + r.getKolicina() + " | " + r.getPromet());
    if (l.size() == 2)
      l.add("Nema preuzete opreme u izabranom mesecu.");
    return pdf(l);
  }
  private void organizacija(String u) {
    organizacijaRepozitorijum.pronadjiPoKorisnickomImenuZaposlenog(u).orElseThrow(
        () -> new IllegalArgumentException("Organizacija nije pronađena"));
  }
  private byte[] pdf(List<String> linije) {
    try (PDDocument dokument = new PDDocument();
         ByteArrayOutputStream izlaz = new ByteArrayOutputStream()) {
      PDPage stranica = new PDPage();
      dokument.addPage(stranica);
      try (PDPageContentStream sadrzaj = new PDPageContentStream(dokument, stranica)) {
        sadrzaj.beginText();
        sadrzaj.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
        sadrzaj.setLeading(14);
        sadrzaj.newLineAtOffset(50, 790);
        for (String linija : linije) {
          sadrzaj.showText(ascii(linija));
          sadrzaj.newLine();
        }
        sadrzaj.endText();
      }
      dokument.save(izlaz);
      return izlaz.toByteArray();
    } catch (IOException e) {
      throw new IllegalStateException("PDF izveštaj nije generisan", e);
    }
  }
  private String ascii(String s) {
    return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .replace('đ', 'd')
        .replace('Đ', 'D');
  }
}
