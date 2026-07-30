package com.businesschess.controllers;

import java.util.List;

import com.businesschess.common.ApiResponse;
import com.businesschess.common.constants.MessageConstant;
import com.businesschess.dto.response.BoardCellResponse;
import com.businesschess.dto.response.BoardResponse;
import com.businesschess.services.BoardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BoardController {

    private static final Long DEFAULT_BOARD_ID = 1L;

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/board")
    public ApiResponse getBoardById() {
        BoardResponse board = boardService.getBoardById(DEFAULT_BOARD_ID);

        return ApiResponse.ok(MessageConstant.BOARD_DETAIL_SUCCESS, board);
    }

    @GetMapping("/board_cell")
    public ApiResponse getBoardCells() {
        List<BoardCellResponse> cells = boardService.getBoardCells(DEFAULT_BOARD_ID);

        return ApiResponse.ok(MessageConstant.BOARD_CELL_LIST_SUCCESS, cells);
    }
}
