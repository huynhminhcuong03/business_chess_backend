package com.businesschess.dto.response;

public class BuyPropertyResponse {

    private Long gamePropertyId;

    private Long boardCellId;

    private Integer boardCellPosition;

    private String boardCellName;

    private Long ownerGamePlayerId;

    private Integer ownerMoney;

    private Integer buyPrice;

    public Long getGamePropertyId() {
        return gamePropertyId;
    }

    public void setGamePropertyId(Long gamePropertyId) {
        this.gamePropertyId = gamePropertyId;
    }

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

    public Long getOwnerGamePlayerId() {
        return ownerGamePlayerId;
    }

    public void setOwnerGamePlayerId(Long ownerGamePlayerId) {
        this.ownerGamePlayerId = ownerGamePlayerId;
    }

    public Integer getOwnerMoney() {
        return ownerMoney;
    }

    public void setOwnerMoney(Integer ownerMoney) {
        this.ownerMoney = ownerMoney;
    }

    public Integer getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(Integer buyPrice) {
        this.buyPrice = buyPrice;
    }
}
