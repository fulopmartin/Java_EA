package com.example.beadando.controller;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/szolgaltatasok")
public class SzolgaltatasController {

    private final JdbcClient jdbc;

    public SzolgaltatasController(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public List<Szolgaltatas> listaz() {
        return jdbc.sql("SELECT id, tipus, jelentes FROM szolgaltatas ORDER BY id")
                .query(Szolgaltatas.class)
                .list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void letrehoz(@RequestBody Urlap adat) {
        ellenoriz(adat);
        jdbc.sql("INSERT INTO szolgaltatas (tipus, jelentes) VALUES (:tipus, :jelentes)")
                .param("tipus", adat.tipus().trim())
                .param("jelentes", adat.jelentes().trim())
                .update();
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modosit(@PathVariable("id") int id, @RequestBody Urlap adat) {
        ellenoriz(adat);
        int count = jdbc.sql("UPDATE szolgaltatas SET tipus = :tipus, jelentes = :jelentes WHERE id = :id")
                .param("tipus", adat.tipus().trim())
                .param("jelentes", adat.jelentes().trim())
                .param("id", id)
                .update();

        if (count == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void torol(@PathVariable("id") int id) {
        int count = jdbc.sql("DELETE FROM szolgaltatas WHERE id = :id")
                .param("id", id)
                .update();

        if (count == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private void ellenoriz(Urlap adat) {
        if (adat.tipus() == null || adat.tipus().isBlank() || adat.tipus().trim().length() > 30
                || adat.jelentes() == null || adat.jelentes().isBlank()
                || adat.jelentes().trim().length() > 255) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    public record Szolgaltatas(int id, String tipus, String jelentes) {
    }

    public record Urlap(String tipus, String jelentes) {
    }
}