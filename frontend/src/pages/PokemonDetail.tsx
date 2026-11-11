import { useState, type FormEvent } from "react";
import { useMutation } from "@tanstack/react-query";
import {
  Alert, Box, Button, Card, CardContent, Chip, Divider,
  Stack, TextField, Typography,
} from "@mui/material";
import { Link as RouterLink, useParams } from "react-router-dom";
import { pokemonApi, type EvolutionNode } from "../api/pokemon";
import {
  usePokemonDetail, usePokemonEvolution, usePokemonSpecies,
} from "../hooks/usePokemon";

function EvolutionBranch({ node }: { node: EvolutionNode }) {
  return (
    <Box sx={{ borderLeft: "2px solid", borderColor: "divider", pl: 2, py: 1 }}>
      <Button
        component={RouterLink}
        to={`/pokemon/${encodeURIComponent(node.name)}`}
        sx={{ textTransform: "capitalize" }}
      >
        {node.name}
      </Button>
      {node.evolvesTo.map((child) =>
        <EvolutionBranch key={child.name} node={child} />)}
    </Box>
  );
}

export function PokemonDetail() {
  const { name = "" } = useParams();
  return <PokemonDetailContent key={name} name={name} />;
}

function PokemonDetailContent({ name }: { name: string }) {
  const detail = usePokemonDetail(name);
  const species = usePokemonSpecies(name);
  const evolution = usePokemonEvolution(name);
  const [question, setQuestion] = useState("");
  const answer = useMutation({
    mutationFn: (text: string) => pokemonApi.ask(name, text),
  });

  function ask(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (question.trim()) answer.mutate(question.trim());
  }

  if (detail.isPending) return <Typography>Loading Pokémon…</Typography>;
  if (detail.error) return <Alert severity="error">{detail.error.message}</Alert>;
  if (!detail.data) return <Alert severity="error">No Pokémon data returned.</Alert>;

  const pokemon = detail.data;

  return (
    <Stack spacing={3}>
      <Button component={RouterLink} to="/pokemon" sx={{ alignSelf: "start" }}>
        ← Back to index
      </Button>
      <Card>
        <CardContent>
          <Box sx={{
            display: "grid",
            gridTemplateColumns: { xs: "1fr", md: "220px 1fr" },
            gap: 3,
            alignItems: "center",
          }}>
            {pokemon.sprite && (
              <Box
                component="img"
                src={pokemon.sprite}
                alt={pokemon.name}
                sx={{ width: "100%", maxWidth: 220, margin: "auto" }}
              />
            )}
            <Stack spacing={2}>
              <Typography variant="h4" component="h1" sx={{
                textTransform: "capitalize",
              }}>
                #{pokemon.id} {pokemon.name}
              </Typography>
              <Stack direction="row" spacing={1}>
                {pokemon.types.map((type) =>
                  <Chip key={type} label={type} sx={{ textTransform: "capitalize" }} />)}
              </Stack>
              <Typography>
                Height: {pokemon.height / 10} m · Weight: {pokemon.weight / 10} kg
              </Typography>
              <Divider />
              <Typography variant="h6" component="h2">Base stats</Typography>
              <Box sx={{
                display: "grid",
                gridTemplateColumns: { xs: "1fr 1fr", sm: "repeat(3, 1fr)" },
                gap: 1,
              }}>
                {Object.entries(pokemon.stats).map(([stat, value]) => (
                  <Typography key={stat}>{stat}: <strong>{value}</strong></Typography>
                ))}
              </Box>
            </Stack>
          </Box>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Typography variant="h6" component="h2" gutterBottom>Species</Typography>
          {species.isPending && <Typography>Loading species…</Typography>}
          {species.error && <Alert severity="error">{species.error.message}</Alert>}
          {species.data && (
            <>
              <Typography component="p" sx={{ mb: 2 }}>{species.data.description}</Typography>
              <Chip label={species.data.generation} />
            </>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Typography variant="h6" component="h2" gutterBottom>
            Evolution
          </Typography>
          {evolution.isPending && <Typography>Loading evolution…</Typography>}
          {evolution.error &&
            <Alert severity="error">{evolution.error.message}</Alert>}
          {evolution.data && <EvolutionBranch node={evolution.data} />}
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Typography variant="h6" component="h2" gutterBottom>
            Ask about {pokemon.name}
          </Typography>
          <Stack
            component="form"
            onSubmit={ask}
            direction={{ xs: "column", sm: "row" }}
            spacing={1}
          >
            <TextField
              label="Your question"
              size="small"
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              slotProps={{ htmlInput: { maxLength: 1000 } }}
              sx={{ flexGrow: 1 }}
            />
            <Button type="submit" variant="contained" disabled={answer.isPending}>
              Ask
            </Button>
          </Stack>
          {answer.isPending && <Typography sx={{ mt: 2 }}>Thinking…</Typography>}
          {answer.isError &&
            <Alert severity="error" sx={{ mt: 2 }}>{answer.error.message}</Alert>}
          {answer.data &&
            <Typography sx={{ mt: 2 }} aria-live="polite">
              {answer.data.answer}
            </Typography>}
          <Typography variant="caption" component="p" sx={{ display: "block", mt: 1 }}>
            AI answers may be inaccurate; verify claims against Pokémon facts.
          </Typography>
        </CardContent>
      </Card>
    </Stack>
  );
}