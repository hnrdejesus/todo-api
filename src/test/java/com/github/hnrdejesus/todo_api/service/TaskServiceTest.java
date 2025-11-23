package com.github.hnrdejesus.todo_api.service;

import com.github.hnrdejesus.todo_api.exception.DuplicateTaskException;
import com.github.hnrdejesus.todo_api.exception.TaskNotFoundException;
import com.github.hnrdejesus.todo_api.model.Task;
import com.github.hnrdejesus.todo_api.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService.
 * Uses Mockito to mock repository dependencies and test business logic in isolation.
 * Verifies service layer behavior including validation rules and exception handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

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

        return Task.builder()
                .id(id)
                .title(title)
                .description(description)
                .completed(completed)
                .build();
    }

    @Test
    @DisplayName("Should return all tasks")
    void shouldReturnAllTasks() {

        Task task1 = createTask(1L, "Learn Spring Boot", "Complete tutorial", false);
        Task task2 = createTask(2L, "Write Unit Tests", "Test all layers", true);

        when(taskRepository.findAll()).thenReturn(Arrays.asList(task1, task2));

        List<Task> result = taskService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(task1, task2);
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return task by ID when found")
    void shouldReturnTaskByIdWhenFound() {

        Task task = createTask(1L, "Learn Spring Boot", "Complete tutorial", false);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Optional<Task> result = taskService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Learn Spring Boot");
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should return empty when task not found by ID")
    void shouldReturnEmptyWhenTaskNotFoundById() {

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Task> result = taskService.findById(999L);

        assertThat(result).isEmpty();
        verify(taskRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should return tasks filtered by completed status")
    void shouldReturnTasksFilteredByCompletedStatus() {

        Task task = createTask(2L, "Write Unit Tests", "Test all layers", true);

        when(taskRepository.findByCompleted(true)).thenReturn(List.of(task));

        List<Task> result = taskService.findByCompleted(true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCompleted()).isTrue();
        verify(taskRepository, times(1)).findByCompleted(true);
    }

    @Test
    @DisplayName("Should return tasks containing title keyword")
    void shouldReturnTasksContainingTitleKeyword() {

        Task task = createTask(1L, "Learn Spring Boot", "Complete tutorial", false);

        when(taskRepository.findByTitleContainingIgnoreCase("spring"))
                .thenReturn(List.of(task));

        List<Task> result = taskService.findByTitleContaining("spring");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("Spring");
        verify(taskRepository, times(1)).findByTitleContainingIgnoreCase("spring");
    }

    @Test
    @DisplayName("Should search tasks by keyword")
    void shouldSearchTasksByKeyword() {

        Task task = createTask(1L, "Learn Spring Boot", "Complete tutorial", false);

        when(taskRepository.searchByKeyword("tutorial"))
                .thenReturn(List.of(task));

        List<Task> result = taskService.searchByKeyword("tutorial");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).contains("tutorial");
        verify(taskRepository, times(1)).searchByKeyword("tutorial");
    }

    @Test
    @DisplayName("Should create task successfully when title is unique")
    void shouldCreateTaskSuccessfullyWhenTitleIsUnique() {

        Task newTask = createTask(null, "New Task", "Description", null);

        when(taskRepository.existsByTitle("New Task")).thenReturn(false);
        when(taskRepository.save(any(Task.class))).thenReturn(newTask);

        Task result = taskService.create(newTask);

        assertThat(result).isNotNull();
        assertThat(result.getCompleted()).isFalse();
        verify(taskRepository, times(1)).existsByTitle("New Task");
        verify(taskRepository, times(1)).save(newTask);
    }

    @Test
    @DisplayName("Should throw DuplicateTaskException when title already exists")
    void shouldThrowDuplicateTaskExceptionWhenTitleAlreadyExists() {

        Task duplicateTask = createTask(null, "Learn Spring Boot", "Duplicate", null);

        when(taskRepository.existsByTitle("Learn Spring Boot")).thenReturn(true);

        assertThatThrownBy(() -> taskService.create(duplicateTask))
                .isInstanceOf(DuplicateTaskException.class)
                .hasMessageContaining("Learn Spring Boot")
                .hasMessageContaining("already exists");

        verify(taskRepository, times(1)).existsByTitle("Learn Spring Boot");
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should update task successfully when task exists")
    void shouldUpdateTaskSuccessfullyWhenTaskExists() {

        Task existingTask = createTask(1L, "Learn Spring Boot", "Complete tutorial", false);
        Task updatedData = createTask(null, "Updated Title", "Updated Description", true);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);

        Task result = taskService.update(1L, updatedData);

        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getCompleted()).isTrue();
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(existingTask);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when updating non-existent task")
    void shouldThrowTaskNotFoundExceptionWhenUpdatingNonExistentTask() {

        Task updatedData = createTask(null, "Updated", "Updated", true);

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(999L, updatedData))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("999");

        verify(taskRepository, times(1)).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should toggle task completion status")
    void shouldToggleTaskCompletionStatus() {

        Task task = createTask(1L, "Learn Spring Boot", "Complete tutorial", false);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task result = taskService.toggleCompleted(1L);

        assertThat(result.getCompleted()).isTrue();
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when toggling non-existent task")
    void shouldThrowTaskNotFoundExceptionWhenTogglingNonExistentTask() {

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.toggleCompleted(999L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("999");

        verify(taskRepository, times(1)).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("Should delete task successfully when task exists")
    void shouldDeleteTaskSuccessfullyWhenTaskExists() {

        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);

        taskService.delete(1L);

        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when deleting non-existent task")
    void shouldThrowTaskNotFoundExceptionWhenDeletingNonExistentTask() {

        when(taskRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.delete(999L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("999");

        verify(taskRepository, times(1)).existsById(999L);
        verify(taskRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should count completed tasks")
    void shouldCountCompletedTasks() {

        when(taskRepository.countByCompleted(true)).thenReturn(5L);

        Long result = taskService.countCompleted();

        assertThat(result).isEqualTo(5L);
        verify(taskRepository, times(1)).countByCompleted(true);
    }

    @Test
    @DisplayName("Should count pending tasks")
    void shouldCountPendingTasks() {

        when(taskRepository.countByCompleted(false)).thenReturn(3L);

        Long result = taskService.countPending();

        assertThat(result).isEqualTo(3L);
        verify(taskRepository, times(1)).countByCompleted(false);
    }
}
