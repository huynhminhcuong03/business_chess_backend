package com.businesschess.controllers;

import java.util.List;

import com.businesschess.common.ApiResponse;
import com.businesschess.common.constants.MessageConstant;
import com.businesschess.dto.request.CreateGamePlayerRequest;
import com.businesschess.dto.response.GamePlayerResponse;
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
@RequestMapping("/api/game_player")
public class GamePlayerController {

    private final GameService gameService;

    public GamePlayerController(GameService gameService) {
        this.gameService = gameService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse createGamePlayer(
            @Valid @RequestBody CreateGamePlayerRequest request
    ) {
        GamePlayerResponse gamePlayer = gameService.createGamePlayer(request);

        return ApiResponse.created(MessageConstant.GAME_PLAYER_CREATE_SUCCESS, gamePlayer);
    }

    @GetMapping("/game/{gameId}")
    public ApiResponse getGamePlayers(
            @PathVariable Long gameId
    ) {
        List<GamePlayerResponse> gamePlayers = gameService.getGamePlayers(gameId);

        return ApiResponse.ok(MessageConstant.GAME_PLAYER_LIST_SUCCESS, gamePlayers);
    }
}
