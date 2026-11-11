package com.example.pokedex.controller;

import com.example.pokedex.model.SpeciesDtos.EvolutionNode;
import com.example.pokedex.model.SpeciesDtos.Species;
import com.example.pokedex.service.SpeciesService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pokemon")
public class SpeciesController {
    private final SpeciesService service;

    public SpeciesController(SpeciesService service) {
        this.service = service;
    }

    @GetMapping("/{name}/species")
    public Species species(@PathVariable String name) {
        return service.species(name);
    }

    @GetMapping("/{name}/evolution")
    public EvolutionNode evolution(@PathVariable String name) {
        return service.evolution(name);
    }
}