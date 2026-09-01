package com.eazybytes.board.controllers;

import com.eazybytes.board.dtos.BoardResponse;
import com.eazybytes.board.dtos.CreateBoardDto;
import com.eazybytes.board.dtos.UpdateBoardDto;
import com.eazybytes.board.entity.Board;
import com.eazybytes.board.services.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(
            @Valid @RequestBody CreateBoardDto createBoardDto,
            @AuthenticationPrincipal UUID userId
    ) {
        BoardResponse newBoard = this.boardService.createBoard(userId, createBoardDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBoard);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<BoardResponse> updateBoard(
            @PathVariable UUID boardId,
            @Valid @RequestBody UpdateBoardDto updateBoardDto,
            @AuthenticationPrincipal UUID userId
    ) {
        BoardResponse updatedBoard = this.boardService.updateBoard(userId, boardId, updateBoardDto);
        return ResponseEntity.status(HttpStatus.OK).body(updatedBoard);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> getBoard(
            @PathVariable UUID boardId,
            @AuthenticationPrincipal UUID userId
    ) {
        Board board = this.boardService.getValidBoard(userId, boardId);
        return ResponseEntity.status(HttpStatus.OK).body(BoardResponse.fromBoard(board));
    }

    @GetMapping("/all")
    public ResponseEntity<List<BoardResponse>> getAllBoard(
            @AuthenticationPrincipal UUID userId
    ) {
        List<BoardResponse> boardList =
                this.boardService.getBoards(userId);
        return ResponseEntity.status(HttpStatus.OK).body(boardList);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<String> deleteBoard(
            @PathVariable UUID boardId,
            @AuthenticationPrincipal UUID userId
    ) {
        boolean isSuccess = this.boardService.deleteBoard(userId, boardId);
        return ResponseEntity.status(HttpStatus.OK).body(isSuccess ? "Done" : "Can not delete");
    }
}