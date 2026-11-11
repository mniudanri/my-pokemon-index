const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export type Summary = { id: number; name: string; sprite: string };
export type Page = {
  total: number;
  offset: number;
  limit: number;
  results: Summary[];
};
export type Detail = {
  id: number;
  name: string;
  height: number;
  weight: number;
  types: string[];
  stats: Record<string, number>;
  sprite: string | null;
};

export type Species = {
  name: string;
  description: string;
  generation: string;
};

export type EvolutionNode = {
  name: string;
  evolvesTo: EvolutionNode[];
};

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${baseUrl}${path}`, init);
  if (!response.ok) {
    throw new Error(`Request failed (${response.status}): ${await response.text()}`);
  }
  return response.json() as Promise<T>;
}

export const pokemonApi = {
  species: (name: string) => request<Species>(`/api/pokemon/${encodeURIComponent(name)}/species`),

evolution: (name: string) =>
  request<EvolutionNode>(`/api/pokemon/${encodeURIComponent(name)}/evolution`),

  list: (offset: number, q: string, type: string) =>
    request<Page>(
      `/api/pokemon?${new URLSearchParams({
        offset: String(offset),
        limit: "20",
        q,
        type,
      })}`,
    ),

    detail: (name: string) => request<Detail>(`/api/pokemon/${encodeURIComponent(name)}`),

    ask: (pokemon: string, question: string) =>
      request<{ answer: string }>("/api/ai/ask", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ pokemon, question }),
      }),
};