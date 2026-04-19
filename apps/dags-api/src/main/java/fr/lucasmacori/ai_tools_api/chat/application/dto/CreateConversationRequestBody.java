package fr.lucasmacori.ai_tools_api.chat.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public record CreateConversationRequestBody(@JsonProperty("name") @NotBlank String name) {}
