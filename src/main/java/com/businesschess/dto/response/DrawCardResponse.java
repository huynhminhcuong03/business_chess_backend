package com.businesschess.dto.response;

import java.util.ArrayList;
import java.util.List;

import com.businesschess.enums.CardActionType;
import com.businesschess.enums.CardType;

public class DrawCardResponse {

    // Response gom cả card vừa rút và kết quả effect để FE cập nhật modal, tiền và vị trí.
    private CardResponse card;

    private CardType cardType;

    private CardActionType actionType;

    private Long gamePlayerId;

    private Integer oldPosition;

    private Integer newPosition;

    private Boolean moved = false;

    private Boolean passedStart = false;

    private Integer startReward = 0;

    private Boolean sentToJail = false;

    private Integer jailPosition;

    private Integer currentPlayerMoney;

    private Long nextPlayerId;

    // Danh sách thay đổi tiền giúp FE cập nhật nhiều player khi card thu/trả tiền giữa người chơi.
    private List<CardPlayerMoneyChangeResponse> moneyChanges = new ArrayList<>();

    public CardResponse getCard() {
        return card;
    }

    public void setCard(CardResponse card) {
        this.card = card;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public CardActionType getActionType() {
        return actionType;
    }

    public void setActionType(CardActionType actionType) {
        this.actionType = actionType;
    }

    public Long getGamePlayerId() {
        return gamePlayerId;
    }

    public void setGamePlayerId(Long gamePlayerId) {
        this.gamePlayerId = gamePlayerId;
    }

    public Integer getOldPosition() {
        return oldPosition;
    }

    public void setOldPosition(Integer oldPosition) {
        this.oldPosition = oldPosition;
    }

    public Integer getNewPosition() {
        return newPosition;
    }

    public void setNewPosition(Integer newPosition) {
        this.newPosition = newPosition;
    }

    public Boolean getMoved() {
        return moved;
    }

    public void setMoved(Boolean moved) {
        this.moved = moved;
    }

    public Boolean getPassedStart() {
        return passedStart;
    }

    public void setPassedStart(Boolean passedStart) {
        this.passedStart = passedStart;
    }

    public Integer getStartReward() {
        return startReward;
    }

    public void setStartReward(Integer startReward) {
        this.startReward = startReward;
    }

    public Boolean getSentToJail() {
        return sentToJail;
    }

    public void setSentToJail(Boolean sentToJail) {
        this.sentToJail = sentToJail;
    }

    public Integer getJailPosition() {
        return jailPosition;
    }

    public void setJailPosition(Integer jailPosition) {
        this.jailPosition = jailPosition;
    }

    public Integer getCurrentPlayerMoney() {
        return currentPlayerMoney;
    }

    public void setCurrentPlayerMoney(Integer currentPlayerMoney) {
        this.currentPlayerMoney = currentPlayerMoney;
    }

    public Long getNextPlayerId() {
        return nextPlayerId;
    }

    public void setNextPlayerId(Long nextPlayerId) {
        this.nextPlayerId = nextPlayerId;
    }

    public List<CardPlayerMoneyChangeResponse> getMoneyChanges() {
        return moneyChanges;
    }

    public void setMoneyChanges(List<CardPlayerMoneyChangeResponse> moneyChanges) {
        this.moneyChanges = moneyChanges;
    }
}
