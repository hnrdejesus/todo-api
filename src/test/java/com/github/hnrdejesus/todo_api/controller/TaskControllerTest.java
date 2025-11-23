package com.github.hnrdejesus.todo_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.hnrdejesus.todo_api.dto.TaskCreateDTO;
import com.github.hnrdejesus.todo_api.dto.TaskResponseDTO;
import com.github.hnrdejesus.todo_api.dto.TaskUpdateDTO;
import com.github.hnrdejesus.todo_api.exception.DuplicateTaskException;
import com.github.hnrdejesus.todo_api.exception.GlobalExceptionHandler;
import com.github.hnrdejesus.todo_api.exception.TaskNotFoundException;
import com.github.hnrdejesus.todo_api.model.Task;
import com.github.hnrdejesus.todo_api.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API tests for TaskController.
 * Uses MockMvc to simulate HTTP requests and test controller endpoints in isolation.
 * Verifies REST API behavior including request/response handling, status codes, and validation.
 */
@WebMvcTest(controllers = {TaskController.class, GlobalExceptionHandler.class})
@DisplayName("TaskController API Tests")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    private Task task;
    private TaskResponseDTO taskResponseDTO;

    @BeforeEach
    void setUp() {
        task = createTask(1L, "Test Task", "Description", false);
        taskResponseDTO = TaskResponseDTO.from(task);
    }

    @Test
    @DisplayName("Should return all tasks")
    void shouldReturnAllTasks() throws Exception {

        List<Task> tasks = Arrays.asList(
                createTask(1L, "Task 1", "Desc 1", false),
                createTask(2L, "Task 2", "Desc 2", true)
        );
        when(taskService.findAll()).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Task 1")))
                .andExpect(jsonPath("$[1].title", is("Task 2")));

        verify(taskService, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return completed tasks when filter is applied")
    void shouldReturnCompletedTasksWhenFilterApplied() throws Exception {

        List<Task> completedTasks = List.of(
                createTask(1L, "Task 1", "Desc", true)
        );
        when(taskService.findByCompleted(true)).thenReturn(completedTasks);

        mockMvc.perform(get("/api/tasks")
                        .param("completed", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].completed", is(true)));

        verify(taskService).findByCompleted(true);
    }

    @Test
    @DisplayName("Should return task by ID")
    void shouldReturnTaskById() throws Exception {

        when(taskService.findById(1L)).thenReturn(Optional.of(task));

        mockMvc.perform(get("/api/tasks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Task")))
                .andExpect(jsonPath("$.description", is("Description")))
                .andExpect(jsonPath("$.completed", is(false)));

        verify(taskService).findById(1L);
    }

    @Test
    @DisplayName("Should return 404 when task not found by ID")
    void shouldReturn404WhenTaskNotFoundById() throws Exception {

        when(taskService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tasks/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(taskService).findById(999L);
    }

    @Test
    @DisplayName("Should create task successfully")
    void shouldCreateTask() throws Exception {

        TaskCreateDTO createDTO = TaskCreateDTO.builder()
                .title("New Task")
                .description("New Description")
                .build();

        Task createdTask = createTask(1L, "New Task", "New Description", false);

        when(taskService.create(any(Task.class))).thenReturn(createdTask);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("New Task")))
                .andExpect(jsonPath("$.completed", is(false)));

        verify(taskService).create(any(Task.class));
    }

    @Test
    @DisplayName("Should return 400 when creating task with invalid data")
    void shouldReturn400WhenCreatingTaskWithInvalidData() throws Exception {

        TaskCreateDTO invalidDTO = TaskCreateDTO.builder()
                .title("")
                .description("Description")
                .build();

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Validation Failed")));

        verify(taskService, never()).create(any(Task.class));
    }

    @Test
    @DisplayName("Should return 409 when creating task with duplicate title")
    void shouldReturn409WhenCreatingTaskWithDuplicateTitle() throws Exception {

        TaskCreateDTO createDTO = TaskCreateDTO.builder()
                .title("Duplicate Task")
                .description("Description")
                .build();

        when(taskService.create(any(Task.class)))
                .thenThrow(new DuplicateTaskException("Duplicate Task"));

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Conflict")))
                .andExpect(jsonPath("$.message", containsString("Duplicate Task")));

        verify(taskService).create(any(Task.class));
    }

    @Test
    @DisplayName("Should update task successfully")
    void shouldUpdateTask() throws Exception {

        TaskUpdateDTO updateDTO = TaskUpdateDTO.builder()
                .title("Updated Task")
                .description("Updated Description")
                .completed(true)
                .build();

        Task updatedTask = createTask(1L, "Updated Task", "Updated Description", true);

        when(taskService.update(eq(1L), any(Task.class))).thenReturn(updatedTask);

        mockMvc.perform(put("/api/tasks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Updated Task")))
                .andExpect(jsonPath("$.completed", is(true)));

        verify(taskService).update(eq(1L), any(Task.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent task")
    void shouldReturn404WhenUpdatingNonExistentTask() throws Exception {

        TaskUpdateDTO updateDTO = TaskUpdateDTO.builder()
                .title("Updated Task")
                .description("Updated Description")
                .completed(true)
                .build();

        when(taskService.update(eq(999L), any(Task.class)))
                .thenThrow(new TaskNotFoundException(999L));

        mockMvc.perform(put("/api/tasks/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")));

        verify(taskService).update(eq(999L), any(Task.class));
    }

    @Test
    @DisplayName("Should toggle task completion status")
    void shouldToggleTaskCompletionStatus() throws Exception {

        Task toggledTask = createTask(1L, "Test Task", "Description", true);
        when(taskService.toggleCompleted(1L)).thenReturn(toggledTask);

        mockMvc.perform(patch("/api/tasks/{id}/toggle", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.completed", is(true)));

        verify(taskService).toggleCompleted(1L);
    }

    @Test
    @DisplayName("Should delete task successfully")
    void shouldDeleteTask() throws Exception {

        doNothing().when(taskService).delete(1L);

        mockMvc.perform(delete("/api/tasks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(taskService).delete(1L);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent task")
    void shouldReturn404WhenDeletingNonExistentTask() throws Exception {

        doThrow(new TaskNotFoundException(999L)).when(taskService).delete(999L);

        mockMvc.perform(delete("/api/tasks/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));

        verify(taskService).delete(999L);
    }

    @Test
    @DisplayName("Should return task statistics")
    void shouldReturnTaskStatistics() throws Exception {

        when(taskService.countCompleted()).thenReturn(5L);
        when(taskService.countPending()).thenReturn(3L);

        mockMvc.perform(get("/api/tasks/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed", is(5)))
                .andExpect(jsonPath("$.pending", is(3)))
                .andExpect(jsonPath("$.total", is(8)));

        verify(taskService).countCompleted();
        verify(taskService).countPending();
    }

    @Test
    @DisplayName("Should search tasks by keyword")
    void shouldSearchTasksByKeyword() throws Exception {

        List<Task> tasks = List.of(
                createTask(1L, "Spring Boot", "Description", false)
        );
        when(taskService.searchByKeyword("Spring")).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks/search")
                        .param("keyword", "Spring")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Spring Boot")));

        verify(taskService).searchByKeyword("Spring");
    }

    /**
     * Helper method to create test task instances.
     * Reduces code duplication and improves test readability.
     *
     * @param id Task ID
     * @param title Task title
     * @param description Task description
     * @param completed Task completion status
     * @return A new Task instance with the specified attributes
     */
    private Task createTask(Long id, String title, String description, Boolean completed) {
        Task task = Task.builder()
                .title(title)
                .description(description)
                .completed(completed)
                .build();

        if (id != null) {
            task.setId(id);
            task.setCreatedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
        }

        return task;
    }
}