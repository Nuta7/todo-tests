package com.todo.requests;


import com.todo.models.Todo;
import io.restassured.response.Response;


public class ValidatedTodoRequest {
    public ValidatedTodoRequest(TodoRequest todoRequest) {
        this.todoRequest = todoRequest;
    }

    private TodoRequest todoRequest;

    public <T> T create(Todo entity, Class<T> responseType) {
        Response response = todoRequest.create(entity);
        return handleResponse(response, responseType);
    }

    public <T> T update(long id, Todo entity, Class<T> responseType) {
        Object response = todoRequest.update(id, entity);
        return handleResponse((Response) response, responseType);
    }

    public <T> T delete(long id, Class<T> responseType) {
        Object response = todoRequest.delete(id);
        return handleResponse((Response) response, responseType);
    }

    private <T> T handleResponse(Response response, Class<T> responseType) {
        int statusCode = response.getStatusCode();

        if (statusCode >= 400) {
            throw new RuntimeException("Ошибка запроса: Статус код " + statusCode + ", Тело ответа: " + response.getBody().asString());
        }
        if (responseType != null && (statusCode == 200 || statusCode == 201)) {
            return response.getBody().as(responseType);
        }
        return null;
    }
}
