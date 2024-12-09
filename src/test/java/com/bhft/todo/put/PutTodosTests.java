package com.bhft.todo.put;

import com.bhft.todo.BaseTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.requests.TodoRequest;
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

public class PutTodosTests extends BaseTest {

    @BeforeEach
    public void setupEach() {
        deleteAllTodos();
    }
    TodoRequest todoRequestValidAuth = new TodoRequest(RequestSpec.authSpec());

    /**
     * TC1: Обновление существующего TODO корректными данными.
     */
    @Test
    public void testUpdateExistingTodoWithValidData() {
        // Создаем TODO для обновления
        Todo originalTodo = new Todo(1, "Original Task", false);
        createTodo(originalTodo);

        // Обновленные данные
        Todo updatedTodo = new Todo(1, "Updated Task", true);

        // Отправляем PUT запрос для обновления
        Assertions.assertEquals(200, ((Response) todoRequestValidAuth.update(1, updatedTodo)).getStatusCode());

        // Проверяем, что данные были обновлены
        Todo[] todos = given()
                .when()
                .get("/todos")
                .then()
                .statusCode(200)
                .extract()
                .as(Todo[].class);

        Assertions.assertEquals(1, todos.length);
        Assertions.assertEquals("Updated Task", todos[0].getText());
        Assertions.assertTrue(todos[0].isCompleted());
    }

    /**
     * TC2: Попытка обновления TODO с несуществующим id.
     */
    @Test
    public void testUpdateNonExistentTodo() {
        // Обновленные данные для несуществующего TODO
        Todo updatedTodo = new Todo(999, "Non-existent Task", true);
        Assertions.assertEquals(404, ((Response) todoRequestValidAuth.update(999, updatedTodo)).getStatusCode());
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

        Assertions.assertEquals(400, ((Response) todoRequestValidAuth.update(2, invalidTodo)).getStatusCode());
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
        Assertions.assertEquals(400, ((Response) todoRequestValidAuth.update(3, invalidTodo)).getStatusCode());
    }

    /**
     * TC5: Обновление TODO без изменения данных (передача тех же значений).
     */
    @Test
    public void testUpdateTodoWithoutChangingData() {
        // Создаем TODO для обновления
        Todo originalTodo = new Todo(4, "Task without Changes", false);
        createTodo(originalTodo);

        // Отправляем PUT запрос с теми же данными
        Assertions.assertEquals(200, ((Response) todoRequestValidAuth.update(4, originalTodo)).getStatusCode());

        // Проверяем, что данные не изменились
        Todo[] todo = given()
                .when()
                .get("/todos")
                .then()
                .statusCode(200)
                .extract()
                .as(Todo[].class);

        Assertions.assertEquals("Task without Changes", todo[0].getText());
        Assertions.assertFalse(todo[0].isCompleted());
    }
}
