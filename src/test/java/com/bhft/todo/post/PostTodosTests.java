package com.bhft.todo.post;

import com.bhft.todo.BaseTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.models.Todo;
import com.todo.requests.TodoRequest;
import com.todo.specs.RequestSpec;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class PostTodosTests extends BaseTest {

    @BeforeEach
    public void setupEach() {
        deleteAllTodos();
    }
    TodoRequest todoRequestValidAuth = new TodoRequest(RequestSpec.authSpec());

    @Test
    public void testCreateTodoWithValidData() {
        Todo newTodo = new Todo(1, "New Task", false);
        Assertions.assertEquals(201, todoRequestValidAuth.create(newTodo).getStatusCode());

        // Проверяем, что TODO было успешно создано
        Todo[] todos = given()
                .when()
                .get("/todos")
                .then()
                .statusCode(200)
                .extract()
                .as(Todo[].class);

        // Ищем созданную задачу в списке
        boolean found = false;
        for (Todo todo : todos) {
            if (todo.getId() == newTodo.getId()) {
                Assertions.assertEquals(newTodo.getText(), todo.getText());
                Assertions.assertEquals(newTodo.isCompleted(), todo.isCompleted());
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Созданная задача не найдена в списке TODO");
    }

    /**
     * TC2: Попытка создания TODO с отсутствующими обязательными полями.
     */
    @Test
    public void testCreateTodoWithMissingFields() throws JsonProcessingException {
        // Создаем JSON без обязательного поля 'text'
        ObjectMapper mapper = new ObjectMapper();
        String invalidTodoJson = "{ \"id\": 2, \"completed\": true }";
        Todo invalidTodo = mapper.readValue(invalidTodoJson, Todo.class);
        Assertions.assertEquals(400, todoRequestValidAuth.create(invalidTodo).getStatusCode());
    }

    /**
     * TC3: Создание TODO с максимально допустимой длиной поля 'text'.
     */
    @Test
    public void testCreateTodoWithMaxLengthText() {
        // Предполагаем, что максимальная длина поля 'text' составляет 255 символов
        String maxLengthText = "A".repeat(255);
        Todo newTodo = new Todo(3, maxLengthText, false);

        // Отправляем POST запрос для создания нового TODO
        Assertions.assertEquals(201, todoRequestValidAuth.create(newTodo).getStatusCode());

        // Проверяем, что TODO было успешно создано
        Todo[] todos = given()
                .when()
                .get("/todos")
                .then()
                .statusCode(200)
                .extract()
                .as(Todo[].class);

        // Ищем созданную задачу в списке
        boolean found = false;
        for (Todo todo : todos) {
            if (todo.getId() == newTodo.getId()) {
                Assertions.assertEquals(newTodo.getText(), todo.getText());
                Assertions.assertEquals(newTodo.isCompleted(), todo.isCompleted());
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Созданная задача не найдена в списке TODO");
    }

    /**
     * TC4: Передача некорректных типов данных в полях.
     */
    @Test
    public void testCreateTodoWithInvalidDataTypes() {
        // Поле 'completed' содержит строку вместо булевого значения
        Todo newTodo = new Todo(3, "djjdjd", false);
        Assertions.assertEquals(400, todoRequestValidAuth.create(newTodo).getStatusCode());
    }

    /**
     * TC5: Создание TODO с уже существующим 'id' (если 'id' задается клиентом).
     */
    @Test
    public void testCreateTodoWithExistingId() {
        // Сначала создаем TODO с id = 5
        Todo firstTodo = new Todo(5, "First Task", false);
        createTodo(firstTodo);

        // Пытаемся создать другую TODO с тем же id
        Todo duplicateTodo = new Todo(5, "Duplicate Task", true);
        Assertions.assertEquals(400, todoRequestValidAuth.create(duplicateTodo).getStatusCode());
    }

}
