package com.bhft.todo.get;


import com.bhft.todo.BaseTest;
import com.todo.requests.TodoRequest;
import com.todo.requests.ValidatedTodoRequest;
import com.todo.specs.RequestSpec;
import io.qameta.allure.*;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import com.todo.models.Todo;

import java.util.List;

@Epic("TODO Management")
@Feature("Get Todos API")
public class GetTodosTests extends BaseTest {

    @BeforeEach
    public void setupEach() {
        deleteAllTodos();
    }

    @Test
    @Description("Получение пустого списка TODO, когда база данных пуста")
    public void testGetTodosWhenDatabaseIsEmpty() {
        ValidatedTodoRequest authValReq = new ValidatedTodoRequest(RequestSpec.authSpec());
        List<Todo> todos = authValReq.readAll();
    }

    @Test
    @Description("Получение списка TODO с существующими записями")
    public void testGetTodosWithExistingEntries() {
        // Предварительно создать несколько TODO
        Todo todo1 = new Todo(1, "Task 1", false);
        Todo todo2 = new Todo(2, "Task 2", true);

        createTodo(todo1);
        createTodo(todo2);

        ValidatedTodoRequest authValReq = new ValidatedTodoRequest(RequestSpec.authSpec());
        List<Todo> todos = authValReq.readAll();

        // Дополнительная проверка содержимого

        Assertions.assertEquals(1, todos.get(0).getId());
        Assertions.assertEquals("Task 1", todos.get(0).getText());
        Assertions.assertFalse(todos.get(0).isCompleted());

        Assertions.assertEquals(2, todos.get(1).getId());
        Assertions.assertEquals("Task 2",todos.get(1).getText());
        Assertions.assertTrue(todos.get(1).isCompleted());
    }

    @Test
    @Description("Использование параметров offset и limit для пагинации")
    public void testGetTodosWithOffsetAndLimit() {
        // Создаем 5 TODO
        for (int i = 1; i <= 5; i++) {
            createTodo(new Todo(i, "Task " + i, i % 2 == 0));
        }
        ValidatedTodoRequest authValReq = new ValidatedTodoRequest(RequestSpec.authSpec());
        List<Todo> todos = authValReq.readAll(2,3);

        // Проверяем, что получили задачи с id 3 и 4

        Assertions.assertEquals(3, todos.get(0).getId());
        Assertions.assertEquals("Task 3", todos.get(0).getText());

        Assertions.assertEquals(4, todos.get(1).getId());
        Assertions.assertEquals("Task 4", todos.get(1).getText());
    }

    @Test
    @DisplayName("Передача некорректных значений в offset и limit")
    public void testGetTodosWithInvalidOffsetAndLimit() {
        // Тест с отрицательным offset
        TodoRequest authValReq = new TodoRequest(RequestSpec.authSpec());
        Response todos = authValReq.readAll(-1,2);

        // Тест с нечисловым limit
        given()
                .filter(new AllureRestAssured())
                .queryParam("offset", 0)
                .queryParam("limit", "abc")
                .when()
                .get("/todos")
                .then()
                .statusCode(400)
                .contentType("text/plain")
                .body(containsString("Invalid query string"));

        // Тест с отсутствующим значением offset
        given()
                .filter(new AllureRestAssured())
                .queryParam("offset", "")
                .queryParam("limit", 2)
                .when()
                .get("/todos")
                .then()
                .statusCode(400)
                .contentType("text/plain")
                .body(containsString("Invalid query string"));
    }

    @Test
    @DisplayName("Проверка ответа при превышении максимально допустимого значения limit")
    public void testGetTodosWithExcessiveLimit() {
        // Создаем 10 TODO
        for (int i = 1; i <= 10; i++) {
            createTodo(new Todo(i, "Task " + i, i % 2 == 0));
        }

        ValidatedTodoRequest authValReq = new ValidatedTodoRequest(RequestSpec.authSpec());
        List<Todo> todos = authValReq.readAll(0,1000);

        // Проверяем, что вернулось 10 задач
        Assertions.assertEquals(10, todos.size());
    }
}
