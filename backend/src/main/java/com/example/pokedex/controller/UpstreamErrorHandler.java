package com.example.pokedex.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class UpstreamErrorHandler {
    @ExceptionHandler(RestClientException.class)
    public org.springframework.http.ResponseEntity<Map<String, String>> upstream(RestClientException exception) {
        exception.printStackTrace();
        return org.springframework.http.ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "An upstream service request failed"));
    }
}