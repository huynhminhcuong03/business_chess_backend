package com.businesschess.dto.response;

public class GoToJailResponse {

    private Long gamePlayerId;

    private Integer fromPosition;

    private Integer jailPosition;

    private Boolean inJail;

    private Integer jailTurn;

    private Integer jailFreeCard;

    public Long getGamePlayerId() {
        return gamePlayerId;
    }

    public void setGamePlayerId(Long gamePlayerId) {
        this.gamePlayerId = gamePlayerId;
    }

    public Integer getFromPosition() {
        return fromPosition;
    }

    public void setFromPosition(Integer fromPosition) {
        this.fromPosition = fromPosition;
    }

    public Integer getJailPosition() {
        return jailPosition;
    }

    public void setJailPosition(Integer jailPosition) {
        this.jailPosition = jailPosition;
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

    public Integer getJailFreeCard() {
        return jailFreeCard;
    }

    public void setJailFreeCard(Integer jailFreeCard) {
        this.jailFreeCard = jailFreeCard;
    }
}
