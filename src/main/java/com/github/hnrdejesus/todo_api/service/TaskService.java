package com.github.hnrdejesus.todo_api.service;

import com.github.hnrdejesus.todo_api.model.Task;
import com.github.hnrdejesus.todo_api.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing Task entities.
 * Handles business logic and coordinates between controllers and repositories.
 * All write operations are transactional to ensure data consistency.
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    /**
     * Retrieves all tasks from the database.
     *
     * @return List of all tasks
     */
    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    /**
     * Finds a task by its unique identifier.
     *
     * @param id The task ID
     * @return Optional containing the task if found, empty otherwise
     */
    @Transactional(readOnly = true)
    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    /**
     * Retrieves tasks filtered by completion status.
     *
     * @param completed True for completed tasks, false for pending tasks
     * @return List of tasks matching the completion status
     */
    @Transactional(readOnly = true)
    public List<Task> findByCompleted(Boolean completed) {
        return taskRepository.findByCompleted(completed);
    }

    /**
     * Searches for tasks by title using case-insensitive partial matching.
     *
     * @param title The title substring to search for
     * @return List of tasks whose titles contain the search term
     */
    @Transactional(readOnly = true)
    public List<Task> findByTitleContaining(String title) {
        return taskRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Performs a full-text search across multiple task fields.
     * Searches in both title and description fields.
     *
     * @param keyword The search keyword
     * @return List of tasks matching the keyword
     */
    @Transactional(readOnly = true)
    public List<Task> searchByKeyword(String keyword) {
        return taskRepository.searchByKeyword(keyword);
    }

    /**
     * Creates a new task in the system.
     * Validates that the title is unique and sets the initial completion status to false.
     *
     * @param task The task to create
     * @return The persisted task with generated ID
     * @throws IllegalArgumentException if a task with the same title already exists
     */
    @Transactional
    public Task create(Task task) {
        // Enforce unique title constraint
        if (taskRepository.existsByTitle(task.getTitle())) {
            throw new IllegalArgumentException(
                    "Task with title '" + task.getTitle() + "' already exists"
            );
        }

        // Initialize new tasks as incomplete
        task.setCompleted(false);

        return taskRepository.save(task);
    }

    /**
     * Updates an existing task with new information.
     * All fields (title, description, completion status) are updated.
     *
     * @param id The ID of the task to update
     * @param updatedTask The task object containing the new values
     * @return The updated task
     * @throws IllegalArgumentException if the task is not found
     */
    @Transactional
    public Task update(Long id, Task updatedTask) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Task with id " + id + " not found"
                ));

        // Update all mutable fields
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setCompleted(updatedTask.getCompleted());

        return taskRepository.save(existingTask);
    }

    /**
     * Toggles the completion status of a task.
     * If completed, marks as pending; if pending, marks as completed.
     *
     * @param id The ID of the task to toggle
     * @return The updated task
     * @throws IllegalArgumentException if the task is not found
     */
    @Transactional
    public Task toggleCompleted(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Task with id " + id + " not found"
                ));

        // Invert current completion status
        task.setCompleted(!task.getCompleted());

        return taskRepository.save(task);
    }

    /**
     * Permanently deletes a task from the system.
     *
     * @param id The ID of the task to delete
     * @throws IllegalArgumentException if the task is not found
     */
    @Transactional
    public void delete(Long id) {
        // Validate existence before deletion
        if (!taskRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Task with id " + id + " not found"
            );
        }

        taskRepository.deleteById(id);
    }

    /**
     * Counts the total number of completed tasks.
     *
     * @return The count of completed tasks
     */
    @Transactional(readOnly = true)
    public Long countCompleted() {
        return taskRepository.countByCompleted(true);
    }

    /**
     * Counts the total number of pending (incomplete) tasks.
     *
     * @return The count of pending tasks
     */
    @Transactional(readOnly = true)
    public Long countPending() {
        return taskRepository.countByCompleted(false);
    }
}