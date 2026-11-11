import { useQuery } from "@tanstack/react-query";
import { pokemonApi } from "../api/pokemon";

export function usePokemonList(offset: number, q: string, type: string) {
  return useQuery({
    queryKey: ["pokemon", "list", offset, q, type],
    queryFn: () => pokemonApi.list(offset, q, type),
  });
}

export function usePokemonDetail(name: string) {
  return useQuery({
    queryKey: ["pokemon", "detail", name],
    queryFn: () => pokemonApi.detail(name),
    enabled: Boolean(name),
  });
}

export function usePokemonSpecies(name: string) {
  return useQuery({
    queryKey: ["pokemon", "species", name],
    queryFn: () => pokemonApi.species(name),
    enabled: Boolean(name),
  });
}

export function usePokemonEvolution(name: string) {
  return useQuery({
    queryKey: ["pokemon", "evolution", name],
    queryFn: () => pokemonApi.evolution(name),
    enabled: Boolean(name),
  });
}