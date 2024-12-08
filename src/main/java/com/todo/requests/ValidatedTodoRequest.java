package com.todo.requests;


import com.todo.models.Todo;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;



public class ValidatedTodoRequest extends TodoRequest {

    public ValidatedTodoRequest(RequestSpecification reqSpec) {
        super(reqSpec);
    }


    public Response sendAssertedPost(Todo entity){
        Response response = super.create(entity);
        return (Response) handleResponse(response, Todo.class);
    }

    public Object sendAssertedPut(long id, Todo entity){
        Response response = (Response) super.update(id, entity);
        return handleResponse(response, Todo.class);
    }

    public  Object sendAssertedDelete(long id){
        Response response = (Response) super.delete(id);
        return handleResponse(response, Todo.class);
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
