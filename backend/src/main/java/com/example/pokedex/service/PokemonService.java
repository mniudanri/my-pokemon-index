package com.example.pokedex.service;

import com.example.pokedex.client.PokeApiClient;
import com.example.pokedex.model.PokemonDtos.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import static java.util.regex.Pattern.compile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PokemonService {
    private final PokeApiClient api;

    public PokemonService(PokeApiClient api) {
        this.api = api;
    }

    public Page list(int offset, int limit, String query, String type) {
        if (offset < 0 || limit < 1 || limit > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "offset must be nonnegative and limit must be 1–100");
        }
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        String t = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
        if (q.length() > 100 || (!t.isEmpty() && !t.matches("[a-z0-9-]+"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid search or type");
        }

        if (q.isEmpty() && t.isEmpty()) {
            UpstreamPage upstream = api.page(offset, limit);
            return new Page(upstream.count(), offset, limit, upstream.results().stream().map(this::summary).toList());
        }

        List<Named> names;
        if (!t.isEmpty()) {
            names = api.type(t).pokemon().stream()
                    .map(TypeEntry::pokemon)
                    .sorted(Comparator.comparingInt(this::id))
                    .toList();
        } else {
            names = api.all().results();
        }
        List<Named> matched = names.stream().filter(p -> p.name().contains(q)).toList();
        List results = new ArrayList<Summary>();
        for (long i = offset; i < Math.min((long) offset + limit, matched.size()); i++) {
            results.add(summary(matched.get((int) i)));
        }
        return new Page(matched.size(), offset, limit, results);
    }

    public Detail detail(String name) {
        if (name == null || !name.toLowerCase(Locale.ROOT).matches("[a-z0-9-]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Pokemon name");
        }
        UpstreamPokemon p = api.detail(name.toLowerCase(Locale.ROOT));
        Map<String, Integer> stats = new LinkedHashMap<String, Integer>();
        p.stats().forEach(s -> stats.put(s.stat().name(), s.base_stat()));

        return new Detail(p.id(), p.name(), p.height(), p.weight(),
                p.types().stream().map(s -> s.type().name()).toList(),
                stats, p.sprites().front_default());
    }

    private Summary summary(Named pokemon) {
        int id = id(pokemon);
        return new Summary(id, pokemon.name(), "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png");
    }

    private int id(Named pokemon) {
        String path = URI.create(pokemon.url()).getPath();
        java.util.regex.Matcher match = compile("/pokemon/(\\d+)/?$").matcher(path);
        if (!match.find()) {
            throw new IllegalStateException("Unexpected PokeAPI Pokemon URL: " + path);
        }
        return Integer.parseInt(match.group(1));
    }
}