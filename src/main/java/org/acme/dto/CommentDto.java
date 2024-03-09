package org.acme.dto;

public record CommentDto(int postId, int id, String name, String email, String body) {
}
