package com.todo.specs;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.authentication.BasicAuthScheme;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.List;

public class RequestSpec {
    private RequestSpecBuilder requestSpecBuilder;

    private static RequestSpecBuilder baseSpecBuilder() {
        RequestSpecBuilder requestSpecBuilder = new RequestSpecBuilder();
        requestSpecBuilder.addFilters(List.of(
                new RequestLoggingFilter(), new
                ResponseLoggingFilter(),
                new AllureRestAssured()));
        requestSpecBuilder.setContentType(ContentType.JSON);
        requestSpecBuilder.setAccept(ContentType.JSON);
        return requestSpecBuilder;
    }

    public static RequestSpecification unauthSpec() {
        BasicAuthScheme basicAuthScheme = new BasicAuthScheme();
        basicAuthScheme.setUserName("bla");
        basicAuthScheme.setPassword("bla");
        baseSpecBuilder().setAuth(basicAuthScheme);

        return baseSpecBuilder().build();
    }

    public static RequestSpecification authSpec() {
        BasicAuthScheme basicAuthScheme = new BasicAuthScheme();
        basicAuthScheme.setUserName("admin");
        basicAuthScheme.setPassword("admin");
        baseSpecBuilder().setAuth(basicAuthScheme);
        return baseSpecBuilder().build();
    }
}
