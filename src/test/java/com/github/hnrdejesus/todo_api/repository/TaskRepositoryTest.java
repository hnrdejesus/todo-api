package com.github.hnrdejesus.todo_api.repository;

import com.github.hnrdejesus.todo_api.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TaskRepository.
 * Uses an in-memory H2 database to test repository operations.
 * Each test runs in a transaction that is rolled back after execution.
 */
@DataJpaTest
@DisplayName("TaskRepository Integration Tests")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Sets up test data before each test execution.
     * Clears the database and populates it with three sample tasks.
     */
    @BeforeEach
    void setUp() {

        taskRepository.deleteAll();

        taskRepository.save(createTask("Learn Spring Boot", "Complete tutorial", false));
        taskRepository.save(createTask("Write Unit Tests", "Test all layers", true));
        taskRepository.save(createTask("Deploy Application", "Deploy to production", false));
    }

    @Test
    @DisplayName("Should save task successfully")
    void shouldSaveTask() {

        Task newTask = createTask("New Task", "Test description", false);

        Task savedTask = taskRepository.save(newTask);

        assertThat(savedTask).isNotNull();
        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getTitle()).isEqualTo("New Task");
        assertThat(savedTask.getCompleted()).isFalse();
        assertThat(savedTask.getCreatedAt()).isNotNull();
        assertThat(savedTask.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should find task by ID")
    void shouldFindTaskById() {

        Task task = createTask("Find Me", "Description", false);
        Task savedTask = taskRepository.save(task);

        Optional<Task> found = taskRepository.findById(savedTask.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Find Me");
    }

    @Test
    @DisplayName("Should return empty when task not found by ID")
    void shouldReturnEmptyWhenTaskNotFoundById() {

        Optional<Task> found = taskRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all tasks")
    void shouldFindAllTasks() {

        List<Task> tasks = taskRepository.findAll();

        assertThat(tasks).hasSize(3);
        assertThat(tasks).extracting(Task::getTitle)
                .containsExactlyInAnyOrder(
                        "Learn Spring Boot",
                        "Write Unit Tests",
                        "Deploy Application"
                );
    }

    @Test
    @DisplayName("Should find completed tasks")
    void shouldFindCompletedTasks() {

        List<Task> completedTasks = taskRepository.findByCompleted(true);

        assertThat(completedTasks).hasSize(1);
        assertThat(completedTasks.get(0).getTitle()).isEqualTo("Write Unit Tests");
        assertThat(completedTasks.get(0).getCompleted()).isTrue();
    }

    @Test
    @DisplayName("Should find pending tasks")
    void shouldFindPendingTasks() {

        List<Task> pendingTasks = taskRepository.findByCompleted(false);

        assertThat(pendingTasks).hasSize(2);
        assertThat(pendingTasks).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Learn Spring Boot", "Deploy Application");
    }

    @Test
    @DisplayName("Should find tasks by title containing keyword")
    void shouldFindTasksByTitleContaining() {

        List<Task> tasks = taskRepository.findByTitleContainingIgnoreCase("spring");

        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTitle()).isEqualTo("Learn Spring Boot");
    }

    @Test
    @DisplayName("Should find multiple tasks when keyword matches multiple titles")
    void shouldFindMultipleTasksWhenKeywordMatchesMultipleTitles() {

        List<Task> tasks = taskRepository.findByTitleContainingIgnoreCase("e");

        assertThat(tasks).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("Should count completed tasks")
    void shouldCountCompletedTasks() {

        Long count = taskRepository.countByCompleted(true);

        assertThat(count).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return true when task exists by title")
    void shouldReturnTrueWhenTaskExistsByTitle() {

        Boolean exists = taskRepository.existsByTitle("Learn Spring Boot");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when task does not exist by title")
    void shouldReturnFalseWhenTaskDoesNotExistByTitle() {

        Boolean exists = taskRepository.existsByTitle("Nonexistent Task");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should delete task successfully")
    void shouldDeleteTask() {

        Task task = createTask("To Delete", "Will be deleted", false);
        Task savedTask = taskRepository.save(task);
        Long taskId = savedTask.getId();

        taskRepository.deleteById(taskId);

        Optional<Task> deleted = taskRepository.findById(taskId);
        assertThat(deleted).isEmpty();

        List<Task> remaining = taskRepository.findAll();
        assertThat(remaining).hasSize(3);
    }

    @Test
    @DisplayName("Should update task successfully")
    void shouldUpdateTask() {

        Task task = createTask("Original Title", "Original description", false);
        Task savedTask = taskRepository.save(task);

        savedTask.setTitle("Updated Title");
        savedTask.setCompleted(true);
        Task updated = taskRepository.save(savedTask);

        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getCompleted()).isTrue();
        assertThat(updated.getUpdatedAt()).isNotNull();

        Task fromDb = taskRepository.findById(savedTask.getId()).get();
        assertThat(fromDb.getTitle()).isEqualTo("Updated Title");
    }

    /**
     * Helper method to create test task instances.
     * Reduces code duplication across test methods.
     *
     * @param title Task title
     * @param description Task description
     * @param completed Task completion status
     * @return A new Task instance with the specified attributes
     */
    private Task createTask(String title, String description, Boolean completed) {
        return Task.builder()
                .title(title)
                .description(description)
                .completed(completed)
                .build();
    }
}