package com.businesschess.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    BOARD_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Board not found"
    ),

    CARDS_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Cards not found"
    ),

    GAME_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Game not found"
    ),

    GAME_PLAYERS_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Game players not found"
    ),

    GAME_NOT_WAITING(
            HttpStatus.BAD_REQUEST,
            "Game is not waiting"
    ),

    GAME_NOT_PLAYING(
            HttpStatus.BAD_REQUEST,
            "Game is not playing"
    ),

    PLAYER_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Player not found"
    ),

    PLAYER_ALREADY_JOINED(
            HttpStatus.CONFLICT,
            "Player already joined this game"
    ),

    TOKEN_COLOR_ALREADY_USED(
            HttpStatus.CONFLICT,
            "Token color already used"
    );

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
