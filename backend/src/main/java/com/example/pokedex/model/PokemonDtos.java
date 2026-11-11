package com.example.pokedex.model;

import java.util.List;
import java.util.Map;

public final class PokemonDtos {
    private PokemonDtos() {}

    public record Named(String name, String url) {}
    public record UpstreamPage(int count, List<Named> results) {}
    public record TypeEntry(Named pokemon) {}
    public record UpstreamType(List<TypeEntry> pokemon) {}
    public record TypeSlot(int slot, Named type) {}
    public record StatSlot(int base_stat, Named stat) {}
    public record Sprites(String front_default) {}
    public record UpstreamPokemon(
        int id, String name,
        int height, int weight,
        List<TypeSlot> types,
        List<StatSlot> stats, Sprites sprites
        ) {}

    public record Summary(int id, String name, String sprite) {}
    public record Page(int total, int offset, int limit, List<Summary> results) {}
    public record Detail(
            int id, String name, int height, int weight,
            List<String> types, Map<String, Integer> stats, String sprite) {}
    public record AskRequest(String pokemon, String question) {}
    public record Answer(String answer) {}
}