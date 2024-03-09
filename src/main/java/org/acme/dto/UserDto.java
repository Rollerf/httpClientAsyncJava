package org.acme.dto;

import java.util.List;

public record UserDto(List<AlbumDto> albums, List<PostDto> posts, List<TodoDto> todos) {
}
