package com.businesschess.services.impl;

import com.businesschess.dto.response.BoardResponse;
import com.businesschess.entities.Board;
import com.businesschess.enums.ErrorCode;
import com.businesschess.exceptions.AppException;
import com.businesschess.mappers.BoardMapper;
import com.businesschess.repositories.BoardRepository;
import com.businesschess.services.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final BoardMapper boardMapper;

    @Override
    public BoardResponse getBoardById(Long id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        ErrorCode.BOARD_NOT_FOUND
                ));

        return boardMapper.toResponse(board);
    }
}