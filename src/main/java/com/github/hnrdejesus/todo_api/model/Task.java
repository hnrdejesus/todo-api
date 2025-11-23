package com.github.hnrdejesus.todo_api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a task in the todo application.
 * Stores task information including title, description, completion status, and timestamps.
 * Implements automatic timestamp management through JPA lifecycle callbacks.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class Task {

    /**
     * Unique identifier for the task.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Task title/name.
     * Required field with length constraints (3-100 characters).
     * Should be descriptive and unique.
     */
    @NotBlank(message = "Title cannot be blank")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * Detailed description of the task.
     * Optional field with maximum length of 500 characters.
     * Stored as TEXT type in database for larger content support.
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(length = 500, columnDefinition = "TEXT")
    private String description;

    /**
     * Completion status of the task.
     * Defaults to false (incomplete) for new tasks.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean completed = false;

    /**
     * Timestamp of when the task was created.
     * Automatically set on entity creation and cannot be modified afterwards.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of the last modification to the task.
     * Automatically updated whenever the entity is modified.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback executed before persisting a new task.
     * Initializes both creation and update timestamps.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle callback executed before updating an existing task.
     * Updates the modification timestamp to reflect the current time.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}