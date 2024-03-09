package org.acme.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.acme.dto.AlbumDto;
import org.acme.dto.CommentDto;
import org.acme.dto.PostDto;
import org.acme.dto.TodoDto;
import org.acme.dto.UserDto;
import org.jboss.resteasy.reactive.RestResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/example")
public class Example {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @GET
    @Path("/requestTest/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public CompletableFuture<RestResponse<UserDto>> modelingRequestWithCommentsTest(
            @PathParam("userId") int userId) {
        Map<String, URI> endpoints = createEndpoints(userId);
        HttpClient client = HttpClient.newHttpClient();
        var todoEndpoint = endpoints.get("TodoDto");
        var postEndpoint = endpoints.get("PostDto");
        var albumEnpoint = endpoints.get("AlbumDto");

        CompletableFuture<HttpResponse<String>> futureTodo = client.sendAsync(
                HttpRequest.newBuilder(todoEndpoint).build(),
                HttpResponse.BodyHandlers.ofString());

        CompletableFuture<HttpResponse<String>> futurePost = client.sendAsync(
                HttpRequest.newBuilder(postEndpoint).build(),
                HttpResponse.BodyHandlers.ofString());

        CompletableFuture<HttpResponse<String>> futureAlbum = client.sendAsync(
                HttpRequest.newBuilder(albumEnpoint).build(),
                HttpResponse.BodyHandlers.ofString());

        return CompletableFuture.allOf(futureTodo, futurePost, futureAlbum)
                .thenApply(v -> {
                    List<TodoDto> todosResponse = null;
                    List<AlbumDto> albumsResponse = null;
                    List<PostDto> postsResponse = null;

                    try {
                        todosResponse = processResponse(
                                futureTodo, new TypeReference<List<TodoDto>>() {
                                }, MAPPER);
                        albumsResponse = processResponse(
                                futureAlbum, new TypeReference<List<AlbumDto>>() {
                                }, MAPPER);
                        TypeReference<List<PostDto>> typeRef2 = new TypeReference<List<PostDto>>() {
                        };

                        postsResponse = MAPPER.readValue(futurePost.get().body(),
                                typeRef2);
                        var postsFuture = processPostResponse(postsResponse);

                        CompletableFuture.allOf(postsFuture.toArray(new CompletableFuture[0])).join();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return RestResponse
                            .ok(new UserDto(albumsResponse, postsResponse, todosResponse));
                });
    }

    private List<CompletableFuture<PostDto>> processPostResponse(List<PostDto> postsResponse)
            throws InterruptedException, ExecutionException {
        HttpClient client = HttpClient.newHttpClient();
        return postsResponse.stream().map(post -> {
            try {
                URI commentsUri = new URI(
                        "https://jsonplaceholder.typicode.com/posts/"
                                + post.getId() + "/comments");
                return client
                        .sendAsync(
                                HttpRequest.newBuilder(commentsUri)
                                        .build(),
                                HttpResponse.BodyHandlers.ofString())
                        .thenApplyAsync(response -> {
                            TypeReference<List<CommentDto>> typeRefComments = new TypeReference<List<CommentDto>>() {
                            };
                            try {
                                List<CommentDto> comments = MAPPER
                                        .readValue(
                                                response.body(),
                                                typeRefComments);
                                post.setComments(comments);
                            } catch (JsonMappingException e) {
                                e.printStackTrace();
                            } catch (JsonProcessingException e) {
                                e.printStackTrace();
                            }
                            return post;
                        });
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    private <T> List<T> processResponse(CompletableFuture<HttpResponse<String>> future, TypeReference<List<T>> type,
            ObjectMapper mapper) throws InterruptedException, ExecutionException {
        try {
            return mapper.readValue(future.get().body(), type);
        } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<String, URI> createEndpoints(int userId) {
        try {
            return Map.of(
                    "TodoDto", new URI("https://jsonplaceholder.typicode.com/users/" + userId + "/todos"),
                    "PostDto", new URI("https://jsonplaceholder.typicode.com/users/" + userId + "/posts"),
                    "AlbumDto", new URI("https://jsonplaceholder.typicode.com/users/" + userId + "/albums"));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
