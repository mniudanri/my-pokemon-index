package com.example.pokedex.controller;

import com.example.pokedex.model.PokemonDtos.*;
import com.example.pokedex.service.AiService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class AiController {
    private final AiService service;

    public AiController(AiService service) {
        this.service = service;
    }

    @GetMapping("/pokemon/{name}/explain")
    public Answer explain(@PathVariable String name) {
        return service.ask(name, "What is this Pokemon? Explain its types and strengths.");
    }

    @PostMapping("/ai/ask")
    public Answer ask(@RequestBody AskRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A request body is required");
        }
        return service.ask(request.pokemon(), request.question());
    }
}