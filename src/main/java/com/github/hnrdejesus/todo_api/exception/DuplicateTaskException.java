package com.github.hnrdejesus.todo_api.exception;

public class DuplicateTaskException extends RuntimeException{

    public DuplicateTaskException(String message) {
        super(message);
    }
}
