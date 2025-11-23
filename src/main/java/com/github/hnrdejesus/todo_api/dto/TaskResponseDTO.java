package com.github.hnrdejesus.todo_api.dto;

import com.github.hnrdejesus.todo_api.model.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for task responses.
 * Represents the complete task data returned by API endpoints.
 * Includes all task fields along with metadata like timestamps.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {

    private Long id;

    private String title;

    private String description;

    private Boolean completed;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Creates a response DTO from a Task entity.
     * Maps all entity fields to the DTO structure.
     *
     * @param task The task entity to convert
     * @return A response DTO containing the task data
     */
    public static TaskResponseDTO from(Task task) {
        return TaskResponseDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .completed(task.getCompleted())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}