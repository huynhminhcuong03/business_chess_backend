package com.businesschess.services;

import java.util.List;

import com.businesschess.dto.request.CreateGamePlayerRequest;
import com.businesschess.dto.request.CreateGameRequest;
import com.businesschess.dto.response.GamePlayerResponse;
import com.businesschess.dto.response.GameResponse;

public interface GameService {

    GameResponse createGame(CreateGameRequest request);

    GameResponse getGame(Long id);

    GameResponse startGame(Long id);

    GamePlayerResponse createGamePlayer(CreateGamePlayerRequest request);

    List<GamePlayerResponse> getGamePlayers(Long gameId);
}
