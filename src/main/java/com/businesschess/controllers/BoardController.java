package com.businesschess.controllers;

import com.businesschess.common.ApiResponse;
import com.businesschess.dto.response.BoardResponse;
import com.businesschess.services.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @GetMapping("/{id}")
    public ApiResponse<BoardResponse> getBoardById(
            @PathVariable Long id
    ) {
        BoardResponse board = boardService.getBoardById(id);

        return ApiResponse.<BoardResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Get board successfully")
                .data(board)
                .build();
    }
}