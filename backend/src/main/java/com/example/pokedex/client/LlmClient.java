package com.example.pokedex.client;

public interface LlmClient {
    String answer(String facts, String question);
}