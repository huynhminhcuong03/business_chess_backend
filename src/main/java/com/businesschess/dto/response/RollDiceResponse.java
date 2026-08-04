package com.businesschess.dto.response;

public class RollDiceResponse {

    private Integer dice1;

    private Integer dice2;

    private Integer total;

    private Boolean isDouble;

    private Integer oldPosition;

    private Integer newPosition;

    private Boolean passedStart;

    private Long currentPlayerId;

    private Long nextPlayerId;

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
}
