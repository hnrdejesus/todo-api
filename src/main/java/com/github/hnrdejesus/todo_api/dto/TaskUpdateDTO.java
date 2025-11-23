package com.github.hnrdejesus.todo_api.dto;

import com.github.hnrdejesus.todo_api.model.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateDTO {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Completed status is required")
    private Boolean completed;

    public Task toEntity() {
        return Task.builder()
                .title(this.title)
                .description(this.description)
                .completed(this.completed)
                .build();
    }
}
