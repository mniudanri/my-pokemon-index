import { useState } from "react";
import {
  Alert, Box, Button, FormControl, InputLabel, MenuItem,
  Select, Stack, Typography,
} from "@mui/material";
import { PokemonCard } from "../components/PokemonCard";
import { SearchBar } from "../components/SearchBar";
import { usePokemonList } from "../hooks/usePokemon";

const pageSize = 20;

const types = [
  "normal", "fire", "water", "electric", "grass", "ice", "fighting",
  "poison", "ground", "flying", "psychic", "bug", "rock", "ghost",
  "dragon", "dark", "steel", "fairy",
];

export function PokemonList() {
  const [q, setQ] = useState("");
  const [type, setType] = useState("");
  const [offset, setOffset] = useState(0);
  const { data, isPending, error } = usePokemonList(offset, q, type);

  const totalPages = Math.ceil((data?.total ?? 0) / pageSize);
  const currentPage = Math.floor(offset / pageSize) + 1;

  return (
    <Stack spacing={3}>
      <Typography variant="h4" component="h1">Pokémon</Typography>

      <Box sx={{
        display: "grid",
        gridTemplateColumns: { xs: "1fr", md: "2fr 1fr" },
        gap: 2,
      }}>
        <SearchBar
          value={q}
          onSearch={(next) => {
            setQ(next);
            setOffset(0);
          }}
        />
        <FormControl size="small">
          <InputLabel id="type-label">Type</InputLabel>
          <Select
            labelId="type-label"
            value={type}
            label="Type"
            onChange={(event) => {
              setType(event.target.value);
              setOffset(0);
            }}
          >
            <MenuItem value="">All types</MenuItem>
            {types.map((item) => (
              <MenuItem key={item} value={item}>{item}</MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      {isPending && <Typography>Loading Pokémon…</Typography>}
      {error && <Alert severity="error">{error.message}</Alert>}

      {data && (
        <>
          <Typography>{data.total} results</Typography>
          {data.results.length === 0 && (
            <Typography>No matches found.</Typography>
          )}

          <Box sx={{
            display: "grid",
            gridTemplateColumns: {
              xs: "repeat(2, minmax(0, 1fr))",
              sm: "repeat(3, minmax(0, 1fr))",
              md: "repeat(4, minmax(0, 1fr))",
            },
            gap: 2,
          }}>
            {data.results.map((pokemon) => (
              <PokemonCard key={pokemon.id} pokemon={pokemon} />
            ))}
          </Box>

          {totalPages > 0 && (
            <Stack
              direction="row"
              spacing={1}
              sx={{ alignItems: "center", flexWrap: "wrap" }}
              aria-label="Pagination"
            >
              <Button disabled={offset === 0} onClick={() => setOffset(0)}>
                First
              </Button>
              <Button
                disabled={offset === 0}
                onClick={() => setOffset(Math.max(0, offset - pageSize))}
              >
                Previous
              </Button>

              <Select
                size="small"
                value={currentPage}
                onChange={(event) =>
                  setOffset((Number(event.target.value) - 1) * pageSize)
                }
                inputProps={{ "aria-label": "Page" }}
              >
                {Array.from({ length: totalPages }, (_, index) => (
                  <MenuItem key={index + 1} value={index + 1}>
                    Page {index + 1} of {totalPages}
                  </MenuItem>
                ))}
              </Select>

              <Button
                disabled={currentPage >= totalPages}
                onClick={() => setOffset(offset + pageSize)}
              >
                Next
              </Button>
              <Button
                disabled={currentPage >= totalPages}
                onClick={() => setOffset((totalPages - 1) * pageSize)}
              >
                Last
              </Button>
            </Stack>
          )}
        </>
      )}
    </Stack>
  );
}