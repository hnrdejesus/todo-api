package com.github.hnrdejesus.todo_api.dto;

import com.github.hnrdejesus.todo_api.model.Task;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
