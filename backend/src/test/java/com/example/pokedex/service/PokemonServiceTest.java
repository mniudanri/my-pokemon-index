package com.example.pokedex.service;

import com.example.pokedex.client.LlmClient;
import com.example.pokedex.client.PokeApiClient;
import com.example.pokedex.model.PokemonDtos.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PokemonServiceTest {
        private PokeApiClient api;
        private PokemonService service;

        @BeforeEach
        void setUp() {
                api = mock(PokeApiClient.class);
                service = new PokemonService(api);
        }

        @Test
        void paginatesUnfilteredResults() {
                when(api.page(20, 2)).thenReturn(new UpstreamPage(100,
                        List.of(named("pikachu", 25), named("raichu", 26))));

                Page page = service.list(20, 2, "", "");

                assertEquals(100, page.total());
                assertEquals(20, page.offset());
                assertEquals(List.of("pikachu", "raichu"),
                        page.results().stream().map(Summary::name).toList());
                verify(api).page(20, 2);
                verifyNoMoreInteractions(api);
        }

        @Test
        void searchesNamesAndPaginatesMatches() {
                when(api.all()).thenReturn(new UpstreamPage(3, List.of(
                        named("pikachu", 25),
                        named("raichu", 26),
                        named("pichu", 172))));

                Page page = service.list(1, 1, "  CHU  ", "");

                assertEquals(3, page.total());
                assertEquals(List.of("raichu"),
                        page.results().stream().map(Summary::name).toList());
                verify(api).all();
                verifyNoMoreInteractions(api);
        }

        @Test
        void combinesTypeFilterWithSearch() {
                when(api.type("electric")).thenReturn(new UpstreamType(List.of(
                        new TypeEntry(named("raichu", 26)),
                        new TypeEntry(named("pikachu", 25)),
                        new TypeEntry(named("electabuzz", 125)))));

                Page page = service.list(0, 20, "CHU", "Electric");

                assertEquals(2, page.total());
                assertEquals(List.of("pikachu", "raichu"),
                        page.results().stream().map(Summary::name).toList());
                verify(api).type("electric");
                verifyNoMoreInteractions(api);
        }

        @Test
        void returnsEmptyPageBeyondLastMatch() {
                when(api.all()).thenReturn(new UpstreamPage(1,
                        List.of(named("pikachu", 25))));

                Page page = service.list(10, 20, "pika", "");

                assertEquals(1, page.total());
                assertTrue(page.results().isEmpty());
        }

        @Test
        void mapsDetailAndNormalizesName() {
                when(api.detail("pikachu")).thenReturn(new UpstreamPokemon(
                        25, "pikachu", 4, 60,
                        List.of(new TypeSlot(1, new Named("electric", ""))),
                        List.of(new StatSlot(55, new Named("attack", ""))),
                        new Sprites("https://example.test/pikachu.png")));

                Detail detail = service.detail("PIKACHU");

                assertEquals(25, detail.id());
                assertEquals(List.of("electric"), detail.types());
                assertEquals(55, detail.stats().get("attack"));
                verify(api).detail("pikachu");
        }

        @Test
        void rejectsInvalidInputsBeforeCallingPokeApi() {
                assertThrows(ResponseStatusException.class,
                        () -> service.list(-1, 20, "", ""));
                assertThrows(ResponseStatusException.class,
                        () -> service.list(0, 101, "", ""));
                assertThrows(ResponseStatusException.class,
                        () -> service.list(0, 20, "", "invalid/type"));
                assertThrows(ResponseStatusException.class,
                        () -> service.detail("bad/name"));
                verifyNoInteractions(api);
        }

        @Test
        void asksLlmUsingFetchedFacts() {
        LlmClient llm = mock(LlmClient.class);
        when(api.detail("pikachu")).thenReturn(new UpstreamPokemon(
                25, "pikachu", 4, 60,
                List.of(new TypeSlot(1, new Named("electric", ""))),
                List.of(new StatSlot(55, new Named("attack", ""))),
                new Sprites(null)));
        when(llm.answer(anyString(), anyString()))
                .thenReturn("Pikachu is Electric-type.");

        Answer answer = new AiService(service, llm)
                .ask("pikachu", "What type is it?");

        assertEquals("Pikachu is Electric-type.", answer.answer());
        verify(llm).answer(
                argThat(facts -> facts.contains("electric")
                        && facts.contains("attack=55")),
                eq("What type is it?"));
        }

        @Test
        void rejectsBlankQuestionWithoutCallingExternalServices() {
        LlmClient llm = mock(LlmClient.class);

        assertThrows(ResponseStatusException.class,
                () -> new AiService(service, llm).ask("pikachu", "  "));

        verifyNoInteractions(api, llm);
        }

        private static Named named(String name, int id) {
                return new Named(name, "https://pokeapi.co/api/v2/pokemon/" + id + "/");
        }
}