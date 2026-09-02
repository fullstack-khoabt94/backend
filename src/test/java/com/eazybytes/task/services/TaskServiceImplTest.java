package com.eazybytes.task.services;

import com.eazybytes.board.entity.Board;
import com.eazybytes.board.services.BoardService;
import com.eazybytes.constant.TaskPriority;
import com.eazybytes.constant.TaskStatus;
import com.eazybytes.dtos.PagedResponse;
import com.eazybytes.exceptions.NotFoundException;
import com.eazybytes.task.dtos.CreateTaskDto;
import com.eazybytes.task.dtos.TaskResponse;
import com.eazybytes.task.dtos.UpdateTaskDto;
import com.eazybytes.task.entity.Task;
import com.eazybytes.task.repositories.TaskRepository;
import com.eazybytes.user.entity.User;
import com.eazybytes.user.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * UNIT TEST for TaskServiceImpl.
 * <p>
 * BoardService is a MOCK, not the real BoardServiceImpl: ownership of a board is
 * already covered by BoardServiceImplTest, so here we only care that this class
 * ASKS for the check and reacts correctly to either answer.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BoardService boardService;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Captor
    private ArgumentCaptor<Task> taskCaptor;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private User owner;
    private Board existBoard;
    private Task existTask;

    /**
     * A board owned by somebody else, and a task inside it — the attacker's target.
     */
    private Board otherBoard;
    private Task otherTask;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(UUID.randomUUID());
        owner.setPassword("hashed");
        owner.setName("Khoa");
        owner.setEmail("khoa@gmail.com");

        existBoard = new Board();
        existBoard.setUser(owner);
        existBoard.setId(UUID.randomUUID());
        existBoard.setTitle("Exist Board");
        existBoard.setDescription("That's random description");
        existBoard.setIcon("default_icon");
        existBoard.setColor("default_color");
        existBoard.setIsArchived(false);

        existTask = new Task();
        existTask.setId(UUID.randomUUID());
        existTask.setBoard(existBoard);
        existTask.setStatus(TaskStatus.TODO);
        existTask.setPriority(TaskPriority.MEDIUM);
        existTask.setTitle("Exist Task");
        existTask.setDescription("That's random description");

        User stranger = new User();
        stranger.setId(UUID.randomUUID());
        stranger.setEmail("stranger@gmail.com");

        otherBoard = new Board();
        otherBoard.setUser(stranger);
        otherBoard.setId(UUID.randomUUID());
        otherBoard.setTitle("Someone else's board");

        otherTask = new Task();
        otherTask.setId(UUID.randomUUID());
        otherTask.setBoard(otherBoard);
        otherTask.setStatus(TaskStatus.TODO);
        otherTask.setPriority(TaskPriority.HIGH);
        otherTask.setTitle("Victim task");
        otherTask.setDescription("Should never be reachable from another board");
    }

    private CreateTaskDto createFakeTaskDto() {
        return new CreateTaskDto(
                "Test Task",
                "That's random description",
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                null
        );
    }

    private UpdateTaskDto updateFakeTaskDto() {
        return new UpdateTaskDto(
                "Updated title",
                "Updated description",
                TaskStatus.IN_PROGRESS,
                LocalDateTime.now().plusDays(3),
                TaskPriority.HIGH
        );
    }

    /**
     * The caller owns the board named in the path.
     */
    private void givenCallerOwnsBoard() {
        when(boardService.getValidBoard(owner.getId(), existBoard.getId())).thenReturn(existBoard);
    }

    /**
     * The caller does NOT own the board named in the path — BoardService rejects it.
     * <p>
     * The exception here must mirror what the REAL BoardServiceImpl throws, or
     * these tests describe a world that no longer exists. It answers 404 rather
     * than 400 so that "not yours" and "does not exist" are indistinguishable
     * from outside.
     */
    private void givenCallerDoesNotOwnBoard() {
        when(boardService.getValidBoard(owner.getId(), existBoard.getId()))
                .thenThrow(new NotFoundException("Board"));
    }

    // ---------------------------------------------------------------- createTask

    @Test
    @DisplayName("createTask: mapped DTO to entity and saved by repository")
    void createTask_shouldPersistTaskWithOwnerAndDtoFields() {
        CreateTaskDto createTaskDto = this.createFakeTaskDto();
        givenCallerOwnsBoard();
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse output = this.taskService.createTask(
                this.owner.getId(), this.existBoard.getId(), createTaskDto);

        verify(taskRepository).save(this.taskCaptor.capture());
        Task savedTask = this.taskCaptor.getValue();

        assertThat(savedTask.getBoard()).isSameAs(this.existBoard);
        assertThat(savedTask.getBoard().getUser()).isSameAs(this.owner);
        assertThat(savedTask.getTitle()).isEqualTo("Test Task");
        assertThat(savedTask.getDescription()).isEqualTo("That's random description");
        assertThat(savedTask.getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(savedTask.getDueDate()).isNull();

        // TaskResponse is a projection of the saved entity, not the entity itself,
        // so compare the fields rather than the reference.
        assertThat(output.title()).isEqualTo(savedTask.getTitle());
        assertThat(output.status()).isEqualTo(savedTask.getStatus());
        assertThat(output.priority()).isEqualTo(savedTask.getPriority());
        assertThat(output.boardId()).isEqualTo(this.existBoard.getId());
    }

    @Test
    @DisplayName("createTask: caller does not own the board -> rejected, nothing saved")
    void createTask_shouldRejectWhenCallerDoesNotOwnBoard() {
        givenCallerDoesNotOwnBoard();

        assertThatThrownBy(() -> taskService.createTask(
                owner.getId(), existBoard.getId(), createFakeTaskDto()))
                .isInstanceOf(NotFoundException.class);

        verify(taskRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ getTask

    @Test
    @DisplayName("getTask: owner reads a task of their own board -> task")
    void getTask_shouldReturnTaskForOwner() {
        givenCallerOwnsBoard();
        when(taskRepository.findById(existTask.getId())).thenReturn(Optional.of(existTask));

        TaskResponse output = taskService.getTask(owner.getId(), existBoard.getId(), existTask.getId());

        assertThat(output.id()).isEqualTo(existTask.getId());
        assertThat(output.title()).isEqualTo("Exist Task");
        assertThat(output.boardId()).isEqualTo(existBoard.getId());
    }

    /**
     * REGRESSION TEST for the IDOR.
     * <p>
     * The attacker owns {boardId} and passes a {taskId} from someone else's board.
     * Owning the board must not be enough — the task has to live in it.
     */
    @Test
    @DisplayName("getTask: task belongs to another board -> NotFoundException")
    void getTask_shouldRejectTaskBelongingToAnotherBoard() {
        givenCallerOwnsBoard();
        when(taskRepository.findById(otherTask.getId())).thenReturn(Optional.of(otherTask));

        assertThatThrownBy(() -> taskService.getTask(
                owner.getId(), existBoard.getId(), otherTask.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Task not found");
    }

    @Test
    @DisplayName("getTask: task does not exist -> NotFoundException")
    void getTask_shouldThrowWhenTaskMissing() {
        UUID unknownTaskId = UUID.randomUUID();
        givenCallerOwnsBoard();
        when(taskRepository.findById(unknownTaskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTask(owner.getId(), existBoard.getId(), unknownTaskId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Task not found");
    }

    @Test
    @DisplayName("getTask: caller does not own the board -> rejected before the task is loaded")
    void getTask_shouldRejectWhenCallerDoesNotOwnBoard() {
        givenCallerDoesNotOwnBoard();

        assertThatThrownBy(() -> taskService.getTask(
                owner.getId(), existBoard.getId(), existTask.getId()))
                .isInstanceOf(NotFoundException.class);

        // The board check runs first, so the task is never even fetched.
        verify(taskRepository, never()).findById(any());
    }

    // --------------------------------------------------------------- updateTask

    @Test
    @DisplayName("updateTask: should overwrite fields")
    void updateTask_shouldOverwriteFields() {
        UpdateTaskDto dto = updateFakeTaskDto();
        givenCallerOwnsBoard();
        when(taskRepository.findById(existTask.getId())).thenReturn(Optional.of(existTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        TaskResponse output = taskService.updateTask(
                owner.getId(), existBoard.getId(), existTask.getId(), dto);

        verify(taskRepository).save(taskCaptor.capture());
        Task savedTask = taskCaptor.getValue();

        assertThat(savedTask.getTitle()).isEqualTo("Updated title");
        assertThat(savedTask.getDescription()).isEqualTo("Updated description");
        assertThat(savedTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(savedTask.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(savedTask.getDueDate()).isEqualTo(dto.dueDate());
        assertThat(output.title()).isEqualTo("Updated title");
    }

    /**
     * Pins the documented limitation: the path board only AUTHORISES the call,
     * it is never assigned. If someone later adds `task.setBoard(board)` to make
     * "move between boards" work, this test turns red and asks them to say so.
     */
    @Test
    @DisplayName("updateTask: never reassigns the board")
    void updateTask_shouldNotReassignBoard() {
        givenCallerOwnsBoard();
        when(taskRepository.findById(existTask.getId())).thenReturn(Optional.of(existTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        taskService.updateTask(owner.getId(), existBoard.getId(), existTask.getId(), updateFakeTaskDto());

        verify(taskRepository).save(taskCaptor.capture());
        assertThat(taskCaptor.getValue().getBoard()).isSameAs(existBoard);
    }

    /**
     * REGRESSION TEST for the IDOR — the write path this time.
     */
    @Test
    @DisplayName("updateTask: task belongs to another board -> rejected, nothing saved")
    void updateTask_shouldRejectTaskBelongingToAnotherBoard() {
        givenCallerOwnsBoard();
        when(taskRepository.findById(otherTask.getId())).thenReturn(Optional.of(otherTask));

        assertThatThrownBy(() -> taskService.updateTask(
                owner.getId(), existBoard.getId(), otherTask.getId(), updateFakeTaskDto()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Task not found");

        verify(taskRepository, never()).save(any());
        assertThat(otherTask.getTitle()).isEqualTo("Victim task");
    }

    // ---------------------------------------------------------------- getTasks

    @Test
    @DisplayName("getTasks: wraps the repository page into a PagedResponse")
    void getTasks_shouldReturnPagedResponse() {
        Pageable requested = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        givenCallerOwnsBoard();
        when(taskRepository.findByBoard(eq(existBoard), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(existTask), requested, 1));

        PagedResponse<TaskResponse> result =
                taskService.getTasks(owner.getId(), existBoard.getId(), requested);

        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).id()).isEqualTo(existTask.getId());
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.total()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("getTasks: keeps an allowed sort and appends the id tie-breaker")
    void getTasks_shouldKeepAllowedSortAndAppendIdTieBreaker() {
        Pageable requested = PageRequest.of(2, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        givenCallerOwnsBoard();
        when(taskRepository.findByBoard(eq(existBoard), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), requested, 0));

        taskService.getTasks(owner.getId(), existBoard.getId(), requested);

        verify(taskRepository).findByBoard(eq(existBoard), pageableCaptor.capture());
        Pageable sanitized = pageableCaptor.getValue();

        assertThat(sanitized.getPageNumber()).isEqualTo(2);
        assertThat(sanitized.getPageSize()).isEqualTo(10);
        assertThat(sanitized.getSort()).isEqualTo(
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
    }

    /**
     * Documents what Sorts.sanitize ACTUALLY does when nothing survives the
     * whitelist. `title` is not in ALLOWED_SORT, so it is dropped — and because
     * the tie-breaker is only appended when something remains, the result is
     * UNSORTED, not `id DESC`. See the note in the review.
     */
    @Test
    @DisplayName("getTasks: a disallowed sort is dropped and leaves the page unsorted")
    void getTasks_shouldReturnUnsortedWhenEverySortIsRejected() {
        Pageable requested = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "title"));
        givenCallerOwnsBoard();
        when(taskRepository.findByBoard(eq(existBoard), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), requested, 0));

        taskService.getTasks(owner.getId(), existBoard.getId(), requested);

        verify(taskRepository).findByBoard(eq(existBoard), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getSort().isUnsorted()).isTrue();
    }

    @Test
    @DisplayName("getTasks: caller does not own the board -> rejected, nothing queried")
    void getTasks_shouldRejectWhenCallerDoesNotOwnBoard() {
        givenCallerDoesNotOwnBoard();

        assertThatThrownBy(() -> taskService.getTasks(
                owner.getId(), existBoard.getId(), PageRequest.of(0, 20)))
                .isInstanceOf(NotFoundException.class);

        verify(taskRepository, never()).findByBoard(any(), any());
    }

    // -------------------------------------------------------------- deleteTask

    @Test
    @DisplayName("deleteTask: owner deletes their own task -> hard delete")
    void deleteTask_shouldDeleteForOwner() {
        when(taskRepository.findById(existTask.getId())).thenReturn(Optional.of(existTask));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        boolean flag = taskService.deleteTask(owner.getId(), existTask.getId());

        assertThat(flag).isTrue();
        verify(taskRepository).delete(existTask);
    }

    @Test
    @DisplayName("deleteTask: not the owner of the task -> NotFoundException, nothing deleted")
    void deleteTask_shouldRejectNonOwner() {
        when(taskRepository.findById(otherTask.getId())).thenReturn(Optional.of(otherTask));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> taskService.deleteTask(owner.getId(), otherTask.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Task not found");

        verify(taskRepository, never()).delete(any());
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteTask: task does not exist -> NotFoundException")
    void deleteTask_shouldThrowWhenTaskMissing() {
        UUID unknownTaskId = UUID.randomUUID();
        when(taskRepository.findById(unknownTaskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(owner.getId(), unknownTaskId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Task not found");

        verify(taskRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteTask: user does not exist -> NotFoundException")
    void deleteTask_shouldThrowWhenUserMissing() {
        UUID unknownUserId = UUID.randomUUID();
        when(taskRepository.findById(existTask.getId())).thenReturn(Optional.of(existTask));
        when(userRepository.findById(unknownUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(unknownUserId, existTask.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");

        verify(taskRepository, never()).delete(any());
    }
}