package ru.yandex.practicum.filmorate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TaskClient {
    HttpClient client;

    public TaskClient(HttpClient client) {
        this.client = client;
    }

    public String sendRequest(String path, String requestType, String object) {
        URI url = URI.create(path);
        HttpRequest request;
        switch (requestType) {
            case "GET": {
                request = HttpRequest.newBuilder().uri(url).GET().build();
                break;
            }
            case "POST": {
                request = HttpRequest.newBuilder()
                        .uri(url)
                        .POST(HttpRequest.BodyPublishers.ofString(object))
                        .build();
                break;
            }
            case "PUT": {
                request = HttpRequest.newBuilder().uri(url)
                        .PUT(HttpRequest.BodyPublishers.ofString(object))
                        .build();
                break;
            }
            default:
                request = HttpRequest.newBuilder().uri(url).GET().build();
        }
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return response.body() + " statusCode: " + response.statusCode();
            }
           return response.body();
        } catch (IOException | InterruptedException e) {
            return "Request error, try again later";
        }
    }
}

