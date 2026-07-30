package com.businesschess.services;

import java.util.List;

import com.businesschess.dto.response.BoardCellResponse;
import com.businesschess.dto.response.BoardResponse;

public interface BoardService {

    BoardResponse getBoardById(Long id);

    List<BoardCellResponse> getBoardCells(Long boardId);
}
