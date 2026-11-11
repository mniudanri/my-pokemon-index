package com.example.pokedex.model;

import com.example.pokedex.model.PokemonDtos.Named;
import java.util.List;

public final class SpeciesDtos {
    private SpeciesDtos() {}

    public record FlavorText(String flavor_text, Named language) {}

    public record UpstreamSpecies(
            String name,
            List<FlavorText> flavor_text_entries,
            Named generation,
            Named evolution_chain) {}

    public record UpstreamEvolutionNode(
            Named species,
            List<UpstreamEvolutionNode> evolves_to) {}

    public record UpstreamEvolutionChain(UpstreamEvolutionNode chain) {}

    public record Species(
            String name,
            String description,
            String generation) {}

    public record EvolutionNode(
            String name,
            List<EvolutionNode> evolvesTo) {}
}