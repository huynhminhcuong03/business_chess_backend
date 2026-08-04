package com.businesschess.mappers;

import java.util.List;

import com.businesschess.dto.response.GamePlayerResponse;
import com.businesschess.dto.response.GamePropertyResponse;
import com.businesschess.dto.response.GameResponse;
import com.businesschess.dto.response.PlayerResponse;
import com.businesschess.entities.Game;
import com.businesschess.entities.GamePlayer;
import com.businesschess.entities.GameProperty;
import com.businesschess.entities.Player;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {

    public GameResponse toResponse(
            Game game,
            List<GamePlayer> gamePlayers,
            List<GameProperty> gameProperties
    ) {
        if (game == null) {
            return null;
        }

        GameResponse response = new GameResponse();
        response.setId(game.getId());
        response.setBoardId(game.getBoard().getId());
        response.setStatus(game.getStatus());
        response.setGameMode(game.getGameMode());
        response.setCurrentPlayerId(game.getCurrentPlayer() == null ? null : game.getCurrentPlayer().getId());
        response.setWinnerId(game.getWinner() == null ? null : game.getWinner().getId());
        response.setConsecutiveDoubles(game.getConsecutiveDoubles());
        response.setCreatedAt(game.getCreatedAt());
        response.setStartedAt(game.getStartedAt());
        response.setFinishedAt(game.getFinishedAt());
        response.setUpdatedAt(game.getUpdatedAt());
        response.setPlayers(gamePlayers == null ? null : gamePlayers.stream()
                .map(this::toResponse)
                .toList());
        response.setProperties(gameProperties == null
                ? List.of()
                : gameProperties.stream()
                .map(this::toResponse)
                .toList());
        return response;
    }

    public GamePropertyResponse toResponse(GameProperty gameProperty) {
        if (gameProperty == null) {
            return null;
        }

        GamePropertyResponse response = new GamePropertyResponse();
        response.setId(gameProperty.getId());
        response.setBoardCellId(gameProperty.getBoardCell().getId());
        response.setBoardCellPosition(gameProperty.getBoardCell().getPosition());
        response.setBoardCellName(gameProperty.getBoardCell().getName());
        response.setOwnerGamePlayerId(gameProperty.getOwner().getId());
        response.setOwnerPlayerId(gameProperty.getOwner().getPlayer().getId());
        response.setHouseCount(gameProperty.getHouseCount());
        response.setHasHotel(gameProperty.getHasHotel());
        response.setMortgaged(gameProperty.getMortgaged());
        return response;
    }

    public GamePlayerResponse toResponse(GamePlayer gamePlayer) {
        if (gamePlayer == null) {
            return null;
        }

        GamePlayerResponse response = new GamePlayerResponse();
        response.setId(gamePlayer.getId());
        response.setGameId(gamePlayer.getGame().getId());
        response.setPlayer(toResponse(gamePlayer.getPlayer()));
        response.setTurnOrder(gamePlayer.getTurnOrder());
        response.setTokenColor(gamePlayer.getTokenColor());
        response.setMoney(gamePlayer.getMoney());
        response.setPosition(gamePlayer.getPosition());
        response.setInJail(gamePlayer.getInJail());
        response.setJailTurn(gamePlayer.getJailTurn());
        response.setBankrupt(gamePlayer.getBankrupt());
        response.setJailFreeCard(gamePlayer.getJailFreeCard());
        response.setJoinedAt(gamePlayer.getJoinedAt());
        return response;
    }

    public PlayerResponse toResponse(Player player) {
        if (player == null) {
            return null;
        }

        PlayerResponse response = new PlayerResponse();
        response.setId(player.getId());
        response.setUsername(player.getUsername());
        response.setDisplayName(player.getDisplayName());
        response.setCreatedAt(player.getCreatedAt());
        return response;
    }
}
