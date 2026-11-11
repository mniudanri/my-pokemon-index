package com.example.pokedex.service;

import com.example.pokedex.client.LlmClient;
import com.example.pokedex.model.PokemonDtos.Answer;
import com.example.pokedex.model.PokemonDtos.Detail;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AiService {
    private final PokemonService pokemonService;
    private final LlmClient llm;

    public AiService(PokemonService pokemonService, LlmClient llm) {
        this.pokemonService = pokemonService;
        this.llm = llm;
    }

    public Answer ask(String name, String question) {
        if (question == null || question.isBlank() || question.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question must contain 1–1000 characters");
        }

        Detail p = pokemonService.detail(name);
        String facts = """
                Name: %s
                Types: %s
                Base stats: %s
                Height in decimetres: %d
                Weight in hectograms: %d
                """.formatted(p.name(), p.types(), p.stats(), p.height(), p.weight());

        return new Answer(llm.answer(facts, question.strip()));
    }
}