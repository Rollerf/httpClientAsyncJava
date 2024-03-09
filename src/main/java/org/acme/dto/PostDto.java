package org.acme.dto;

import java.util.List;

import lombok.Data;

@Data
public class PostDto {
    private int userId;
    private int id;
    private String title;
    private String body;
    private List<CommentDto> comments;
}
