import { createTheme, ThemeProvider } from "@mui/material/styles";
import { AppBar, Box, Button, Container, CssBaseline, Toolbar, Typography } from "@mui/material";
import { BrowserRouter, Link as RouterLink, Route, Routes } from "react-router-dom";
import { PokemonDetail } from "./pages/PokemonDetail";
import { PokemonList } from "./pages/PokemonList";
import "./App.css";

const theme = createTheme({
  palette: {
    primary: { main: "#234c83" },
    secondary: { main: "#d44d43" },
    background: { default: "#f5f7fb" },
  },
  shape: { borderRadius: 12 },
});

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <BrowserRouter>
        <AppBar position="static">
          <Toolbar sx={{ gap: 1, flexWrap: "wrap" }}>
            <Typography variant="h6" component="span" sx={{ flexGrow: 1 }}>
              Pokémon Index
            </Typography>
            <Button component={RouterLink} to="/" color="inherit">Home</Button>
            <Button component={RouterLink} to="/pokemon" color="inherit">
              Pokémon
            </Button>
            <Button component={RouterLink} to="/about" color="inherit">
              About
            </Button>
          </Toolbar>
        </AppBar>

        <Container maxWidth="lg" sx={{ py: { xs: 3, md: 5 } }}>
          <Routes>
            <Route path="/" element={
              <Box>
                <Typography variant="h3" component="h1" gutterBottom>
                  Explore Pokémon
                </Typography>
                <Typography component="p" sx={{ mb: 2 }}>
                  Browse Pokémon, inspect their evolution, and ask questions.
                </Typography>
                <Button component={RouterLink} to="/pokemon" variant="contained">
                  Browse the index
                </Button>
              </Box>
            } />
            <Route path="/pokemon" element={<PokemonList />} />
            <Route path="/pokemon/:name" element={<PokemonDetail />} />
            <Route path="/about" element={
              <Box>
                <Typography variant="h4" component="h1" gutterBottom>About</Typography>
                <Typography>
                  Pokémon data comes from PokeAPI. AI answers use a LLM
                  model and may be inaccurate.
                </Typography>
              </Box>
            } />
          </Routes>
        </Container>
      </BrowserRouter>
    </ThemeProvider>
  );
}