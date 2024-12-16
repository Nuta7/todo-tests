package com.todo.requests;

public interface SearchInterface<T> {
    Object readAll(int offset, int limit);
    Object readAll();
}
