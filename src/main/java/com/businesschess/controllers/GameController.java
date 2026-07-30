package com.businesschess.controllers;

import com.businesschess.common.ApiResponse;
import com.businesschess.common.constants.MessageConstant;
import com.businesschess.dto.request.CreateGameRequest;
import com.businesschess.dto.response.GameResponse;
import com.businesschess.services.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse createGame(
            @Valid @RequestBody(required = false) CreateGameRequest request
    ) {
        GameResponse game = gameService.createGame(request);

        return ApiResponse.created(MessageConstant.GAME_CREATE_SUCCESS, game);
    }

    @GetMapping("/{id}")
    public ApiResponse getGame(
            @PathVariable Long id
    ) {
        GameResponse game = gameService.getGame(id);

        return ApiResponse.ok(MessageConstant.GAME_DETAIL_SUCCESS, game);
    }

    @PostMapping("/{id}/start")
    public ApiResponse startGame(
            @PathVariable Long id
    ) {
        GameResponse game = gameService.startGame(id);

        return ApiResponse.ok(MessageConstant.GAME_START_SUCCESS, game);
    }
}
