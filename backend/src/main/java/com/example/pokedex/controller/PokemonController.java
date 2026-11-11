package com.example.pokedex.controller;

import com.example.pokedex.model.PokemonDtos.*;
import com.example.pokedex.service.PokemonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {
    private final PokemonService service;

    public PokemonController(PokemonService service) {
        this.service = service;
    }

    @GetMapping
    public Page list(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String type) {
        return service.list(offset, limit, q, type);
    }

    @GetMapping("/{name}")
    public Detail detail(@PathVariable String name) {
        return service.detail(name);
    }
}