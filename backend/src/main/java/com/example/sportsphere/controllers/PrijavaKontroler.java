package com.example.sportsphere.controllers;

import com.example.sportsphere.models.helpers.PrijavaOdgovor;
import com.example.sportsphere.models.helpers.PrijavaZahtev;
import com.example.sportsphere.services.PrijavaServis;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prijava")
public class PrijavaKontroler {
  private final PrijavaServis prijavaServis;
  public PrijavaKontroler(PrijavaServis prijavaServis) {
    this.prijavaServis = prijavaServis;
  }

  @PostMapping
  public PrijavaOdgovor prijava(@Valid @RequestBody PrijavaZahtev zahtev) {
    return prijavaServis.prijava(zahtev);
  }
}
