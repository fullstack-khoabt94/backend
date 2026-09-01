package com.eazybytes.board.services;

import com.eazybytes.board.dtos.CreateBoardDto;
import com.eazybytes.board.dtos.UpdateBoardDto;
import com.eazybytes.board.entity.Board;
import com.eazybytes.board.repository.BoardRepository;
import com.eazybytes.exceptions.BadRequestException;
import com.eazybytes.exceptions.NotFoundException;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BoardServiceImpl boardService;

    @Captor
    private ArgumentCaptor<Board> boardCaptor;

    //    mock owner
    private User owner;
    private Board existBoard;

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

    }

    private CreateBoardDto createFakeBoardDto() {
        return new CreateBoardDto(
                "Test Board",
                "That's random description",
                "default_color",
                "default_icon"
        );
    }


    @Test
    @DisplayName("createBoard: mapped DTO to entity and saved by repository")
    void createBoard_shouldPersistBoardWithOwnerAndDtoFields() {
        // 1. create mock data
        CreateBoardDto createBoardDto = this.createFakeBoardDto();

        // 2. define behavior of Repositories
        when(userRepository.findById(this.owner.getId())).thenReturn(Optional.of(this.owner));
        when(boardRepository.save(any(Board.class))).thenAnswer(inv -> inv.getArgument(0));

        // 3. call service method & ensure repository is called
        Board output = this.boardService.createBoard(this.owner.getId(), createBoardDto);
        verify(boardRepository).save(this.boardCaptor.capture());

        Board savedBoard = this.boardCaptor.getValue();

        // 4. assert the result
        assertThat(savedBoard.getUser()).isSameAs(this.owner);
        assertThat(savedBoard.getTitle()).isEqualTo("Test Board");
        assertThat(savedBoard.getDescription()).isEqualTo("That's random description");
        assertThat(savedBoard.getColor()).isEqualTo("default_color");
        assertThat(savedBoard.getIcon()).isEqualTo("default_icon");
        assertThat(savedBoard.getIsArchived()).isFalse();
        assertThat(output).isSameAs(savedBoard);
    }

    @Test
    @DisplayName("createBoard: user not exist -> NotFoundException, and no created board")
    void createBoard_shouldThrowWhenUserMissing() {
        // 1. create mock data
        CreateBoardDto createBoardDto = this.createFakeBoardDto();
        UUID randomUserId = UUID.randomUUID();

        // 2. define behavior of Repositories
        when(userRepository.findById(randomUserId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> boardService.createBoard(randomUserId, createBoardDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");

        verify(boardRepository, never()).save(any());
    }

    @Test
    @DisplayName("getValidBoard: owner of the board -> board")
    void getValidBoard_returnValidBoard() {
        when(boardRepository.findById(this.existBoard.getId())).thenReturn(Optional.of(this.existBoard));
        Board found = this.boardService.getValidBoard(this.owner.getId(), this.existBoard.getId());

        assertThat(found).isSameAs(this.existBoard);
    }

    @Test
    @DisplayName("getValidBoard: not owner of the board -> BadRequestException")
    void getValidBoard_shouldRejectNonOwner() {
        UUID randomUserId = UUID.randomUUID();
        when(boardRepository.findById(this.existBoard.getId())).thenReturn(Optional.of(this.existBoard));
        assertThatThrownBy(() -> boardService.getValidBoard(randomUserId, this.existBoard.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("This owner does not own the board!");
    }

    @Test
    @DisplayName("getValidBoard: not exist board -> NotFoundException")
    void getValidBoard_shouldThrowNotFoundExceptionForNonExistBoard() {
        UUID randomBoardId = UUID.randomUUID();
        when(boardRepository.findById(randomBoardId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> boardService.getValidBoard(this.owner.getId(), randomBoardId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Board not found");
    }

    @Test
    @DisplayName("updateBoard: should overwrite fields")
    void updateBoard_shouldOverwriteFields() {
        UpdateBoardDto updateBoardDto = new UpdateBoardDto(
                "Updated title",
                "Updated description",
                "updated_color",
                "updated_icon"
        );
        when(boardRepository.findById(this.existBoard.getId())).thenReturn(Optional.of(this.existBoard));
        when(boardRepository.save(any(Board.class))).thenAnswer(inv -> inv.getArgument(0));
        Board output = this.boardService.updateBoard(this.owner.getId(), this.existBoard.getId(), updateBoardDto);

        verify(boardRepository).save(this.boardCaptor.capture());

        Board savedBoard = this.boardCaptor.getValue();
        assertThat(output).isSameAs(savedBoard);

        assertThat(savedBoard.getTitle()).isEqualTo("Updated title");
        assertThat(savedBoard.getDescription()).isEqualTo("Updated description");
        assertThat(savedBoard.getColor()).isEqualTo("updated_color");
        assertThat(savedBoard.getIcon()).isEqualTo("updated_icon");
    }

    @Test
    @DisplayName("deleteBoard: soft delete")
    void deleteBoard_shouldArchiveIsTrue() {
        when(boardRepository.findById(this.existBoard.getId())).thenReturn(Optional.of(this.existBoard));

        boolean flag = this.boardService.deleteBoard(this.owner.getId(), this.existBoard.getId());

        verify(boardRepository).save(this.boardCaptor.capture());

        Board savedBoard = this.boardCaptor.getValue();

        assertThat(flag).isTrue();
        assertThat(savedBoard.getIsArchived()).isTrue();

        verify(boardRepository, never()).delete(any());
        verify(boardRepository, never()).deleteById(any());

    }

    @Test
    @DisplayName("getBoards: should return list of board")
    void getBoards_shouldReturnListOfBoards() {
        when(userRepository.findById(this.owner.getId())).thenReturn(Optional.of(this.owner));
        when(boardRepository.findByUser(this.owner)).thenReturn(List.of(this.existBoard));

        List<Board> results = this.boardService.getBoards(this.owner.getId());

        assertThat(results).containsExactly(this.existBoard);

    }

    @Test
    @DisplayName("getBoards: should throw NotFoundException if the owner is not exist")
    void getBoards_shouldThrowIfOwnerNotExist() {
        UUID randomUserId = UUID.randomUUID();
        when(userRepository.findById(randomUserId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> boardService.getBoards(randomUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");

    }
}