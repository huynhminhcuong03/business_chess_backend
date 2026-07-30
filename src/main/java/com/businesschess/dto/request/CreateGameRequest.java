package com.businesschess.dto.request;

import com.businesschess.enums.GameMode;

public class CreateGameRequest {

    private GameMode gameMode;

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }
}
