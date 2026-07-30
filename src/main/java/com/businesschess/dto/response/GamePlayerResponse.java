package com.businesschess.dto.response;

import java.time.LocalDateTime;

import com.businesschess.enums.TokenColor;

public class GamePlayerResponse {

    private Long id;

    private Long gameId;

    private PlayerResponse player;

    private Integer turnOrder;

    private TokenColor tokenColor;

    private Integer money;

    private Integer position;

    private Boolean inJail;

    private Integer jailTurn;

    private Boolean bankrupt;

    private Integer jailFreeCard;

    private LocalDateTime joinedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public PlayerResponse getPlayer() {
        return player;
    }

    public void setPlayer(PlayerResponse player) {
        this.player = player;
    }

    public Integer getTurnOrder() {
        return turnOrder;
    }

    public void setTurnOrder(Integer turnOrder) {
        this.turnOrder = turnOrder;
    }

    public TokenColor getTokenColor() {
        return tokenColor;
    }

    public void setTokenColor(TokenColor tokenColor) {
        this.tokenColor = tokenColor;
    }

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Boolean getInJail() {
        return inJail;
    }

    public void setInJail(Boolean inJail) {
        this.inJail = inJail;
    }

    public Integer getJailTurn() {
        return jailTurn;
    }

    public void setJailTurn(Integer jailTurn) {
        this.jailTurn = jailTurn;
    }

    public Boolean getBankrupt() {
        return bankrupt;
    }

    public void setBankrupt(Boolean bankrupt) {
        this.bankrupt = bankrupt;
    }

    public Integer getJailFreeCard() {
        return jailFreeCard;
    }

    public void setJailFreeCard(Integer jailFreeCard) {
        this.jailFreeCard = jailFreeCard;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
