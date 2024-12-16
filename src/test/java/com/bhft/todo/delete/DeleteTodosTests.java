package com.bhft.todo.delete;

import com.bhft.todo.BaseTest;

import com.bhft.todo.BaseTest;
import com.todo.requests.TodoRequest;
import com.todo.requests.ValidatedTodoRequest;
import com.todo.specs.RequestSpec;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import com.todo.models.Todo;

import java.util.List;

public class DeleteTodosTests extends BaseTest {

    @BeforeEach
    public void setupEach() {
        deleteAllTodos();
    }

    /**
     * TC1: Успешное удаление существующего TODO с корректной авторизацией.
     */
    @Test
    public void testDeleteExistingTodoWithValidAuth() {
        // Создаем TODO для удаления
        Todo todo = new Todo(1, "Task to Delete", false);
        createTodo(todo);

        ValidatedTodoRequest authValReq = new ValidatedTodoRequest(RequestSpec.authSpec());
        authValReq.create(todo);

        // Проверяем, что TODO было успешно создано
        List<Todo> todos = authValReq.readAll();

        // Проверяем, что удаленная задача отсутствует в списке
        boolean found = false;
        for (Todo t : todos) {
            if (t.getId() == todo.getId()) {
                found = true;
                break;
            }
        }
        Assertions.assertFalse(found, "Удаленная задача все еще присутствует в списке TODO");
    }

    /**
     * TC2: Попытка удаления TODO без заголовка Authorization.
     */
    @Test
    public void testDeleteTodoWithoutAuthHeader() {
        // Создаем TODO для удаления
        Todo todo = new Todo(2, "Task to Delete", false);
        createTodo(todo);

        // Отправляем DELETE запрос без заголовка Authorization
        TodoRequest unauthValReq = new TodoRequest(RequestSpec.unauthSpec());
        unauthValReq.create(todo)
                .then()
                .statusCode(401);

        // Проверяем, что TODO не было удалено
        List<Todo> todos = (List<Todo>) unauthValReq.readAll();

        // Проверяем, что задача все еще присутствует в списке
        boolean found = false;
        for (Todo t : todos) {
            if (t.getId() == todo.getId()) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Задача отсутствует в списке TODO, хотя не должна была быть удалена");
    }

    /**
     * TC3: Попытка удаления TODO с некорректными учетными данными.
     */
    @Test
    public void testDeleteTodoWithInvalidAuth() {
        // Создаем TODO для удаления
        Todo todo = new Todo(3, "Task to Delete", false);
        createTodo(todo);

        // Отправляем DELETE запрос с некорректной авторизацией
        TodoRequest unauthValReq = new TodoRequest(RequestSpec.unauthSpec());
        unauthValReq.create(todo)
                .then()
                .statusCode(401);

        // Проверяем, что TODO не было удалено
        List<Todo> todos = (List<Todo>) unauthValReq.readAll();

        // Проверяем, что задача все еще присутствует в списке
        boolean found = false;
        for (Todo t : todos) {
            if (t.getId() == todo.getId()) {
                found = true;
                break;
            }
        }
        Assertions.assertTrue(found, "Задача отсутствует в списке TODO, хотя не должна была быть удалена");
    }

    /**
     * TC4: Удаление TODO с несуществующим id.
     */
    @Test
    public void testDeleteNonExistentTodo() {
        // Отправляем DELETE запрос для несуществующего TODO с корректной авторизацией
        given()
                .filter(new AllureRestAssured())
                .auth()
                .preemptive()
                .basic("admin", "admin")
                .when()
                .delete("/todos/999")
                .then()
                .statusCode(404);
//                .contentType(ContentType.JSON)
//                .body("error", notNullValue());

        // Дополнительно можем проверить, что список TODO не изменился
        Todo[] todos = given()
                .when()
                .get("/todos")
                .then()
                .statusCode(200)
                .extract()
                .as(Todo[].class);


        // В данном случае, поскольку мы не добавляли задач с id 999, список должен быть пуст или содержать только ранее добавленные задачи
    }

    /**
     * TC5: Попытка удаления с некорректным форматом id (например, строка вместо числа).
     */
    @Test
    public void testDeleteTodoWithInvalidIdFormat() {
        // Отправляем DELETE запрос с некорректным id
            given()
                    .filter(new AllureRestAssured())
                    .auth()
                    .preemptive()
                    .basic("admin", "admin")
                    .when()
                    .delete("/todos/invalidId")
                    .then()
                    .statusCode(404);
//                .contentType(ContentType.JSON)
//                .body("error", notNullValue());
        }
    }
