import { Card, CardActionArea, CardContent, CardMedia, Typography } from "@mui/material";
import { Link as RouterLink } from "react-router-dom";
import type { Summary } from "../api/pokemon";

export function PokemonCard({ pokemon }: { pokemon: Summary }) {
  return (
    <Card sx={{ height: "100%" }}>
      <CardActionArea
        component={RouterLink}
        to={`/pokemon/${encodeURIComponent(pokemon.name)}`}
        sx={{ height: "100%" }}
      >
        <CardMedia
          component="img"
          image={pokemon.sprite}
          alt={pokemon.name}
          sx={{ height: 128, objectFit: "contain", p: 2 }}
        />
        <CardContent>
          <Typography sx={{ textTransform: "capitalize" }}>
            #{pokemon.id} {pokemon.name}
          </Typography>
        </CardContent>
      </CardActionArea>
    </Card>
  );
}