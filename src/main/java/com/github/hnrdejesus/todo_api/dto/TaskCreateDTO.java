package com.github.hnrdejesus.todo_api.dto;

import com.github.hnrdejesus.todo_api.model.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating new tasks.
 * Contains only the fields required for task creation with validation rules.
 * The completed status is automatically set to false.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Converts this DTO into a Task entity.
     * Sets the initial completed status to false.
     *
     * @return A new Task entity ready to be persisted
     */
    public Task toEntity() {
        return Task.builder()
                .title(this.title)
                .description(this.description)
                .completed(false)
                .build();
    }
}