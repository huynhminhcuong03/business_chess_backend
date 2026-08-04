package com.businesschess.dto.response;

public class PayRentResponse {

    private Long boardCellId;

    private Integer boardCellPosition;

    private String boardCellName;

    private Long payerGamePlayerId;

    private Long ownerGamePlayerId;

    private Integer rentAmount;

    private Integer payerMoney;

    private Integer ownerMoney;

    public Long getBoardCellId() {
        return boardCellId;
    }

    public void setBoardCellId(Long boardCellId) {
        this.boardCellId = boardCellId;
    }

    public Integer getBoardCellPosition() {
        return boardCellPosition;
    }

    public void setBoardCellPosition(Integer boardCellPosition) {
        this.boardCellPosition = boardCellPosition;
    }

    public String getBoardCellName() {
        return boardCellName;
    }

    public void setBoardCellName(String boardCellName) {
        this.boardCellName = boardCellName;
    }

    public Long getPayerGamePlayerId() {
        return payerGamePlayerId;
    }

    public void setPayerGamePlayerId(Long payerGamePlayerId) {
        this.payerGamePlayerId = payerGamePlayerId;
    }

    public Long getOwnerGamePlayerId() {
        return ownerGamePlayerId;
    }

    public void setOwnerGamePlayerId(Long ownerGamePlayerId) {
        this.ownerGamePlayerId = ownerGamePlayerId;
    }

    public Integer getRentAmount() {
        return rentAmount;
    }

    public void setRentAmount(Integer rentAmount) {
        this.rentAmount = rentAmount;
    }

    public Integer getPayerMoney() {
        return payerMoney;
    }

    public void setPayerMoney(Integer payerMoney) {
        this.payerMoney = payerMoney;
    }

    public Integer getOwnerMoney() {
        return ownerMoney;
    }

    public void setOwnerMoney(Integer ownerMoney) {
        this.ownerMoney = ownerMoney;
    }
}
