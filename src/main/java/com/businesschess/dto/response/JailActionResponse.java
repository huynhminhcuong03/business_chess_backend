package com.businesschess.dto.response;

import com.businesschess.enums.JailActionType;

public class JailActionResponse {

    private JailActionType actionType;

    private Integer dice1;

    private Integer dice2;

    private Integer total;

    private Boolean isDouble;

    private Boolean moved;

    private Integer oldPosition;

    private Integer newPosition;

    private Boolean passedStart;

    private Integer startReward;

    private Integer currentPlayerMoney;

    private Long currentPlayerId;

    private Long nextPlayerId;

    private Boolean inJail;

    private Integer jailTurn;

    private Integer jailFreeCard;

    private Integer finePaid;

    public JailActionType getActionType() {
        return actionType;
    }

    public void setActionType(JailActionType actionType) {
        this.actionType = actionType;
    }

    public Integer getDice1() {
        return dice1;
    }

    public void setDice1(Integer dice1) {
        this.dice1 = dice1;
    }

    public Integer getDice2() {
        return dice2;
    }

    public void setDice2(Integer dice2) {
        this.dice2 = dice2;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Boolean getIsDouble() {
        return isDouble;
    }

    public void setIsDouble(Boolean isDouble) {
        this.isDouble = isDouble;
    }

    public Boolean getMoved() {
        return moved;
    }

    public void setMoved(Boolean moved) {
        this.moved = moved;
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

    public Integer getCurrentPlayerMoney() {
        return currentPlayerMoney;
    }

    public void setCurrentPlayerMoney(Integer currentPlayerMoney) {
        this.currentPlayerMoney = currentPlayerMoney;
    }

    public Long getCurrentPlayerId() {
        return currentPlayerId;
    }

    public void setCurrentPlayerId(Long currentPlayerId) {
        this.currentPlayerId = currentPlayerId;
    }

    public Long getNextPlayerId() {
        return nextPlayerId;
    }

    public void setNextPlayerId(Long nextPlayerId) {
        this.nextPlayerId = nextPlayerId;
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

    public Integer getFinePaid() {
        return finePaid;
    }

    public void setFinePaid(Integer finePaid) {
        this.finePaid = finePaid;
    }
}
