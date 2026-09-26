package com.example.sportsphere.controllers;

import com.example.sportsphere.models.helpers.PorukaOdgovor;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GreskeKontroler {
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<PorukaOdgovor> losZahtev(IllegalArgumentException e) {
    return ResponseEntity.badRequest().body(new PorukaOdgovor(e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<PorukaOdgovor> validacija(MethodArgumentNotValidException e) {
    String poruka= e.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(greska -> greska.getDefaultMessage())
                        .distinct()
                        .collect(Collectors.joining(", "));
    return ResponseEntity.badRequest().body(new PorukaOdgovor(poruka));
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<PorukaOdgovor> prevelikFajl() {
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
        .body(new PorukaOdgovor("Fajl je prevelik"));
  }
}
