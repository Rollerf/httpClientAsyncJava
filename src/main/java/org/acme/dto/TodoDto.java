package org.acme.dto;

public record TodoDto(int userId, int id, String title, boolean completed) {
}
