package com.github.hnrdejesus.todo_api.controller;

import com.github.hnrdejesus.todo_api.dto.TaskCreateDTO;
import com.github.hnrdejesus.todo_api.dto.TaskResponseDTO;
import com.github.hnrdejesus.todo_api.dto.TaskUpdateDTO;
import com.github.hnrdejesus.todo_api.exception.TaskNotFoundException;
import com.github.hnrdejesus.todo_api.model.Task;
import com.github.hnrdejesus.todo_api.service.TaskService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for managing task operations.
 * Handles HTTP requests and delegates business logic to the service layer.
 * All endpoints return DTOs to decouple internal models from API responses.
 */
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * Retrieves all tasks or filters by completion status.
     * Supports optional filtering via query parameter.
     *
     * @param completed Optional filter - true for completed tasks, false for pending, null for all
     * @return List of tasks matching the filter criteria
     */
    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> findAll(@RequestParam(required = false) Boolean completed) {

        List<Task> tasks;

        if (completed != null) {
            tasks = taskService.findByCompleted(completed);
        } else {
            tasks = taskService.findAll();
        }

        List<TaskResponseDTO> response = tasks.stream()
                .map(TaskResponseDTO::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single task by its unique identifier.
     *
     * @param id The task ID
     * @return The requested task
     * @throws TaskNotFoundException if no task exists with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> findById(@PathVariable Long id) {

        return taskService.findById(id)
                .map(TaskResponseDTO::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    /**
     * Searches for tasks by keyword across title and description fields.
     * Uses case-insensitive partial matching.
     *
     * @param keyword The search term to match against task content
     * @return List of tasks containing the keyword
     */
    @GetMapping("/search")
    public ResponseEntity<List<TaskResponseDTO>> search(@RequestParam String keyword) {

        List<Task> tasks = taskService.searchByKeyword(keyword);

        List<TaskResponseDTO> response = tasks.stream()
                .map(TaskResponseDTO::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new task in the system.
     * The task is automatically marked as incomplete upon creation.
     *
     * @param dto The task creation data with validated fields
     * @return The created task with generated ID and timestamp
     */
    @PostMapping
    public ResponseEntity<TaskResponseDTO> create(@Valid @RequestBody TaskCreateDTO dto) {

        Task task = dto.toEntity();

        Task created = taskService.create(task);

        TaskResponseDTO response = TaskResponseDTO.from(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing task with new information.
     * All fields in the request body will replace the current task data.
     *
     * @param id The ID of the task to update
     * @param dto The updated task data with validated fields
     * @return The updated task
     * @throws TaskNotFoundException if no task exists with the given ID
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateDTO dto) {

        Task updatedData = dto.toEntity();

        Task updated = taskService.update(id, updatedData);

        TaskResponseDTO response = TaskResponseDTO.from(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Toggles the completion status of a task.
     * Completed tasks become pending and vice versa.
     *
     * @param id The ID of the task to toggle
     * @return The task with updated completion status
     * @throws TaskNotFoundException if no task exists with the given ID
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<TaskResponseDTO> toggleCompleted(@PathVariable Long id) {

        Task toggled = taskService.toggleCompleted(id);
        TaskResponseDTO response = TaskResponseDTO.from(toggled);
        return ResponseEntity.ok(response);
    }

    /**
     * Permanently deletes a task from the system.
     *
     * @param id The ID of the task to delete
     * @throws TaskNotFoundException if no task exists with the given ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves statistics about task completion.
     * Provides counts for completed, pending, and total tasks.
     *
     * @return Task statistics summary
     */
    @GetMapping("/stats")
    public ResponseEntity<TaskStatsDTO> getStats() {

        Long completed = taskService.countCompleted();
        Long pending = taskService.countPending();
        Long total = completed + pending;

        TaskStatsDTO stats = new TaskStatsDTO(completed, pending, total);
        return ResponseEntity.ok(stats);
    }

    /**
     * Internal DTO for task statistics.
     * Kept as an inner class due to its simplicity and single-purpose use within this controller.
     */
    @Data
    @AllArgsConstructor
    private static class TaskStatsDTO {

        private Long completed;
        private Long pending;
        private Long total;
    }
}