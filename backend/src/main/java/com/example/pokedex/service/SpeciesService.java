package com.example.pokedex.service;

import com.example.pokedex.client.PokeApiClient;
import com.example.pokedex.model.SpeciesDtos;
import com.example.pokedex.model.SpeciesDtos.EvolutionNode;
import com.example.pokedex.model.SpeciesDtos.Species;
import com.example.pokedex.model.SpeciesDtos.UpstreamEvolutionNode;
import com.example.pokedex.model.SpeciesDtos.UpstreamSpecies;
import java.net.URI;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SpeciesService {
    private static final Pattern CHAIN_PATH = Pattern.compile("/evolution-chain/(\\d+)/?");

    private final PokeApiClient api;

    public SpeciesService(PokeApiClient api) {
        this.api = api;
    }

    public Species species(String name) {
        UpstreamSpecies upstream = loadSpecies(name);

        if (upstream.flavor_text_entries() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "PokeAPI returned no species descriptions");
        }

        String description = upstream.flavor_text_entries().stream()
                .filter(entry -> entry.language() != null
                        && "en".equals(entry.language().name()))
                .map(entry -> entry.flavor_text().replaceAll("\\s+", " ").trim())
                .filter(text -> !text.isBlank())
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_GATEWAY, "PokeAPI returned no English species description"));

        if (upstream.generation() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "PokeAPI returned no generation");
        }

        return new Species(
                upstream.name(),
                description,
                upstream.generation().name());
    }

    public EvolutionNode evolution(String name) {
        UpstreamSpecies species = loadSpecies(name);
        if (species.evolution_chain() == null || species.evolution_chain().url() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "PokeAPI returned no evolution chain");
        }

        String path = URI.create(species.evolution_chain().url()).getPath();
        Matcher matcher = CHAIN_PATH.matcher(path);
        if (!matcher.find()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "PokeAPI returned an invalid evolution link");
        }

        int id = Integer.parseInt(matcher.group(1));
        SpeciesDtos.UpstreamEvolutionChain chain = api.evolutionChain(id);
        if (chain == null || chain.chain() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "PokeAPI returned an empty evolution chain");
        }
        return map(chain.chain());
    }

    private UpstreamSpecies loadSpecies(String name) {
        if (name == null || !name.toLowerCase(Locale.ROOT).matches("[a-z0-9-]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Pokemon name");
        }
        UpstreamSpecies result = api.species(name.toLowerCase(Locale.ROOT));
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "PokeAPI returned an empty species response");
        }
        return result;
    }

    private EvolutionNode map(UpstreamEvolutionNode node) {
        if (node.species() == null || node.evolves_to() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "PokeAPI returned an invalid evolution chain");
        }
        return new EvolutionNode(node.species().name(), node.evolves_to().stream().map(this::map).toList());
    }
}