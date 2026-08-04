package com.businesschess.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.businesschess.enums.GameMode;
import com.businesschess.enums.GameStatus;

public class GameResponse {

    private Long id;

    private Long boardId;

    private GameStatus status;

    private GameMode gameMode;

    private Long currentPlayerId;

    private Long winnerId;

    private Integer consecutiveDoubles;

    private LocalDateTime createdAt;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private LocalDateTime updatedAt;

    private List<GamePlayerResponse> players;

    private List<GamePropertyResponse> properties;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBoardId() {
        return boardId;
    }

    public void setBoardId(Long boardId) {
        this.boardId = boardId;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Long getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(Long currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public Long getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }

    public Integer getConsecutiveDoubles() {
        return consecutiveDoubles;
    }

    public void setConsecutiveDoubles(Integer consecutiveDoubles) {
        this.consecutiveDoubles = consecutiveDoubles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<GamePlayerResponse> getPlayers() {
        return players;
    }

    public void setPlayers(List<GamePlayerResponse> players) {
        this.players = players;
    }

    public List<GamePropertyResponse> getProperties() {
        return properties;
    }

    public void setProperties(List<GamePropertyResponse> properties) {
        this.properties = properties;
    }
}
