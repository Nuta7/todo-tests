package com.bhft.todo.put;

import com.bhft.todo.BaseTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.requests.TodoRequest;
import com.todo.requests.ValidatedTodoRequest;
import com.todo.specs.RequestSpec;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import com.todo.models.Todo;

import java.util.List;

public class PutTodosTests extends BaseTest {

    @BeforeEach
    public void setupEach() {
        deleteAllTodos();
    }


    /**
     * TC1: Обновление существующего TODO корректными данными.
     */
    @Test
    public void testUpdateExistingTodoWithValidData() {
        // Создаем TODO для обновления
        Todo originalTodo = new Todo(2, "Original Task", false);
        createTodo(originalTodo);

        // Обновленные данные
        Todo updatedTodo = new Todo(2, "Updated Task", true);

        // Отправляем PUT запрос для обновления
        ValidatedTodoRequest authValReq = new ValidatedTodoRequest(RequestSpec.authSpec());
        authValReq.create(updatedTodo);

        // Проверяем, что данные были обновлены
        List<Todo> todos = authValReq.readAll();

        Assertions.assertEquals(1, todos.size());
        Assertions.assertEquals("Updated Task", todos.get(0).getText());
        Assertions.assertTrue(todos.get(0).isCompleted());
    }

    /**
     * TC2: Попытка обновления TODO с несуществующим id.
     */
    @Test
    public void testUpdateNonExistentTodo() {
        // Обновленные данные для несуществующего TODO
        Todo updatedTodo = new Todo(999, "Non-existent Task", true);
        ValidatedTodoRequest authValReq = new ValidatedTodoRequest(RequestSpec.authSpec());
        authValReq.create(updatedTodo);
    }

    /**
     * TC3: Обновление TODO с отсутствием обязательных полей.
     */
    @Test
    public void testUpdateTodoWithMissingFields() throws JsonProcessingException {
        // Создаем TODO для обновления
        Todo originalTodo = new Todo(2, "Task to Update", false);
        createTodo(originalTodo);

        // Обновленные данные с отсутствующим полем 'text'
        ObjectMapper mapper = new ObjectMapper();
        String invalidTodoJson = "{ \"id\": 2, \"completed\": true }";
        Todo invalidTodo = mapper.readValue(invalidTodoJson, Todo.class);

        TodoRequest authValReq = new TodoRequest(RequestSpec.authSpec());
        authValReq.create(invalidTodo)
                .then()
                .statusCode(400)
                .contentType(ContentType.TEXT)
                .body(notNullValue());
    }

    /**
     * TC4: Передача некорректных типов данных при обновлении.
     */
    @Test
    public void testUpdateTodoWithInvalidDataTypes() throws JsonProcessingException {
        // Создаем TODO для обновления
        Todo originalTodo = new Todo(3, "Another Task", false);
        createTodo(originalTodo);

        // Обновленные данные с некорректным типом поля 'completed'
        ObjectMapper mapper = new ObjectMapper();
        String invalidTodoJson = "{ \"id\": 3, \"text\": \"Updated Task\", \"completed\": \"notBoolean\" }";
        Todo invalidTodo = mapper.readValue(invalidTodoJson, Todo.class);
        TodoRequest authValReq = new TodoRequest(RequestSpec.authSpec());
        authValReq.create(invalidTodo)
                .then()
                .statusCode(400)
                .contentType(ContentType.TEXT)
                .body(notNullValue());
    }

    /**
     * TC5: Обновление TODO без изменения данных (передача тех же значений).
     */
    @Test
    public void testUpdateTodoWithoutChangingData() {
        // Создаем TODO для обновления
        Todo originalTodo = new Todo(5, "Task without Changes", false);
        createTodo(originalTodo);

        // Отправляем PUT запрос с теми же данными
        ValidatedTodoRequest authValReq = new ValidatedTodoRequest(RequestSpec.authSpec());
        authValReq.create(originalTodo);

        // Проверяем, что данные были обновлены
        List<Todo> todos = authValReq.readAll();

        Assertions.assertEquals("Task without Changes", todos.get(0).getText());
        Assertions.assertFalse(todos.get(0).isCompleted());
    }
}
