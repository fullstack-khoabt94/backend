package com.eazybytes.board.services;

import com.eazybytes.board.dtos.CreateBoardDto;
import com.eazybytes.board.dtos.UpdateBoardDto;
import com.eazybytes.board.entity.Board;
import com.eazybytes.board.repository.BoardRepository;
import com.eazybytes.exceptions.BadRequestException;
import com.eazybytes.exceptions.NotFoundException;
import com.eazybytes.user.entity.User;
import com.eazybytes.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public Board getValidBoard(UUID ownerId, UUID boardId) throws BadRequestException {
        Board board = this.boardRepository.findById(boardId).orElseThrow(() -> new NotFoundException("Board"));
        if (board.getUser().getId().equals(ownerId)) return board;
        else throw new BadRequestException("This owner does not own the board!");
    }

    @Override
    public Board createBoard(UUID ownerId, CreateBoardDto createBoardDto) {
        Board newBoard = new Board();
        User user = this.userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("User"));
        newBoard.setUser(user);
        newBoard.setTitle(createBoardDto.title());
        newBoard.setDescription(createBoardDto.description());
        newBoard.setColor(createBoardDto.color());
        newBoard.setIcon(createBoardDto.icon());

        return boardRepository.save(newBoard);

    }

    @Override
    public Board updateBoard(UUID ownerId, UUID boardId, UpdateBoardDto updateBoardDto) {
        Board board = this.getValidBoard(ownerId, boardId);
        board.setTitle(updateBoardDto.title());
        board.setDescription(updateBoardDto.description());
        board.setIcon(updateBoardDto.icon());
        board.setColor(updateBoardDto.color());

        return boardRepository.save(board);
    }

    @Override
    public List<Board> getBoards(UUID ownerId) {
        User user = this.userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException("User"));
        return this.boardRepository.findByUser(user);
    }

    @Override
    public boolean deleteBoard(UUID ownerId, UUID boardId) {
        Board board = this.getValidBoard(ownerId, boardId);
        board.setIsArchived(true);
        boardRepository.save(board);
        return true;
    }
}