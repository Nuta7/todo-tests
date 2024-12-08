package com.todo.requests;

import com.todo.models.Todo;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public abstract class Request {
    protected RequestSpecification reqSpec;

    public Request(RequestSpecification reqSpec) {
        this.reqSpec = reqSpec;
    }

}
