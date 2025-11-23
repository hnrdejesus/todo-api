package com.github.hnrdejesus.todo_api.repository;

import com.github.hnrdejesus.todo_api.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Task entity data access operations.
 * Extends JpaRepository to provide standard CRUD operations and custom query methods.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Finds all tasks by completion status.
     *
     * @param completed True for completed tasks, false for pending tasks
     * @return List of tasks matching the completion status
     */
    List<Task> findByCompleted(Boolean completed);

    /**
     * Searches for tasks by title using case-insensitive partial matching.
     *
     * @param title The title substring to search for
     * @return List of tasks whose titles contain the search term
     */
    List<Task> findByTitleContainingIgnoreCase(String title);

    /**
     * Finds tasks by both completion status and title search.
     * Useful for filtering completed/pending tasks with a specific title pattern.
     *
     * @param completed The completion status to filter by
     * @param title The title substring to search for (case-insensitive)
     * @return List of tasks matching both criteria
     */
    List<Task> findByCompletedAndTitleContainingIgnoreCase(Boolean completed, String title);

    /**
     * Counts the number of tasks by completion status.
     *
     * @param completed True to count completed tasks, false for pending tasks
     * @return The count of tasks matching the status
     */
    Long countByCompleted(Boolean completed);

    /**
     * Checks if a task with the given title already exists.
     * Used to enforce unique title constraint.
     *
     * @param title The title to check
     * @return True if a task with this title exists, false otherwise
     */
    Boolean existsByTitle(String title);

    /**
     * Performs a full-text search across task title and description fields.
     * Uses case-insensitive LIKE pattern matching on both fields.
     *
     * @param keyword The search keyword to match against title or description
     * @return List of tasks where title or description contains the keyword
     */
    @Query("SELECT t FROM Task t WHERE " +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Task> searchByKeyword(String keyword);

    /**
     * Retrieves the 10 most recently created tasks.
     * Uses native SQL query ordered by creation timestamp descending.
     *
     * @return List of the 10 most recent tasks
     */
    @Query(value = "SELECT * FROM tasks ORDER BY created_at DESC LIMIT 10",
            nativeQuery = true)
    List<Task> findTop10RecentTasks();
}