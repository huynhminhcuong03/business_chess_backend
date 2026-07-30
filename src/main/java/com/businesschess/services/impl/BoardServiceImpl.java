package com.businesschess.services.impl;

import java.util.List;

import com.businesschess.dto.response.BoardCellResponse;
import com.businesschess.dto.response.BoardResponse;
import com.businesschess.entities.Board;
import com.businesschess.entities.BoardCell;
import com.businesschess.enums.ErrorCode;
import com.businesschess.exceptions.AppException;
import com.businesschess.mappers.BoardMapper;
import com.businesschess.repositories.BoardCellRepository;
import com.businesschess.repositories.BoardRepository;
import com.businesschess.services.BoardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardCellRepository boardCellRepository;
    private final BoardMapper boardMapper;

    public BoardServiceImpl(
            BoardRepository boardRepository,
            BoardCellRepository boardCellRepository,
            BoardMapper boardMapper
    ) {
        this.boardRepository = boardRepository;
        this.boardCellRepository = boardCellRepository;
        this.boardMapper = boardMapper;
    }

    @Override
    public BoardResponse getBoardById(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorCode.BOARD_NOT_FOUND
                ));

        return boardMapper.toResponse(board);
    }

    @Override
    public List<BoardCellResponse> getBoardCells(Long boardId) {
        ensureBoardExists(boardId);

        return boardCellRepository.findByBoardIdOrderByPositionAsc(boardId).stream()
                .map(boardMapper::toCellResponse)
                .toList();
    }

    private void ensureBoardExists(Long boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new AppException(ErrorCode.BOARD_NOT_FOUND);
        }
    }

}
