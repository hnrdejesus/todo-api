package com.github.hnrdejesus.todo_api.exception;

public class TaskNotFoundException extends RuntimeException{

    public TaskNotFoundException(String message) {
        super(message);
    }

    public TaskNotFoundException(Long id) {
        super("Task with id " + id + " not found");
    }
}

