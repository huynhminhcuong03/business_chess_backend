package com.businesschess.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    BOARD_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Board not found"
    ),

    BOARD_CELLS_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Board cells not found"
    ),

    BOARD_CELL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Board cell not found"
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

    PLAYER_NOT_IN_GAME(
            HttpStatus.BAD_REQUEST,
            "Player is not in this game"
    ),

    PLAYER_NOT_CURRENT_TURN(
            HttpStatus.BAD_REQUEST,
            "It is not this player's turn"
    ),

    PLAYER_NOT_ON_CELL(
            HttpStatus.BAD_REQUEST,
            "Player is not on this board cell"
    ),

    PROPERTY_NOT_PURCHASABLE(
            HttpStatus.BAD_REQUEST,
            "Property cannot be purchased"
    ),

    PROPERTY_DETAIL_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "Property detail not found"
    ),

    PROPERTY_ALREADY_OWNED(
            HttpStatus.CONFLICT,
            "Property already owned"
    ),

    PROPERTY_NOT_OWNED(
            HttpStatus.BAD_REQUEST,
            "Property is not owned"
    ),

    PROPERTY_OWNED_BY_PLAYER(
            HttpStatus.BAD_REQUEST,
            "Player owns this property"
    ),

    PLAYER_NOT_ENOUGH_MONEY(
            HttpStatus.BAD_REQUEST,
            "Player does not have enough money"
    ),

    DICE_TOTAL_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "Dice total is required"
    ),

    TAX_OPTION_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "Income tax option is required"
    ),

    PLAYER_NOT_ON_TAX_CELL(
            HttpStatus.BAD_REQUEST,
            "Player is not on a tax cell"
    ),

    PLAYER_NOT_ON_GO_TO_JAIL_CELL(
            HttpStatus.BAD_REQUEST,
            "Player is not on go to jail cell"
    ),

    PLAYER_NOT_ON_CARD_CELL(
            HttpStatus.BAD_REQUEST,
            "Player is not on a chance or community card cell"
    ),

    CARD_AMOUNT_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "Card amount is required"
    ),

    CARD_TARGET_POSITION_REQUIRED(
            HttpStatus.BAD_REQUEST,
            "Card target position is required"
    ),

    CARD_ACTION_DATA_INVALID(
            HttpStatus.BAD_REQUEST,
            "Card action data is invalid"
    ),

    PLAYER_IN_JAIL(
            HttpStatus.BAD_REQUEST,
            "Player must choose a jail action before rolling dice"
    ),

    PLAYER_NOT_IN_JAIL(
            HttpStatus.BAD_REQUEST,
            "Player is not in jail"
    ),

    JAIL_CARD_NOT_AVAILABLE(
            HttpStatus.BAD_REQUEST,
            "Player does not have a get out of jail card"
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
