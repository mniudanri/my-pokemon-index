import { useState, type FormEvent } from "react";
import { Button, Stack, TextField } from "@mui/material";

export function SearchBar({
  value,
  onSearch,
}: {
  value: string;
  onSearch: (value: string) => void;
}) {
  const [draft, setDraft] = useState(value);

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSearch(draft.trim());
  }

  return (
    <Stack
      component="form"
      onSubmit={submit}
      direction={{ xs: "column", sm: "row" }}
      spacing={1}
    >
      <TextField
        label="Search Pokémon names"
        size="small"
        value={draft}
        onChange={(event) => setDraft(event.target.value)}
        slotProps={{ htmlInput: { maxLength: 100 } }}
        sx={{ flexGrow: 1 }}
      />
      <Button type="submit" variant="contained">Search</Button>
    </Stack>
  );
}