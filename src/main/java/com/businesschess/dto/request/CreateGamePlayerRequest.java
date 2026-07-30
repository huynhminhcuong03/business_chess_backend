package com.businesschess.dto.request;

import com.businesschess.enums.TokenColor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateGamePlayerRequest {

    @NotNull
    private Long gameId;

    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank
    @Size(max = 100)
    private String displayName;

    @NotNull
    private TokenColor tokenColor;

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public TokenColor getTokenColor() {
        return tokenColor;
    }

    public void setTokenColor(TokenColor tokenColor) {
        this.tokenColor = tokenColor;
    }
}
