package com.eazybytes.board.services;


import com.eazybytes.board.dtos.CreateBoardDto;
import com.eazybytes.board.dtos.UpdateBoardDto;
import com.eazybytes.board.entity.Board;

import java.util.List;
import java.util.UUID;

public interface BoardService {
    Board createBoard(UUID ownerId, CreateBoardDto createBoardDto);

    Board updateBoard(UUID ownerId, UUID taskId, UpdateBoardDto updateBoardDto);

    List<Board> getBoards(UUID ownerId);

    Board getValidBoard(UUID ownerId, UUID taskID);

    boolean deleteBoard(UUID ownerId, UUID taskID);
}